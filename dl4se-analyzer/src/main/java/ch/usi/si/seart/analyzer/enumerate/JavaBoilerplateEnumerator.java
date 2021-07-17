package ch.usi.si.seart.analyzer.enumerate;

import ch.usi.si.seart.model.code.Boilerplate;
import ch.usi.si.seart.treesitter.Node;

public class JavaBoilerplateEnumerator extends BoilerplateEnumerator {

    @Override
    public Boilerplate asEnum(Node node) {
        String type = node.getType();
        if (type.equals("constructor_declaration")) return Boilerplate.CONSTRUCTOR;
        if (!type.equals("method_declaration")) return null;
        Node identifier = node.getChildByFieldName("name");
        String name = identifier.getContent();
        if (name.startsWith("set")) return Boilerplate.SETTER;
        if (name.startsWith("get")) return Boilerplate.GETTER;
        switch (name) {
            case "equals":
            