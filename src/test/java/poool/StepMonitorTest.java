package poool;

import org.junit.jupiter.api.Test;
import poool.concurrent.StepMonitor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class StepMonitorTest {
    @Test
    void monitorReleasesWorkerOnNewPhase() throws Exception {
        final StepMonitor monitor = new StepMonitor();
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread worker = new Thread(() -> {
            try {
                monitor.awaitPhase(0);
                latch.countDown();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        });
        worker.start();
        monitor.beginPhase();
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        monitor.stop();
    }
}
