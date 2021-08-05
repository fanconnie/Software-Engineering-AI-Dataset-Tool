package ch.usi.si.seart.analyzer;

import ch.usi.si.seart.analyzer.count.CharacterCounter;
import ch.usi.si.seart.analyzer.count.CodeTokenCounter;
import ch.usi.si.seart.analyzer.count.Counter;
import ch.usi.si.seart.analyzer.count.LineCounter;
import ch.usi.si.seart.analyzer.count.TokenCounter;
import ch.usi.si.seart.analyzer.enumerate.BoilerplateEnumerator;
import ch.usi.si.seart.analyzer.enumerate.Enumerator;
import ch.usi.si.seart.analyzer.extract.Extractor;
import ch.usi.si.seart.analyzer.extract.FunctionExtractorFactory;
import ch.usi.si.seart.analyzer.hash.ContentHasher;
import ch.usi.si.seart.analyzer.hash.Hasher;
import ch.usi.si.seart.analyzer.hash.SyntaxTreeHasher;
import ch.usi.si.seart.analyzer.predicate.node.ContainsErrorPredicate;
import ch.usi.si.seart.analyzer.predicate.node.ContainsNonAsciiPredicate;
import ch.usi.si.seart.analyzer.predicate.node.NodePredicate;
import ch.usi.si.seart.analyzer.predicate.path.TestFilePredicate;
import ch.usi.si.seart.analyzer.printer.NodePrinter;
import ch.usi.si.seart.analyzer.printer.OffsetSyntaxTreePrinter;
import ch.usi.si.seart.analyzer.printer.Printer;
import ch.usi.si.seart.analyzer.printer.SymbolicExpressionPrinter;
import ch.usi.si.seart.analyzer.printer.SyntaxTreePrinter;
import ch.usi.si.seart.model.code.Boilerplate;
import ch.usi.si.seart.model.code.File;
import ch.usi.si.seart.model.code.Function;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Parser;
import ch.usi.si.seart.treesitter.Point;
import ch.usi.si.seart.treesitter.Tree;
import com.google.common.io.CharStreams;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Analyzer implements AutoCloseable {

    Language language;
    Parser parser;
    Tree tree;

    LocalClone localClone;
    Path path;

    Counter lineCounter;
    Counter totalTokenCounter;
    Counter codeTokenCounter;
    Counter characterCounter;

    Hasher contentHasher;
    Hasher syntaxTreeHasher;

    NodePredicate containsError;
    NodePredicate containsNonAscii;

    Predicate<Path> testFilePredicate;

    Printer nodePrinter;
    Printer syntaxTreePrinter;
    Printer expressionPrinter;

    Extractor functionExtractor;

    Enumerator<Boilerplate> boilerplateEnumerator;

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    public Analyzer(LocalClone localClone, Path path) throws IOException {
        this(
                localClone,
                path,
                Language.associatedWith(path)
                        .stream()
                        .findFirst()
                        .orElseThrow()
        );
    }

    public Analyzer(LocalClone localClone, Path path, Language language) throws IOException {
        this.language = language;
        this.parser = Parser.getFor(language);
        this.localClone = localClone;
        this.path = path;
        this.tree = parser.parse(readFile(path));
        this.lineCounter = new LineCounter();
        this.totalTokenCounter = TokenCounter.getInstance(language);
        this.codeTokenCounter = CodeTokenCounter.getInstance(language);
        this.characterCounter = new CharacterCounter();
        this.contentHasher = new ContentHasher();
        this.syntaxTreeHasher = new SyntaxTreeHasher();
        this.containsError = new ContainsErrorPredicate();
        this.containsNonAscii = new ContainsNonAsciiPredicate();
        this.testFilePredicate = TestFilePredicate.getInstance(language);
        this.nodePrint