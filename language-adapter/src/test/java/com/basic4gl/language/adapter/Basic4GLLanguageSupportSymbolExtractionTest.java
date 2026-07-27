package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.basic4gl.desktop.spi.language.IndexedSymbol;
import com.basic4gl.desktop.spi.language.SymbolDeclaration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class Basic4GLLanguageSupportSymbolExtractionTest {

    private final Basic4GLLanguageSupport support = new Basic4GLLanguageSupport();

    @Test
    public void function_withParameters_isExtractedAsUserfunc() {
        String source = "function MyFunc(x as integer) as integer\n" + "  MyFunc = x + 1\n" + "end function\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        IndexedSymbol myFunc = symbolNamed(symbols, "MyFunc");
        assertEquals("userfunc", myFunc.kind());
        assertEquals("MyFunc(x as integer)", myFunc.signature());
    }

    @Test
    public void sub_withNoParameters_isExtractedAsUserfunc() {
        String source = "sub DoThing()\nend sub\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        IndexedSymbol doThing = symbolNamed(symbols, "DoThing");
        assertEquals("userfunc", doThing.kind());
        assertEquals("DoThing()", doThing.signature());
        assertTrue(doThing.parameters().isEmpty());
    }

    @Test
    public void function_withExplicitlyTypedParameter_populatesStructuredParameters() {
        String source = "function MyFunc(x as integer) as integer\nend function\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        assertEquals(List.of("integer x"), symbolNamed(symbols, "MyFunc").parameters());
    }

    /**
     * Regression test: parameters using Basic4GL's type-suffix shorthand ({@code $}/{@code %}/
     * {@code #}, e.g. {@code x$}) have no {@code as Type} clause at all, so the type must be
     * inferred from the suffix character - the same convention {@code dim} declarations already
     * use (see {@code inferTypeFromIdentifierSuffix}). Previously this was never done for function
     * parameters, so completions offered no argument-type information for them.
     */
    @Test
    public void function_withTypeSuffixParameters_infersTypesFromSuffix() {
        String source = "function Foo(x$, y%, z as integer, w#)\nend function\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        assertEquals(
                List.of("string x$", "integer y%", "integer z", "real w#"),
                symbolNamed(symbols, "Foo").parameters());
    }

    /**
     * Regression test: the parameter-list scanner used to double-count the signature's own opening
     * paren, so it never recognized the matching close as the end of the parameter list — the
     * function was silently dropped instead of being added to the symbol table.
     */
    @Test
    public void function_isNotSilentlyDropped() {
        String source = "function MyFunc(x as integer) as integer\nend function\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        assertTrue(
                symbols.stream().anyMatch(s -> "MyFunc".equals(s.name())),
                "expected MyFunc to be extracted, got: " + symbols);
    }

    /**
     * Regression test: "end function"/"end sub" was scanned with a lookahead that peeked at the
     * closing function/sub keyword without consuming it, so the main scan loop reprocessed that same
     * keyword token as if it were starting a brand new declaration. That swallowed whatever
     * declaration actually followed (a second function, a dim, a label, ...).
     */
    @Test
    public void declarationAfterEndFunction_isNotSwallowed() {
        String source = "function First()\nend function\n" + "\n" + "sub Second()\nend sub\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        assertTrue(symbols.stream().anyMatch(s -> "First".equals(s.name())));
        assertTrue(symbols.stream().anyMatch(s -> "Second".equals(s.name())));
    }

    @Test
    public void declarationsAfterEndSub_areNotSwallowed() {
        String source = "sub DoThing()\nend sub\n" + "\n" + "dim y as integer\n" + "Start:\n";

        List<IndexedSymbol> symbols = support.extractSymbols(source);

        assertTrue(symbols.stream().anyMatch(s -> "DoThing".equals(s.name())));
        assertTrue(symbols.stream().anyMatch(s -> "y".equals(s.name()) && "variable".equals(s.kind())));
        assertTrue(symbols.stream().anyMatch(s -> "Start".equals(s.name()) && "label".equals(s.kind())));
    }

    /**
     * {@link Basic4GLLanguageSupport#extractDeclarations} duplicates the same parameter-list and
     * end-function/end-sub scanning logic as {@link Basic4GLLanguageSupport#extractSymbols}; verify
     * it independently since the two do not share an implementation.
     */
    @Test
    public void extractDeclarations_functionIsNotSilentlyDroppedOrFollowedBySwallowedDeclaration() {
        String source =
                "function First()\nend function\n" + "\n" + "sub Second()\nend sub\n" + "\n" + "dim y as integer\n";

        List<SymbolDeclaration> declarations = support.extractDeclarations(source, "test.gb");

        assertTrue(declarations.stream().anyMatch(d -> "First".equals(d.name()) && "userfunc".equals(d.kind())));
        assertTrue(declarations.stream().anyMatch(d -> "Second".equals(d.name()) && "userfunc".equals(d.kind())));
        assertTrue(declarations.stream().anyMatch(d -> "y".equals(d.name()) && "variable".equals(d.kind())));
    }

    private static IndexedSymbol symbolNamed(List<IndexedSymbol> symbols, String name) {
        Optional<IndexedSymbol> found =
                symbols.stream().filter(s -> name.equals(s.name())).findFirst();
        assertTrue(found.isPresent(), "expected a symbol named " + name + ", got: " + symbols);
        return found.get();
    }
}
