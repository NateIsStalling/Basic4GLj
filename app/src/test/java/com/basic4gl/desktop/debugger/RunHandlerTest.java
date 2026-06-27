package com.basic4gl.desktop.debugger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.basic4gl.desktop.spi.Configuration;
import com.basic4gl.desktop.spi.Target;
import com.basic4gl.language.adapter.Basic4GLDebugService;
import com.basic4gl.language.core.extensions.IAppSettings;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class RunHandlerTest {

    @Test
    public void buildCommandArgs_jarLaunch_includesConfiguredJvmOptionsWithoutDebugAgentWhenDisabled()
            throws Exception {
        IAppSettings settings = new TestAppSettings(
                false, false, null, Arrays.asList("-Xmx512m", "-Ddemo=true"), Arrays.asList("one", "two"));

        String[] args = invokeBuildCommandArgs(settings, "library.jar");
        String command = String.join(" ", args);

        assertTrue(command.contains("java"));
        assertTrue(command.contains("-Xmx512m"));
        assertTrue(command.contains("-Ddemo=true"));
        assertTrue(command.contains("-- one two"));
        assertFalse(command.contains("-agentlib:jdwp="));
    }

    @Test
    public void buildCommandArgs_jarLaunch_usesConfiguredDebugPortAndSuspendOption() throws Exception {
        IAppSettings settings = new TestAppSettings(true, true, 5005, Collections.emptyList(), Collections.emptyList());

        String[] args = invokeBuildCommandArgs(settings, "library.jar");
        String command = String.join(" ", args);

        assertTrue(command.contains("-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:5005,server=y,suspend=y"));
    }

    private String[] invokeBuildCommandArgs(IAppSettings settings, String libraryBinPath) throws Exception {
        Method buildCommandArgs = Basic4GLDebugService.class.getDeclaredMethod(
                "buildCommandArgs",
                Target.class,
                IAppSettings.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class);
        buildCommandArgs.setAccessible(true);

        Object result = buildCommandArgs.invoke(
                null,
                new TestTarget(),
                settings,
                ".",
                libraryBinPath,
                "vmPath",
                "configPath",
                "lineMapPath",
                "0000",
                false);
        return (String[]) result;
    }

    private static class TestTarget implements Target, ITargetCommandLineOptions {
        @Override
        public String name() {
            return "Test Library";
        }

        @Override
        public String description() {
            return "Test";
        }

        @Override
        public Configuration getSettings() {
            return null;
        }

        @Override
        public Configuration getConfiguration() {
            return null;
        }

        @Override
        public void setConfiguration(Configuration config) {}

        @Override
        public void loadConfiguration(InputStream stream) {}

        @Override
        public void saveConfiguration(OutputStream stream) {}

        @Override
        public void saveState(OutputStream stream) {}

        @Override
        public void loadState(InputStream stream) {}

        @Override
        public void cleanup() {}

        @Override
        public List<String> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getClassPathObjects() {
            return Collections.emptyList();
        }

        @Override
        public void reset() {}

        @Override
        public String getConfigFilePathCommandLineOption() {
            return "cfg";
        }

        @Override
        public String getLineMappingFilePathCommandLineOption() {
            return "linemap";
        }

        @Override
        public String getLogFilePathCommandLineOption() {
            return "log";
        }

        @Override
        public String getParentDirectoryCommandLineOption() {
            return "parent";
        }

        @Override
        public String getProgramFilePathCommandLineOption() {
            return "vm";
        }

        @Override
        public String getDebuggerPortCommandLineOption() {
            return "debug";
        }

        @Override
        public String getSandboxModeEnabledOption() {
            return "sandbox";
        }
    }

    private static class TestAppSettings implements IAppSettings {
        private final boolean jvmDebuggingEnabled;
        private final boolean jvmDebugSuspendUntilAttach;
        private final Integer jvmDebugPortOverride;
        private final List<String> jvmArguments;
        private final List<String> programArguments;

        private TestAppSettings(
                boolean jvmDebuggingEnabled,
                boolean jvmDebugSuspendUntilAttach,
                Integer jvmDebugPortOverride,
                List<String> jvmArguments,
                List<String> programArguments) {
            this.jvmDebuggingEnabled = jvmDebuggingEnabled;
            this.jvmDebugSuspendUntilAttach = jvmDebugSuspendUntilAttach;
            this.jvmDebugPortOverride = jvmDebugPortOverride;
            this.jvmArguments = jvmArguments;
            this.programArguments = programArguments;
        }

        @Override
        public boolean isSandboxModeEnabled() {
            return false;
        }

        @Override
        public int getSyntax() {
            return 1;
        }

        @Override
        public List<String> getProgramArguments() {
            return programArguments;
        }

        @Override
        public List<String> getJvmArguments() {
            return jvmArguments;
        }

        @Override
        public boolean isJvmDebuggingEnabled() {
            return jvmDebuggingEnabled;
        }

        @Override
        public boolean isJvmDebugSuspendUntilAttach() {
            return jvmDebugSuspendUntilAttach;
        }

        @Override
        public Integer getJvmDebugPortOverride() {
            return jvmDebugPortOverride;
        }
    }
}
