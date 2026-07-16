package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.language.spi.PluginManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceTest {

    @Mock
    TomBasicCompiler compiler;

    @Mock
    Preprocessor preprocessor;

    @Mock
    PluginManager pluginManager;

    @Test
    public void extractStringLiterals_returnsDecodedValues() {
        String source = "print \"assets\\\\image.png\"\nprint \"He said: \\\"ok\\\"\"\nprint \"line\\nfeed\"";

        LanguageService languageService = new Basic4GLLanguageService(compiler, preprocessor, pluginManager);

        List<String> literals = languageService.extractStringLiterals(source);

        assertEquals(Arrays.asList("assets\\image.png", "He said: \"ok\"", "line\\nfeed"), literals);
    }

    @Test
    public void extractStringLiterals_ignoresUnterminatedLiteral() {
        String source = "print \"complete\"\nprint \"unterminated";

        LanguageService languageService = new Basic4GLLanguageService(compiler, preprocessor, pluginManager);

        List<String> literals = languageService.extractStringLiterals(source);

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

        LanguageService languageService = new Basic4GLLanguageService(compiler, preprocessor, pluginManager);

        List<String> literals = languageService.extractStringLiterals(source.toString());

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).endsWith("asset.dat"));
    }
}
