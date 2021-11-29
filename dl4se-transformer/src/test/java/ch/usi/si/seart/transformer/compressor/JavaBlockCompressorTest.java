package ch.usi.si.seart.transformer.compressor;

import ch.usi.si.seart.transformer.JavaBaseTest;
import ch.usi.si.seart.transformer.Transformer;

class JavaBlockCompressorTest extends JavaBaseTest {

    @Override
    protected Transformer getTestSubject() {
        return new JavaBlockCompressor();
    }

    @Override
    protected String getTestInput() {
        return "class Main {\n" +
                "    p