package ch.usi.si.seart.analyzer.count;

import ch.usi.si.seart.analyzer.JavaBaseTest;
import ch.usi.si.seart.treesitter.Node;
import ch.usi.si.seart.treesitter.Tree;
import lombok.Cleanup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

class JavaLineCounterTest extends JavaBaseTest {

    private static final String message = "Total number of lines should be equal to the number of lines reported by the `lines()` method";

    @Test
    void countEmptyTest() {
        Counter counter = new LineCounter();
        Assertions.assertEquals(0, counter.count());
        Assertions.assertEquals(0, counter.count(new HashSet<>()));
    }

    @Test
    void countRootTest() {
        Counter counter = new LineCounter();
        Long actual = counter.count(tree.getRootNode());
        Assertions.assertEquals(getInput().lines().count(), actual, messa