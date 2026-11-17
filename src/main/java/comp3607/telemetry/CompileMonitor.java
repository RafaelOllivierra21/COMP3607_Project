package comp3607.telemetry;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

import java.io.File;

/**
 * Observer of BlueJ's compiler. BlueJ is the subject and calls this class back
 * whenever a compile starts, succeeds, fails or reports an error.
 *
 * Only compiles the user asked for are recorded, so BlueJ's own background
 * compiles do not show up in the report. Events go into the {@link EventBuffer}.
 */
public class CompileMonitor implements CompileListener {

    /** Shared buffer the compile events are written into. */
    private final EventBuffer buffer;

    /**
     * Creates the monitor.
     *
     * @param buffer the shared buffer to record compile events into
     */
    public CompileMonitor(EventBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Records {@link TelemetryEvent#COMPILE_START} with the names of the files being compiled.
     *
     * @param event the compile event from BlueJ
     */
    @Override
    public void compileStarted(CompileEvent event) {
        if (event.isUserGeneratedCompilation()) {
            buffer.add(TelemetryEvent.COMPILE_START, fileNames(event));
        }
    }

    /**
     * Called for compiler warnings. Warnings are not part of the report, so nothing is recorded.
     *
     * @param event the compile event from BlueJ
     */
    @Override
    public void compileWarning(CompileEvent event) {
    }

    /**
     * Records {@link TelemetryEvent#COMPILE_ERROR} as {@code code|line|message}, using
     * {@link ErrorCodes#codeFor(String)} to work out the short error code.
     *
     * @param event the compile event from BlueJ, holding the error message and line number
     */
    @Override
    public void compileError(CompileEvent event) {
        if (!event.isUserGeneratedCompilation()) {
            return;
        }
        String message = event.getErrorMessage();
        if (message == null) {
            message = "";
        }
        String code = ErrorCodes.codeFor(message);
        buffer.add(TelemetryEvent.COMPILE_ERROR,
                code + "|" + event.getErrorLineNumber() + "|" + message);
    }

    /**
     * Records {@link TelemetryEvent#COMPILE_OK} with the names of the files that compiled.
     *
     * @param event the compile event from BlueJ
     */
    @Override
    public void compileSucceeded(CompileEvent event) {
        if (event.isUserGeneratedCompilation()) {
            buffer.add(TelemetryEvent.COMPILE_OK, fileNames(event));
        }
    }

    /**
     * Records {@link TelemetryEvent#COMPILE_FAILED} with the names of the files that did not compile.
     *
     * @param event the compile event from BlueJ
     */
    @Override
    public void compileFailed(CompileEvent event) {
        if (event.isUserGeneratedCompilation()) {
            buffer.add(TelemetryEvent.COMPILE_FAILED, fileNames(event));
        }
    }

    /**
     * Joins the names of the files in a compile into one comma separated string.
     *
     * @param event the compile event from BlueJ
     * @return the file names such as {@code Hello.java,Main.java}
     */
    private String fileNames(CompileEvent event) {
        StringBuilder names = new StringBuilder();
        for (File file : event.getFiles()) {
            if (names.length() > 0) {
                names.append(",");
            }
            names.append(file.getName());
        }
        return names.toString();
    }
}
