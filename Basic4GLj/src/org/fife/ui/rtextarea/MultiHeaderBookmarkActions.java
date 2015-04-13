package org.fife.ui.rtextarea;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by Nate on 3/22/2015.
 */
public class MultiHeaderBookmarkActions {
    /**
     * Action to jump to the next bookmark.
     */
    public static final String rtaMultiHeaderNextBookmarkAction		= "RTA.NextBookmarkAction";

    /**
     * Action to jump to the previous bookmark.
     */
    public static final String rtaMultiHeaderPrevBookmarkAction		= "RTA.PrevBookmarkAction";

    /**
     * Toggles whether the current line has a bookmark, if this text area
     * is in an {@link RMultiHeaderScrollPane}.
     */
    public static final String rtaMultiHeaderToggleBookmarkAction		= "RTA.ToggleBookmarkAction";

    /**
     * Action that moves the caret to the next (or previous) bookmark.
     */
    public static class MultiHeaderNextBookmarkAction extends RecordableTextAction {
        private int headerIndex;
        private boolean forward;

        public MultiHeaderNextBookmarkAction(String name, boolean forward, int headerIndex) {
            super(name);
            this.forward = forward;
            this.headerIndex = headerIndex;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            MultiHeaderGutter gutter = null;
            Container parent = textArea.getParent();
            if(parent instanceof JViewport) {
                parent = parent.getParent();
                if(parent instanceof RMultiHeaderScrollPane) {
                    RMultiHeaderScrollPane sp = (RMultiHeaderScrollPane)parent;
                    gutter = sp.getGutter();
                }
            }

            if (gutter!=null) {
                if (headerIndex > -1 && headerIndex < gutter.getIconRowHeaderCount()) {
                    try {

                        GutterIconInfo[] bookmarks = gutter.getBookmarks(headerIndex);
                        if (bookmarks.length == 0) {
                            UIManager.getLookAndFeel().
                                    provideErrorFeedback(textArea);
                            return;
                        }

                        GutterIconInfo moveTo = null;
                        int curLine = textArea.getCaretLineNumber();

                        if (forward) {
                            for (int i = 0; i < bookmarks.length; i++) {
                                GutterIconInfo bookmark = bookmarks[i];
                                int offs = bookmark.getMarkedOffset();
                                int line = textArea.getLineOfOffset(offs);
                                if (line > curLine) {
                                    moveTo = bookmark;
                                    break;
                                }
                            }
                            if (moveTo == null) { // Loop back to beginning
                                moveTo = bookmarks[0];
                            }
                        } else {
                            for (int i = bookmarks.length - 1; i >= 0; i--) {
                                GutterIconInfo bookmark = bookmarks[i];
                                int offs = bookmark.getMarkedOffset();
                                int line = textArea.getLineOfOffset(offs);
                                if (line < curLine) {
                                    moveTo = bookmark;
                                    break;
                                }
                            }
                            if (moveTo == null) { // Loop back to end
                                moveTo = bookmarks[bookmarks.length - 1];
                            }
                        }

                        int offs = moveTo.getMarkedOffset();
                        if (textArea instanceof RSyntaxTextArea) {
                            RSyntaxTextArea rsta = (RSyntaxTextArea) textArea;
                            if (rsta.isCodeFoldingEnabled()) {
                                rsta.getFoldManager().
                                        ensureOffsetNotInClosedFold(offs);
                            }
                        }
                        int line = textArea.getLineOfOffset(offs);
                        offs = textArea.getLineStartOffset(line);
                        textArea.setCaretPosition(offs);

                    } catch (BadLocationException ble) { // Never happens
                        UIManager.getLookAndFeel().
                                provideErrorFeedback(textArea);
                        ble.printStackTrace();
                    }
                }
            }

        }

        @Override
        public final String getMacroID() {
            return getName();
        }

    }

    /**
     * Toggles whether the current line has a bookmark.
     */
    public static class MultiHeaderToggleBookmarkAction extends RecordableTextAction {
        private int headerIndex;

        public MultiHeaderToggleBookmarkAction(String name,int headerIndex) {
            super(name);
            this.headerIndex = headerIndex;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            MultiHeaderGutter gutter = null;
            Container parent = textArea.getParent();
            if(parent instanceof JViewport) {
                parent = parent.getParent();
                if(parent instanceof RMultiHeaderScrollPane) {
                    RMultiHeaderScrollPane sp = (RMultiHeaderScrollPane)parent;
                    gutter = sp.getGutter();
                }
            }

            if (gutter!=null) {
                int line = textArea.getCaretLineNumber();
                try {
                    gutter.toggleBookmark(headerIndex, line);
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().
                            provideErrorFeedback(textArea);
                    ble.printStackTrace();
                }
            }
        }

        @Override
        public final String getMacroID() {
            return getName();
        }

    }

}
