package ch.usi.si.seart.server.service;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.code.File;
import ch.usi.si.seart.model.code.Function;
import ch.usi.si.seart.model.task.Status;
import ch.usi.si.seart.model.user.User;
import ch.usi.si.seart.repository.CodeRepository;
import ch.usi.si.seart.repository.FileRepository;
import ch.usi.si.seart.repository.FunctionRepository;
import ch.usi.si.seart.repository.GitRepoRepository;
import ch.usi.si.seart.repository.TableRowCountRepository;
import ch.usi.si.seart.repository.TaskRepository;
import ch.usi.si.seart.views.GroupedCount;
import ch.usi.si.seart.views.TableRowCount;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Table;
import javax.persistence.Tuple;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StatisticsService {

    Long codeSize();
    Long codeLines();
    Long countUsers();
    Long countGitRepos();
    Long countFiles();
    Long countFunctions();
    Map<Language, Long> countGitReposByLanguage();
    Map<Language, Long> countFilesByLanguage();
    Map<Language, Long> countFunctionsByLanguage();
    Long countTasks();
    Long countTasks(User user);
    Map<Status, Long> countTasksByStatus();
    Map<Status, Long> countTasksByStatus(User user);
    Long getTotalTaskSize();
    Long getTotalTaskSize(User user);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StatisticsServiceImpl implements StatisticsService {

        TableRowCountRepository tableRowCountRepository;

        GitRepoRepository gitRepoRepository;
        FileRepository fileRepository;
        FunctionRepository functionRepository;
        CodeRepository codeRepository;
        TaskRepository taskRepository;

        @Override
        public Long codeSize() {
            return codeRepository.size();
        }

        @Override
        public Long codeLines() {
            return codeRepository.lines();
        }

        @Override
        public Long countUsers() {
            return count(User.class);
        }

        @Override
        public Long countGitRepos() {
            return count(GitRepo.class);
        }

        @Override
        public Long countFiles() {
            return count(File.class);
        }

        @Override
        public Long countFunctions() {
            return count(Function.class);
        }

        private Long count(Class<?> type) {
            Table table = type.getAnnotation(Table.class);
            if (table == null) throw new IllegalArgumentException("Not an entity: " + type.getSimpleName());
            String name = table.name().replaceAll("\"", "");
            return tableRowCountRepository.findById(name)
                    .map(TableRowCount::getCount)
                    .orElse(null);
        }

        @Override
        public Map<Language, Long> countGitReposByLanguage() {
            return countByLanguage(gitRepoRepository::countByLanguage);
        }

        @Override
        public Map<Language, Long> countFilesByLanguage() {
            return countByLanguage(fileRepository::countByLanguage);
        }

