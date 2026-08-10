package com.basic4gl.desktop.debugger;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class VmWorkerTest {

    @Test
    public void cancel_beforeWorkerStarts_countsDownCompletionLatch() throws Exception {
        CountDownLatch completionLatch = new CountDownLatch(1);
        VmWorker worker = new VmWorker(new TestFileProvider());
        worker.setCompletionLatch(completionLatch);

        assertTrue(worker.cancelWorker(true));

        assertTrue(completionLatch.await(1, TimeUnit.SECONDS));
    }

    private static class TestFileProvider implements IFileProvider {
        @Override
        public void useAppDirectory() {}

        @Override
        public void useCurrentDirectory() {}
    }
}
