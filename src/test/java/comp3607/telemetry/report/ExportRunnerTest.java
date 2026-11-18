package comp3607.telemetry.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class ExportRunnerTest {

    @Test
    void executesCommandsInSubmissionOrderAndFinishesCleanly() {
        List<String> executionOrder = Collections.synchronizedList(new ArrayList<>());
        ExportRunner runner = new ExportRunner();
        runner.start();

        runner.submit(() -> executionOrder.add("first"));
        runner.submit(() -> executionOrder.add("second"));
        runner.finish(() -> executionOrder.add("final"), 2000L);

        assertEquals(List.of("first", "second", "final"), executionOrder);
    }

    @Test
    void handlesInterruptedThreadGracefullyDuringRun() throws InterruptedException {
        ExportRunner runner = new ExportRunner();
        Thread thread = new Thread(runner);
        thread.start();

        // Interrupt while waiting on queue.poll(...)
        Thread.sleep(50);
        thread.interrupt();
        thread.join(1000L);

        assertFalse(thread.isAlive());
    }
}