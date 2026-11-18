package comp3607.telemetry;

import bluej.extensions2.BObject;
import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

/**
 * Listens for finished method and constructor runs as an Observer of BlueJ. BlueJ is
 * the subject and calls this class back each time something finishes running, for
 * example when the user calls a method on the object bench.
 *
 * Each run is recorded into the {@link EventBuffer} as {@code Class.method | status | result}.
 * {@link TelemetryExtension} creates the monitor and registers it with BlueJ when the
 * plugin starts.
 */
public class RunMonitor implements InvocationFinishedListener {

    /** Shared buffer the run events are written into. */
    private final EventBuffer buffer;

    /**
     * Creates the monitor.
     *
     * @param buffer the shared buffer to record run events into
     */
    public RunMonitor(EventBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Records {@link TelemetryEvent#RUN} when a run finishes. If BlueJ does not give a
     * method name (constructors, exceptions, terminated runs) {@code unknown} is used.
     * Object results are left out because BlueJ only gives a reference to them.
     *
     * @param event the invocation event from BlueJ
     */
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

    /**
     * Turns how the run ended into a short word for the report.
     *
     * @param event the invocation event from BlueJ
     * @return {@code normal}, {@code exception}, {@code terminated} or {@code unknown}
     */
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
