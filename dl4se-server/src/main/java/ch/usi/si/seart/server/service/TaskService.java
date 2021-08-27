package ch.usi.si.seart.server.service;

import ch.usi.si.seart.exception.TaskNotFoundException;
import ch.usi.si.seart.model.dataset.Dataset;
import ch.usi.si.seart.model.task.Status;
import ch.usi.si.seart.model.task.Statuses;
import ch.usi.si.seart.model.task.Task;
import ch.usi.si.seart.model.task.Task_;
import ch.usi.si.seart.model.user.User;
import ch.usi.si.seart.repository.TaskRepository;
import ch.usi.si.seart.server.exception.TaskFailedException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface TaskService {

    long countActiveTasks(User user);
    boolean activeTaskExists(User user, Dataset dataset, JsonNode query, JsonNode processing);
    void create(User requester, Dataset dataset, JsonNode query, JsonNode processing, LocalDateTime requestedAt);
    Task update(Task task);
    void cancel(Task task);
    void registerException(TaskFailedException ex);
    void forEachNonExpired(Consumer<Task> consumer);
    Optional<Task> getNext();
    Page<Task> getAll(Specification<Task> specification, Pageable pageable);
    Task getWithUUID(UUID uuid);

    @Slf4j
    @Service
    @RequiredArgsConstructor(onConstructor_ = @Autowired)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class TaskServiceImpl implements TaskService, InitializingBean {

        TaskRepository taskRepository;

        TaskLockProvider locks = new TaskLockProvider();

        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        private static final class TaskLockProvider {

            ConcurrentMap<Long, Lock> map = new ConcurrentReferenceHashMap<>();

            private Lock getLock(Task task) {
                return map.compute(task.getId(), TaskLockProvider::getLock);
            }

            private static Lock getLock(Long id, Lock lock) {
                return Objects.requireNonNullElse(lock, new ReentrantLock());
            }
        }

        @Override
        public void afterPropertiesSet() {
            List<Task> tasks = taskRepository.findAllByStatus(Status.EXECUTING);
            if (tasks.isEmpty()) return;
            log.info("Returning {} interrupted tasks back to queue...", tasks.size());
            tasks.forEach(task -> task.setStatus(Status.QUEUED));
            taskRepository.saveAllAndFlush(tasks);
        }

        @Override
        public long countActiveTasks(User use