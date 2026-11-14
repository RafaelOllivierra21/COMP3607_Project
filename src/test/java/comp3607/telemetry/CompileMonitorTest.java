package comp3607.telemetry;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import bluej.extensions2.event.CompileEvent;

public class CompileMonitorTest {

    // Stub simulating BlueJ 6.0.1 CompileEvent without opening BlueJ GUI
    private static class StubCompileEvent extends CompileEvent {
        private final boolean userGenerated;
        private final String errorMessage;
        private final int lineNumber;
        private final File[] files;

        StubCompileEvent(boolean userGenerated, String errorMessage, int lineNumber, File[] files) {
            super(CompileEvent.EventType.COMPILE_START_EVENT, userGenerated, files == null ? new File[0] : files);
            this.userGenerated = userGenerated;
            this.errorMessage = errorMessage;
            this.lineNumber = lineNumber;
            this.files = files;
        }

        @Override public boolean isUserGeneratedCompilation() { return userGenerated; }
        @Override public String getErrorMessage() { return errorMessage; }
        @Override public int getErrorLineNumber() { return lineNumber; }
        @Override public File[] getFiles() { return files == null ? new File[0] : files; }
    }

    @Test
    void testUserCompilationLifecycle() {
        EventBuffer buffer = new EventBuffer();
        CompileMonitor monitor = new CompileMonitor(buffer);
        File[] files = new File[]{ new File("Main.java"), new File("Helper.java") };

        monitor.compileStarted(new StubCompileEvent(true, null, 0, files));
        monitor.compileSucceeded(new StubCompileEvent(true, null, 0, files));

        List<TelemetryEvent> events = buffer.getEvents();
        assertEquals(2, events.size());
        assertEquals(TelemetryEvent.COMPILE_START, events.get(0).getType());
        assertEquals("Main.java,Helper.java", events.get(0).getDetail());
        assertEquals(TelemetryEvent.COMPILE_OK, events.get(1).getType());
    }

    @Test
    void testCompileErrorFormattingAndFailedEvent() {
        EventBuffer buffer = new EventBuffer();
        CompileMonitor monitor = new CompileMonitor(buffer);
        File[] files = new File[]{ new File("Main.java") };

        monitor.compileError(new StubCompileEvent(true, "';' expected", 14, files));
        monitor.compileFailed(new StubCompileEvent(true, null, 0, files));

        List<TelemetryEvent> events = buffer.getEvents();
        assertEquals(2, events.size());
        assertEquals(TelemetryEvent.COMPILE_ERROR, events.get(0).getType());
        assertEquals("E02|14|';' expected", events.get(0).getDetail());
        assertEquals(TelemetryEvent.COMPILE_FAILED, events.get(1).getType());
    }

    @Test
    void testBackgroundCompilationIgnored() {
        EventBuffer buffer = new EventBuffer();
        CompileMonitor monitor = new CompileMonitor(buffer);

        monitor.compileError(new StubCompileEvent(false, "';' expected", 5, new File[0]));
        monitor.compileSucceeded(new StubCompileEvent(false, null, 0, new File[0]));

        assertTrue(buffer.getEvents().isEmpty());
    }
}