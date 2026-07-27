package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.basic4gl.app.desktop.config.EditorAppSettings;
import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.Configuration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ConfigurationMapperTest {

    @Test
    void toEditorConfiguration_runtimeConfigCopiesAllSettings() {
        com.basic4gl.language.core.runtime.Configuration runtime =
                new com.basic4gl.language.core.runtime.Configuration();
        runtime.addSetting(new String[] {"Header"}, com.basic4gl.language.core.runtime.Configuration.PARAM_HEADING, "");
        runtime.addSetting(
                new String[] {"Toggle"},
                com.basic4gl.language.core.runtime.Configuration.PARAM_BOOL,
                "true",
                "Toggle info");

        Configuration mapped = ConfigurationMapper.toEditorConfiguration(runtime);

        assertNotNull(mapped);
        assertEquals(2, mapped.getSettingCount());
        assertEquals("Header", mapped.getField(0)[0]);
        assertEquals(Configuration.PARAM_BOOL, mapped.getParamType(1));
        assertEquals("true", mapped.getValue(1));
        assertEquals("Toggle info", mapped.getFieldInfoText(1));

        runtime.getField(0)[0] = "Changed";
        assertEquals("Header", mapped.getField(0)[0]);
    }

    @Test
    void toRuntimeConfiguration_editorConfigCopiesAllSettings() {
        Configuration editor = new Configuration();
        editor.addSetting(new String[] {"Name"}, Configuration.PARAM_STRING, "value", "Name info");

        com.basic4gl.language.core.runtime.Configuration mapped = ConfigurationMapper.toRuntimeConfiguration(editor);

        assertNotNull(mapped);
        assertEquals(1, mapped.getSettingCount());
        assertEquals("Name", mapped.getField(0)[0]);
        assertEquals(com.basic4gl.language.core.runtime.Configuration.PARAM_STRING, mapped.getParamType(0));
        assertEquals("value", mapped.getValue(0));
        assertEquals("Name info", mapped.getFieldInfoText(0));
    }

    @Test
    void appSettingsRoundTrip_preservesValues() {
        EditorAppSettings source = new EditorAppSettings();
        source.setSandboxModeEnabled(false);
        source.setSyntax(2);
        source.setProgramArguments(Arrays.asList("-one", "value two"));
        source.setJvmArguments(Arrays.asList("-Xmx512m", "-Dmode=test"));
        source.setJvmDebuggingEnabled(true);
        source.setJvmDebugSuspendUntilAttach(true);
        source.setJvmDebugPortOverride(5005);
        source.setPluginDirectories(Arrays.asList("/tmp/plugins", "/opt/plugins"));

        Configuration configuration = ConfigurationMapper.toEditorConfiguration(source);
        IConfigurableAppSettings mapped = ConfigurationMapper.toAppSettings(configuration);

        assertFalse(mapped.isSandboxModeEnabled());
        assertEquals(2, mapped.getSyntax());
        assertEquals(Arrays.asList("-one", "value two"), mapped.getProgramArguments());
        assertEquals(Arrays.asList("-Xmx512m", "-Dmode=test"), mapped.getJvmArguments());
        assertTrue(mapped.isJvmDebuggingEnabled());
        assertTrue(mapped.isJvmDebugSuspendUntilAttach());
        assertEquals(5005, mapped.getJvmDebugPortOverride());
        assertEquals("/tmp/plugins", mapped.getPluginDirectory());
        assertEquals(Arrays.asList("/tmp/plugins", "/opt/plugins"), mapped.getPluginDirectories());
    }

    @Test
    void toAppSettings_handlesMissingAndInvalidValues() {
        Configuration configuration = new Configuration();
        configuration.addSetting(new String[] {"Editor"}, Configuration.PARAM_HEADING, "");
        configuration.addSetting(new String[] {"Sandbox Mode"}, Configuration.PARAM_BOOL, "true");
        configuration.addSetting(new String[] {"Syntax"}, Configuration.PARAM_INT, "1");
        configuration.addSetting(new String[] {"Program Arguments"}, Configuration.PARAM_STRING, "");
        configuration.addSetting(new String[] {"JVM Arguments"}, Configuration.PARAM_STRING, "");
        configuration.addSetting(new String[] {"Enable JVM Debugger"}, Configuration.PARAM_BOOL, "false");
        configuration.addSetting(new String[] {"Suspend Until Attach"}, Configuration.PARAM_BOOL, "false");
        configuration.addSetting(new String[] {"Debug Port Override"}, Configuration.PARAM_INT, "not-a-port");

        IConfigurableAppSettings mapped = ConfigurationMapper.toAppSettings(configuration);

        assertTrue(mapped.isSandboxModeEnabled());
        assertNull(mapped.getJvmDebugPortOverride());
        assertNull(mapped.getPluginDirectory());
    }
}
