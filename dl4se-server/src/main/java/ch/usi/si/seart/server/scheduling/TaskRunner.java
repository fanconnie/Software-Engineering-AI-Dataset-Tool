
package ch.usi.si.seart.server.scheduling;

import ch.usi.si.seart.model.code.Code;
import ch.usi.si.seart.model.code.Code_;
import ch.usi.si.seart.model.dataset.Dataset;
import ch.usi.si.seart.model.task.Status;
import ch.usi.si.seart.model.task.Task;
import ch.usi.si.seart.server.exception.TaskFailedException;
import ch.usi.si.seart.server.service.CodeService;
import ch.usi.si.seart.server.service.EmailService;
import ch.usi.si.seart.server.service.FileSystemService;
import ch.usi.si.seart.server.service.TaskService;
import ch.usi.si.seart.transformer.Transformer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Iterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Expression;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskRunner implements Runnable {

    CodeService codeService;
    TaskService taskService;
    EmailService emailService;
    FileSystemService fileSystemService;
    ConversionService conversionService;

    PlatformTransactionManager transactionManager;

    @PersistenceContext
    EntityManager entityManager;

    Integer fetchSize;

    @Autowired
    public TaskRunner(
            CodeService codeService,
            TaskService taskService,
            EmailService emailService,
            FileSystemService fileSystemService,
            ConversionService conversionService,
            PlatformTransactionManager transactionManager,
            EntityManager entityManager,
            @Value("${spring.jpa.properties.hibernate.jdbc.fetch_size}") Integer fetchSize
    ) {
        this.codeService = codeService;
        this.taskService = taskService;
        this.emailService = emailService;
        this.fileSystemService = fileSystemService;
        this.conversionService = conversionService;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
        this.fetchSize = fetchSize;
    }

    @Override
    public void run() {
        log.debug("Fetching next task to execute...");
        Optional<Task> next;
        synchronized (TaskRunner.class) {
            next = taskService.getNext();
        }
        next.ifPresentOrElse(this::run, () -> log.debug("No tasks to execute!"));
    }

    @SuppressWarnings("unchecked")
    private void run(Task task) {
        UUID uuid = task.getUuid();
        Dataset dataset = task.getDataset();
        log.info("Executing task: [{}]", uuid);
        // FIXME: 06.10.2023 This will need to be changed later for other dataset types
        if (!Dataset.CODE.equals(dataset)) {
            String message = "Unsupported task type: " + dataset;
            UnsupportedOperationException cause = new UnsupportedOperationException(message);
            throw new TaskFailedException(task, cause);
        }
        Specification<Code> specification = conversionService.convert(task, Specification.class);
        Transformer transformer = conversionService.convert(task, Transformer.class);
        JsonMapper jsonMapper = conversionService.convert(task, JsonMapper.class);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        TransactionCallback callback = new TransactionCallback(task, specification, transformer, jsonMapper);
        template.setName(uuid.toString());
        template.setReadOnly(true);
        template.execute(callback);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private class TransactionCallback extends TransactionCallbackWithoutResult {

        Task task;

        Specification<Code> specification;

        Transformer transformer;

        JsonMapper jsonMapper;

        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
            try {
                Future<Long> totalResultCount = codeService.count(specification);
                Long checkpointId = Optional.ofNullable(task.getCheckpointId()).orElse(0L);
                specification = specification.and((root, criteriaQuery, criteriaBuilder) -> {
                    Expression<Long> expression = root.get(Code_.id);
                    return criteriaBuilder.greaterThan(expression, checkpointId);
                });
                Path exportPath = fileSystemService.createArchive(task);
                try (
                        Stream<Code> stream = codeService.stream(specification);
                        FileOutputStream fileOutputStream = new FileOutputStream(exportPath.toFile(), true);
                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(gzipOutputStream);
                        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)
                ) {
                    Iterable<Code> iterable = stream::iterator;
                    long count = task.getProcessedResults();
                    for (Code code: iterable) {
                        count += 1;
                        Long id = code.getId();
                        task.setCheckpointId(id);
                        task.setProcessedResults(count);

                        String original = code.getContent();
                        String transformed = transformer.apply(original);
                        code.setContent(transformed);

                        String serialized = jsonMapper.writeValueAsString(code);
                        bufferedWriter.write(serialized);
                        bufferedWriter.newLine();

                        if (count % fetchSize == 0) {
                            if (totalResultCount.isDone()) {
                                Long totalResults = totalResultCount.get();
                                task.setTotalResults(totalResults);
                            }
                            Long fileSize = fileSystemService.getArchiveSize(task);
                            task.setSize(fileSize);
                            task = taskService.update(task);
                            bufferedWriter.flush();
                            entityManager.clear();
                        }
                    }
                }

                Long totalResults = totalResultCount.get();
                Long fileSize = fileSystemService.getArchiveSize(task);
                task.setSize(fileSize);
                task.setTotalResults(totalResults);
                task.setStatus(Status.FINISHED);
                task.setFinished(LocalDateTime.now(ZoneOffset.UTC));
                task = taskService.update(task);
                log.info("Finished task:  [{}]", task.getUuid());
                emailService.sendTaskNotificationEmail(task);
            } catch (OptimisticLockingFailureException ex) {
                /*
                 * Only two threads can modify a single task:
                 *
                 *   1) The task executor
                 *   2) The /cancel route
                 *
                 * That means if the locking failure occurs here,
                 * the task has been marked as cancelled,
                 * and we should move on to executing the next task.
                 * Still, this exception should be logged at the debug level,
                 * just in case we run into other locking
                 * issues when we introduce new features...
                */
                fileSystemService.cleanArchive(task);
                log.info("Cancelled task: [{}]", task.getUuid());
                log.debug("", ex);
            } catch (Throwable thr) {
                throw new TaskFailedException(task, thr);
            }
        }
    }
}