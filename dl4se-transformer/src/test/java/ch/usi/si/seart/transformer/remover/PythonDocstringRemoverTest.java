package ch.usi.si.seart.transformer.remover;

import ch.usi.si.seart.transformer.PythonBaseTest;
import ch.usi.si.seart.transformer.Transformer;

class PythonDocstringRemoverTest extends PythonBaseTest {

    @Override
    protected Transformer getTestSubject() {
        return new