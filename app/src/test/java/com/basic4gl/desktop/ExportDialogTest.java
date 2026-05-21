package com.basic4gl.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ExportDialogTest {

    @Test
    public void extractStringLiterals_returnsDecodedValues() {
        String source = "print \"assets\\\\image.png\"\nprint \"He said: \\\"ok\\\"\"\nprint \"line\\nfeed\"";

        List<String> literals = ExportDialog.extractStringLiterals(source);

        assertEquals(Arrays.asList("assets\\image.png", "He said: \"ok\"", "line\\nfeed"), literals);
    }

    @Test
    public void extractStringLiterals_ignoresUnterminatedLiteral() {
        String source = "print \"complete\"\nprint \"unterminated";

        List<String> literals = ExportDialog.extractStringLiterals(source);

        assertEquals(Collections.singletonList("complete"), literals);
    }

    @Test
    public void extractStringLiterals_handlesLargeEscapedInputWithoutRecursion() {
        StringBuilder source = new StringBuilder("print ");
        source.append('"');
        for (int i = 0; i < 200000; i++) {
            source.append("\\\\");
        }
        source.append("asset.dat");
        source.append('"');

        List<String> literals = ExportDialog.extractStringLiterals(source.toString());

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).endsWith("asset.dat"));
    }
}



