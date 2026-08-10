package com.basic4gl.desktop.content;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class FileEditorDefaultNameTest {

    @Test
    public void firstDefaultNameDoesNotUseNumericSuffix() {
        assertEquals("Untitled", FileEditor.getNextDefaultName(List.of()));
    }

    @Test
    public void defaultNamesUseLowestAvailableIndex() {
        assertEquals("Untitled 2", FileEditor.getNextDefaultName(List.of("Untitled")));
        assertEquals("Untitled 2", FileEditor.getNextDefaultName(List.of("Untitled", "Untitled 3")));
        assertEquals("Untitled 3", FileEditor.getNextDefaultName(List.of("Untitled", "Untitled 2")));
    }

    @Test
    public void defaultNameMatchingIgnoresNonDefaultNames() {
        assertEquals(
                "Untitled 2",
                FileEditor.getNextDefaultName(List.of("Untitled", "Untitled 1", "Untitled-two", "program.gb")));
    }
}
