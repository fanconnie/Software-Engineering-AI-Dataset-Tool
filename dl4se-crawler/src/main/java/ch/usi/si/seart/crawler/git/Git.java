package ch.usi.si.seart.crawler.git;

import ch.usi.si.seart.model.Language;
import com.google.common.collect.ObjectArrays;
import com.google.common.io.CharStreams;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class used for interacting with the Git version control system.
 *
 * @author dabico
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Git implements AutoCloseable {

    private static final String repoLinkPattern = "https://github.com/%s.git";

    Path localDir;
    String name;
    String url;

    /**
     * Creates a new {@code Git} object, performing a regular clone operation in the process.
     * Clones a repository based on its full name: {@code {user}/{repo}}, into a directory of our choosing.
     * Primary use case is when we are interested in the commit history of a repository.
     * For the operation to succeed, the chosen directory <b>must be empty</b>.
     *
     * @param name The fully qualified name of the repository
     * @param localDir The directory in which the repository will be cloned
     * @throws GitException In case the remote does not exist, or the supplied directory is not empty.
     */
    public Git(String name, Path localDir) throws GitException {
        this(name, localDir, false);
    }

    /**
     * Creates a new {@code Git} object, performing a regular or shallow clone operation in the process.
     * Clones a repository based on its full name: {@code {user}/{repo}}, into a directory of our choosing.
     * A shallow clone includes all files from the latest revision of the default branch, but no project history
     * information (save for the latest commit). As such, the primary use case is when we are interested only in the
     * repository file contents of the latest snapshot. For the operation to succeed, the chosen directory
     * <b>must be empty</b>.
     *
     * @param name The fully qualified name of the repository
     * @param localDir The directory in which the repository will be cloned
     * @param shallow Whether the clone should be shallow
     * @throws GitException In case the remote does not exist, or the supplied directory is not empty.
     */
    public Git(String name, Path localDir, boolean shallow) throws GitException {
        this.name = name;
        this.localDir = localDir;
        this.url 