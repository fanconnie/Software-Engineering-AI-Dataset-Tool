package ch.usi.si.seart.analyzer.printer;

import ch.usi.si.seart.analyzer.JavaBaseTest;
import ch.usi.si.seart.treesitter.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

class SyntaxTreePrinterTest extends JavaBaseTest {

    @Test
    void printNothingTest() {
        Printer printer = new SyntaxTreePrinter();
        Assertions.assertEquals("", printer.print());
        Assertions.assertEquals("", printer.print(new HashSet<>()));
    }

    @Test
    void printRootTest() {
        Printer printer = new SyntaxTreePrinter();
        Node root = tree.getRootNode();
        String actual = printer.print(root);
        String expected =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                SyntaxTreePrinter.TAG_OPEN +
                    "<program start=\"0:0\" end=\"7:1\">" +
                        "<package_declaration start=\"0:0\" end=\"0:18\">" +
                            "<scoped_identifier start=\"0:8\" end=\"0:17\">" +
                                "<scoped_identifier name=\"scope\" start=\"0:8\" end=\"0:14\">" +
                                    "<identifier name=\"scope\" start=\"0:8\" end=\"0:10\">" +
                                    "</identifier>" +
                                    "<identifier name=\"name\" start=\"0:11\" end=\"0:14\">" +
                                    "</identifier>" +
                                "</scoped_identifier>" +
                                "<identifier name=\"name\" start=\"0:15\" end=\"0:17\">" +
                                "</identifier>" +
                            "</scoped_identifier>" +
                        "</package_declaration>" +
                        "<class_declaration start=\"2:0\" end=\"7:1\">" +
                            "<modifiers start=\"2:0\" end=\"2:6\">" +
                            "</modifiers>" +
                            "<identifier name=\"name\" start=\"2:13\" end=\"2:17\">" +
                            "</identifier>" +
                            "<class_body name=\"body\" start=\"2:18\" end=\"7:1\">" +
                                "<method_declaration start=\"3:4\" end=\"6:5\">" +
                                    "<modifiers start=\"3:4\" end=\"3:17\">" +
                                    "</modifiers>" +
                                    "<void_type name=\"type\" start=\"3:18\" end=\"3:22\">" +
                                    "</void_type>" +
                                    "<identifier name=\"name\" start=\"3:23\" end=\"3:27\">" +
                                    "</identifier>" +
                                    "<formal_parameters name=\"parameters\" start=\"3:27\" end=\"3:42\">" +
                                        "<formal_parameter start=\"3:28\" end=\"3:41\">" +
                                            "<array_type name=\"type\" start=\"3:28\" end=\"3:36\">" +
                                                "<type_identifier name=\"element\" start=\"3:28\" end=\"3:34\">" +
                                                "</type_identifier>" +
                                                "<dimensions name=\"dimensions\" start=\"3:34\" end=\"3:36\">" +
                                                "</dimensions>" +
                                            "</array_type>" +
                                            "<identifier name=\"name\" start=\"3:37\" end=\"3:41\">" +
                                            "</identifier>" +
                                        "</formal_parameter>" +
                                    "</for