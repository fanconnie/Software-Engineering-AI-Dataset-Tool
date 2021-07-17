package ch.usi.si.seart.analyzer.extract;

import ch.usi.si.seart.treesitter.Capture;
import ch.usi.si.seart.treesitter.Language;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Query;
import ch.usi.si.seart.treesitter.QueryCursor;
import ch.usi.si.seart.treesitter.QueryMatch;
import ch.usi.si.seart.treesitter.Tree;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class QueryBasedExtractor implements Extractor {

    protected abstract Language getLanguage();

    protected abstract List<String> getPatterns();

    protected Node getStartingNode(Tree tree) {
        return tree.getRootNode();
    }

    private void validate(Query query) {
        Set<String> captures = query.getCaptures().stream()
                .map(Capture::getName)
                .collect(Collectors.toUnmodifiableSet());
        if (!captures.contains("target"))
      