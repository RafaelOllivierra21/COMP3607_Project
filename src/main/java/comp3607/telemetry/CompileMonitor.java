package comp3607.telemetry;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

import java.io.File;

public class CompileMonitor implements CompileListener {

    private final EventBuffer buffer;

    public CompileMonitor(EventBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void compileStarted(CompileEvent event) {
    }

    @Override
    public void compileWarning(CompileEvent event) {
    }

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

    @Override
    public void compileSucceeded(CompileEvent event) {
        if (event.isUserGeneratedCompilation()) {
            buffer.add(TelemetryEvent.COMPILE_OK, fileNames(event));
        }
    }

    @Override
    public void compileFailed(CompileEvent event) {
        if (event.isUserGeneratedCompilation()) {
            buffer.add(TelemetryEvent.COMPILE_FAILED, fileNames(event));
        }
    }

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
