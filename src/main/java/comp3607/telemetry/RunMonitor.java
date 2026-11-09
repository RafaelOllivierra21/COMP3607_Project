package comp3607.telemetry;

import bluej.extensions2.BObject;
import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

public class RunMonitor implements InvocationFinishedListener {

    private final EventBuffer buffer;

    public RunMonitor(EventBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void invocationFinished(InvocationFinishedEvent event) {
        String method = event.getMethodName();
        if (method == null) {
            method = "unknown";
        }

        String resultText = "";
        try {
            Object result = event.getResult();
            if (result != null && !(result instanceof BObject)) {
                resultText = result.toString();
            }
        } catch (RuntimeException e) {
            resultText = "";
        }

        buffer.add(TelemetryEvent.RUN,
                event.getClassName() + "." + method + " | " + status(event) + " | " + resultText);
    }

    private String status(InvocationFinishedEvent event) {
        switch (event.getEventType()) {
            case NORMAL_EXIT:
                return "normal";
            case EXCEPTION_EXIT:
                return "exception";
            case TERMINATED_EXIT:
                return "terminated";
            default:
                return "unknown";
        }
    }
}
