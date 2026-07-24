package com.basic4gl.language.adapter.util;

import com.basic4gl.desktop.spi.language.IndexedSymbol;
import com.basic4gl.desktop.spi.language.LangToken;
import com.basic4gl.desktop.spi.language.SymbolDeclaration;
import com.basic4gl.desktop.spi.language.TypeDefinition;
import com.basic4gl.language.adapter.antlr.Basic4GL;
import com.basic4gl.language.core.types.BasicValType;
import com.basic4gl.language.core.types.ValType;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public final class LanguageUtil {
    private LanguageUtil() {}

    //    public static VariableDefinition toVariableDefinition() {
    //        return new VariableDefinition()
    //    }
    //
    //
    //    public static VariableDefinition buildVariableDefinition(String ) {
    //
    //    }

    public static TypeDefinition toTypeDefinition(ValType type) {
        String name = getTypeString(type);
        return new TypeDefinition(name, "", "", "");
    }

    public static TypeDefinition toTypeDefinition(int type) {
        String name = getTypeString(type);
        return new TypeDefinition(name, "", "", "");
    }

    public static String getTypeString(ValType type) {
        if (type == null) {
            return "???";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < type.getVirtualPointerLevel(); i++) {
            result.append('&');
        }
        result.append(getTypeString(type.basicType));
        for (int i = 0; i < type.arrayLevel; i++) {
            result.append("()");
        }
        return result.toString();
    }

    public static String getTypeString(int type) {
        switch (type) {
            case BasicValType.VTP_INT:
                return "int";
            case BasicValType.VTP_REAL:
                return "real";
            case BasicValType.VTP_STRING:
                return "string";
            default:
                return "???";
        }
    }

    public static Basic4GL createLexer(String input) {
        Basic4GL lexer = new Basic4GL(CharStreams.fromString(input));
        lexer.removeErrorListeners(); // suppress console noise on partial / invalid source
        return lexer;
    }

    public static LangToken toLangToken(Token t) {
        int start = t.getStartIndex();
        // getStopIndex() is inclusive; LangToken.end is exclusive
        int end = t.getStopIndex() + 1;
        return new LangToken(t.getType(), t.getText(), start, end);
    }

    /** Returns the first non-whitespace token at or after position {@code from}, or null. */
    public static Token peekNonWs(List<Token> tokens, int from) {
        for (int i = from; i < tokens.size(); i++) {
            int type = tokens.get(i).getType();
            if (type != Basic4GL.WS && type != Token.EOF) {
                return tokens.get(i);
            }
        }
        return null;
    }

    public static void addFirstLabel(Map<String, IndexedSymbol> out, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("label", name, null);
        out.putIfAbsent(key, new IndexedSymbol("label", name, name + ":"));
    }

    public static void addFirstFunction(Map<String, IndexedSymbol> out, String name, String signature) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("userfunc", name, null);
        out.putIfAbsent(key, new IndexedSymbol("userfunc", name, signature));
    }

    public static void addFirstStruct(Map<String, IndexedSymbol> out, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("struc", name, null);
        out.putIfAbsent(key, new IndexedSymbol("struc", name, "struc " + name));
    }

    public static void flushVariable(
            Map<String, IndexedSymbol> out,
            Map<String, Integer> variableDeclCounts,
            String name,
            String type,
            String currentRoutine) {
        if (name == null || name.isBlank()) {
            return;
        }

        String scope = currentRoutine == null ? "global" : currentRoutine;
        String key = symbolKey("variable", name, scope);
        int declCount = variableDeclCounts.merge(key, 1, Integer::sum);

        String baseSig = (type != null && !type.isBlank()) ? type + " " + name : name;
        String scopedSig = baseSig + " [scope: " + scope + "]";
        String sig = declCount > 1 ? scopedSig + " [re-dim x" + declCount + "]" : scopedSig;
        out.put(key, new IndexedSymbol("variable", name, sig));
    }

    public static void emitVariableDeclaration(
            List<SymbolDeclaration> declarations,
            Map<String, Integer> variableDeclCounts,
            Token nameToken,
            String type,
            String currentRoutine,
            String fileId) {
        String name = nameToken.getText();
        if (name == null || name.isBlank()) {
            return;
        }
        String scope = currentRoutine == null ? "global" : currentRoutine;
        String key = symbolKey("variable", name, scope);
        int declCount = variableDeclCounts.merge(key, 1, Integer::sum);
        String baseSig = (type != null && !type.isBlank()) ? type + " " + name : name;
        String scopedSig = baseSig + " [scope: " + scope + "]";
        String sig = declCount > 1 ? scopedSig + " [re-dim x" + declCount + "]" : scopedSig;

        declarations.add(new SymbolDeclaration(
                "variable",
                name,
                sig,
                scope,
                declCount,
                fileId,
                Math.max(0, nameToken.getLine() - 1),
                Math.max(0, nameToken.getCharPositionInLine())));
    }

    public static String symbolKey(String kind, String name, String scope) {
        String normalizedName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (scope == null || scope.isBlank()) {
            return kind + "|" + normalizedName;
        }
        return kind + "|" + scope.toLowerCase(Locale.ROOT) + "|" + normalizedName;
    }

    /**
     * Infer the type of a variable from its identifier suffix.
     * Returns the inferred type, or null if no suffix.
     *
     * <ul>
     *   <li>{@code #} or {@code !} → "real"</li>
     *   <li>{@code $} → "string"</li>
     *   <li>{@code %} → "integer"</li>
     *   <li>no suffix → null (undefined type)</li>
     * </ul>
     */
    public static String inferTypeFromIdentifierSuffix(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        char last = identifier.charAt(identifier.length() - 1);
        return switch (last) {
            case '#', '!' -> "real";
            case '$' -> "string";
            case '%' -> "integer";
            default -> null;
        };
    }
}
