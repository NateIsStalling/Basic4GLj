package com.basic4gl.desktop.debugger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.IServiceCollection;
import com.basic4gl.lib.util.Library;
import com.basic4gl.runtime.TomVM;
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
                false,
                false,
                null,
                Arrays.asList("-Xmx512m", "-Ddemo=true"),
                Arrays.asList("one", "two"));

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
        IAppSettings settings = new TestAppSettings(
                true,
                true,
                5005,
                Collections.emptyList(),
                Collections.emptyList());

        String[] args = invokeBuildCommandArgs(settings, "library.jar");
        String command = String.join(" ", args);

        assertTrue(command.contains("-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=y"));
    }

    private String[] invokeBuildCommandArgs(IAppSettings settings, String libraryBinPath) throws Exception {
        Method buildCommandArgs = RunHandler.class.getDeclaredMethod(
                "buildCommandArgs",
                Library.class,
                IAppSettings.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class);
        buildCommandArgs.setAccessible(true);

        Object result = buildCommandArgs.invoke(
                null,
                new TestLibrary(),
                settings,
                ".",
                libraryBinPath,
                "vmPath",
                "configPath",
                "lineMapPath");
        return (String[]) result;
    }

    private static class TestLibrary implements Library {
        @Override
        public String name() {
            return "Test Library";
        }

        @Override
        public String description() {
            return "Test";
        }

        @Override
        public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {}

        @Override
        public void init(TomBasicCompiler comp, IServiceCollection services) {}

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

