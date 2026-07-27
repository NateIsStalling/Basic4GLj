package com.basic4gl.desktop.language;

import com.basic4gl.language.adapter.antlr.Basic4GL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;

/**
 * Fold parser for Basic4GL code based on ANTLR tokenization.
 *
 * <p>Recognizes all folding structures:
 *
 * <ul>
 *   <li>{@code function Name(...) ... end function}</li>
 *   <li>{@code sub Name(...) ... end sub}</li>
 *   <li>{@code if ... else ... elseif ... endif}</li>
 *   <li>{@code for ... next}</li>
 *   <li>{@code while ... wend}</li>
 *   <li>{@code struc Name ... endstruc}</li>
 *   <li>{@code type Name ... end type}</li>
 *   <li>{@code label: ... next label:} (labels as implicit scopes)</li>
 * </ul>
 *
 * <p>Uses the same ANTLR lexer as {@link com.basic4gl.language.adapter.Basic4GLLanguageSupport} to ensure consistent
 * tokenization, correctly handling comments and strings.
 *
 */
public class Basic4GLFoldParser implements FoldParser {
    private static final int FOLD_TYPE_CODE = 0;

    private enum ScopeKind {
        BLOCK,
        LABEL
    }

    private static final class Scope {
        final ScopeKind kind;
        final String blockType;
        final Fold fold;

        Scope(ScopeKind kind, String blockType, Fold fold) {
            this.kind = kind;
            this.blockType = blockType;
            this.fold = fold;
        }
    }

    @Override
    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        List<Fold> folds = new ArrayList<>();
        if (textArea == null || textArea.getDocument().getLength() == 0) {
            return folds;
        }

        Document doc = textArea.getDocument();
        String docText;
        try {
            docText = doc.getText(0, doc.getLength());
        } catch (Exception e) {
            return folds;
        }

        // Use the same lexer as Basic4GLLanguageSupport for consistency
        Basic4GL lexer = new Basic4GL(CharStreams.fromString(docText));
        lexer.removeErrorListeners(); // suppress console noise
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        List<Token> tokens = stream.getTokens();

        Deque<Scope> scopes = new ArrayDeque<>();
        int lastContentEndOffset = -1;

        for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            Token token = tokens.get(tokenIndex);
            int type = token.getType();
            int tokenOffset = token.getStartIndex();

            // Track the end offset of meaningful tokens so EOF fold closure has a target.
            if (type != Basic4GL.WS && type != Basic4GL.NEWLINE && type != Token.EOF) {
                lastContentEndOffset = token.getStopIndex();
            }

            if (type == Basic4GL.IDENTIFIER) {
                Token next = peekNextNonWs(tokens, tokenIndex + 1);
                if (next != null && next.getType() == Basic4GL.COLON) {
                    int prevIndex = tokenIndex - 1;
                    while (prevIndex >= 0 && tokens.get(prevIndex).getType() == Basic4GL.WS) {
                        prevIndex--;
                    }
                    boolean atStatementStart = prevIndex < 0
                            || tokens.get(prevIndex).getType() == Basic4GL.NEWLINE
                            || tokens.get(prevIndex).getType() == Basic4GL.COLON;
                    if (atStatementStart) {
                        // Labels fall through, so each new label opens a nested region.
                        openScope(scopes, folds, textArea, ScopeKind.LABEL, "label", tokenOffset);
                        continue;
                    }
                }
            }

