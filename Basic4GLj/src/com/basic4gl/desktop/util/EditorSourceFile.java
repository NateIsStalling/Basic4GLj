package com.basic4gl.desktop.util;

import com.basic4gl.compiler.util.ISourceFile;

import javax.swing.*;
import javax.swing.text.BadLocationException;

/**
 * Created by Nate on 2/25/2015.
 */
public class EditorSourceFile implements ISourceFile {
    JTextArea sourceMemo;
    int lineNumber;
    String filename;

    public EditorSourceFile(JTextArea textArea, String filename){
        this.sourceMemo = textArea;
        this.filename = filename;
    }
    @Override
    public String GetNextLine() {
        int start, stop;
        String line;
        if (!Eof()) {
            try {

                start = sourceMemo.getLineStartOffset(lineNumber);
                stop = sourceMemo.getLineEndOffset(lineNumber);

                line = sourceMemo.getText(start, stop - start);
            } catch (BadLocationException ex){
                ex.printStackTrace();
                line = "";
            }
            lineNumber++;
            return line;
        }
        else {
            return "";
        }
    }

    @Override
    public String Filename() {
        return filename;
    }

    @Override
    public int LineNumber() {
        return lineNumber;
    }

    @Override
    public boolean Eof() {
        return lineNumber >= sourceMemo.getLineCount();
    }

    @Override
    public void Release() {

    }
}
