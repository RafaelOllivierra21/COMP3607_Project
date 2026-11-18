package comp3607.telemetry.report;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import comp3607.telemetry.EventBuffer;

class AutosaveTimerTest {

    @TempDir
    Path tempDir;

    @Test
    void stopsCleanlyWhenThreadIsInterrupted() throws InterruptedException {
        EventBuffer buffer = new EventBuffer();
        ExportRunner runner = new ExportRunner();
        ReportLocation location = new ReportLocation(tempDir.toFile());
        AutosaveTimer timer = new AutosaveTimer(buffer, runner, List.of(new JsonExporter()), location);

        Thread timerThread = new Thread(timer);
        timerThread.start();

        // Let the timer enter Thread.sleep(INTERVAL_MS), then interrupt like TelemetryExtension.terminate()
        Thread.sleep(50);
        timerThread.interrupt();
        timerThread.join(1000L);

        assertFalse(timerThread.isAlive());
    }
}