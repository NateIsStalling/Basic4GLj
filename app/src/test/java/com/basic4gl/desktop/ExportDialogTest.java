package com.basic4gl.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ExportDialogTest {

    @Test
    public void extractStringLiterals_preservesBackslashesLiterally() {
        // Basic4GL string literals have no escape sequences, so backslashes are
        // ordinary characters and must be returned exactly as written.
        String source = "print \"assets\\\\image.png\"\nprint \"line\\nfeed\"";

        List<String> literals = ExportDialog.extractStringLiterals(source);

        assertEquals(Arrays.asList("assets\\\\image.png", "line\\nfeed"), literals);
    }

    @Test
    public void extractStringLiterals_quoteAlwaysTerminatesEvenAfterBackslash() {
        // A '"' always ends the literal, even when immediately preceded by a
        // backslash, so "He said: \"ok\"" is two adjacent literals, not one
        // literal containing an escaped quote.
        String source = "print \"He said: \\\"ok\\\"\"";

        List<String> literals = ExportDialog.extractStringLiterals(source);

        assertEquals(Arrays.asList("He said: \\", ""), literals);
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
