package com.basic4gl.desktop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditorSettingsFactoryTest {

    private Path storageDir;

    @Before
    public void createStorageDir() throws IOException {
        storageDir = Files.createTempDirectory("editor-settings-test");
    }

    @After
    public void deleteStorageDir() throws IOException {
        Files.deleteIfExists(storageDir.resolve(EditorSettingsFactory.CONFIG_FILE_NAME));
        Files.deleteIfExists(storageDir);
    }

    @Test
    public void loadFrom_missingFile_defaultsAutocompleteSettingsToEnabled() throws IOException {
        EditorSettings settings = EditorSettingsFactory.loadFrom(storageDir.toString());

        assertTrue(settings.autoCompleteEnabled);
        assertTrue(settings.showFunctionSignatures);
    }

    @Test
    public void saveThenLoad_roundTripsAutocompleteSettings() throws IOException {
        EditorSettings settings = new EditorSettings();
        settings.autoCompleteEnabled = false;
        settings.showFunctionSignatures = false;

        EditorSettingsFactory.save(settings, storageDir.toString());
        EditorSettings loaded = EditorSettingsFactory.loadFrom(storageDir.toString());

        assertFalse(loaded.autoCompleteEnabled);
        assertFalse(loaded.showFunctionSignatures);
    }

    @Test
    public void saveThenLoad_roundTripsMixedAutocompleteSettings() throws IOException {
        EditorSettings settings = new EditorSettings();
        settings.autoCompleteEnabled = true;
        settings.showFunctionSignatures = false;

        EditorSettingsFactory.save(settings, storageDir.toString());
        EditorSettings loaded = EditorSettingsFactory.loadFrom(storageDir.toString());

        assertTrue(loaded.autoCompleteEnabled);
        assertFalse(loaded.showFunctionSignatures);
    }
}