            if (type == Basic4GL.ENDFUNCTION_KW) {
                closeToBlock(scopes, folds, "function", tokenOffset);
            } else if (type == Basic4GL.ENDSUB_KW) {
                closeToBlock(scopes, folds, "sub", tokenOffset);
            } else if (type == Basic4GL.FUNCTION_KW || type == Basic4GL.SUB_KW) {
                openScope(scopes, folds, textArea, ScopeKind.BLOCK, toLower(token.getText()), tokenOffset);
            } else if (type == Basic4GL.IF_KW) {
                openScope(scopes, folds, textArea, ScopeKind.BLOCK, "if", tokenOffset);
            } else if (type == Basic4GL.FOR_KW) {
                openScope(scopes, folds, textArea, ScopeKind.BLOCK, "for", tokenOffset);
            } else if (type == Basic4GL.WHILE_KW) {
                openScope(scopes, folds, textArea, ScopeKind.BLOCK, "while", tokenOffset);
            } else if (type == Basic4GL.STRUC_KW || type == Basic4GL.TYPE_KW) {
                openScope(scopes, folds, textArea, ScopeKind.BLOCK, toLower(token.getText()), tokenOffset);
            } else if (type == Basic4GL.ENDIF_KW) {
                closeToBlock(scopes, folds, "if", tokenOffset);
            } else if (type == Basic4GL.NEXT_KW) {
                closeToBlock(scopes, folds, "for", tokenOffset);
            } else if (type == Basic4GL.WEND_KW) {
                closeToBlock(scopes, folds, "while", tokenOffset);
            } else if (type == Basic4GL.ENDSTRUC_KW) {
                closeToBlock(scopes, folds, "struc", tokenOffset);
            } else if (type == Basic4GL.END_KW) {
                int nextIndex = findNextNonWsIndex(tokens, tokenIndex + 1);
                if (nextIndex > -1) {
                    Token nextToken = tokens.get(nextIndex);
                    String endType = endBlockType(nextToken);
                    if (endType != null) {
                        closeToBlock(scopes, folds, endType, tokenOffset);
                        tokenIndex = nextIndex;
                    }
                }
            } else if (type == Basic4GL.RETURN_KW) {
                // Returning from a gosub/function closes fall-through label scopes.
                closeWhile(scopes, folds, ScopeKind.LABEL, tokenOffset);
            }
        }

        if (lastContentEndOffset > -1) {
            while (!scopes.isEmpty()) {
                closeOne(scopes, folds, lastContentEndOffset);
            }
        }

        return folds;
    }

    /**
     * Peek at the next non-whitespace/non-newline token at the given index.
     */
    private Token peekNextNonWs(List<Token> tokens, int fromIndex) {
        int index = findNextNonWsIndex(tokens, fromIndex);
        return index > -1 ? tokens.get(index) : null;
    }

    private static int findNextNonWsIndex(List<Token> tokens, int fromIndex) {
        for (int i = fromIndex; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType() != Basic4GL.WS && t.getType() != Basic4GL.NEWLINE && t.getType() != Token.EOF) {
                return i;
            }
        }
        return -1;
    }

    private static String endBlockType(Token token) {
        int type = token.getType();
        if (type == Basic4GL.FUNCTION_KW) {
            return "function";
        } else if (type == Basic4GL.SUB_KW) {
            return "sub";
        } else if (type == Basic4GL.TYPE_KW) {
            return "type";
        } else if (type == Basic4GL.IF_KW) {
            return "if";
        }
        return null;
    }

    private static String toLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static void openScope(
            Deque<Scope> scopes,
            List<Fold> roots,
            RSyntaxTextArea textArea,
            ScopeKind kind,
            String blockType,
            int startOffset) {
        try {
            Fold fold;
            Scope parent = scopes.peek();
            if (parent == null) {
                fold = new Fold(FOLD_TYPE_CODE, textArea, startOffset);
                roots.add(fold);
            } else {
                fold = parent.fold.createChild(FOLD_TYPE_CODE, startOffset);
            }
            scopes.push(new Scope(kind, blockType, fold));
        } catch (BadLocationException ignored) {
            // Ignore malformed partial source while typing.
        }
    }

    private static void closeOne(Deque<Scope> scopes, List<Fold> roots, int endOffset) {
        if (scopes.isEmpty()) {
            return;
        }
        Scope scope = scopes.pop();
        try {
            scope.fold.setEndOffset(Math.max(scope.fold.getStartOffset(), endOffset));
            if (scope.fold.isOnSingleLine()) {
                if (!scope.fold.removeFromParent()) {
                    roots.remove(scope.fold);
                }
            }
        } catch (BadLocationException ignored) {
            // Ignore malformed partial source while typing.
        }
    }

    private static void closeToBlock(Deque<Scope> scopes, List<Fold> roots, String blockType, int endOffset) {
        while (!scopes.isEmpty()) {
            Scope top = scopes.peek();
            closeOne(scopes, roots, endOffset);
            if (top.kind == ScopeKind.BLOCK && blockType.equals(top.blockType)) {
                break;
            }
        }
    }

    private static void closeWhile(Deque<Scope> scopes, List<Fold> roots, ScopeKind kind, int endOffset) {
        while (!scopes.isEmpty() && scopes.peek().kind == kind) {
            closeOne(scopes, roots, endOffset);
        }
    }
}
