package comp3607.telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the {@link TelemetryEvent}s recorded during a session and keeps them safe
 * to use from several threads at once.
 *
 * Every monitor (class, compile, run, editor) adds events from its own thread,
 * while the report package reads them from another. Both methods are synchronized,
 * so no event is lost or half written. The monitors only record into this buffer
 * and never write files themselves.
 *
 * {@link TelemetryExtension} creates the one shared buffer and passes it to every
 * monitor and to the exporters, because the project does not use a Singleton. A
 * monitor records with {@code buffer.add(TelemetryEvent.COMPILE_OK, "Hello.java")}
 * and the report reads with {@code buffer.getEvents()}.
 */
public class EventBuffer {

    /** All events recorded so far, in the order they were added. */
    private final List<TelemetryEvent> events = new ArrayList<>();

    /** Creates an empty buffer. {@link TelemetryExtension} calls this once when the plugin starts. */
    public EventBuffer() {
    }

    /**
     * Records a new event stamped with the current time.
     *
     * @param type one of the {@link TelemetryEvent} type constants
     * @param detail extra information for this event type
     */
    public synchronized void add(String type, String detail) {
        events.add(new TelemetryEvent(type, detail, System.currentTimeMillis()));
    }

    /**
     * Returns a snapshot of the events so far. The list is a copy, so the caller
     * can read it while other threads keep adding events.
     *
     * @return a copy of the recorded events, oldest first
     */
    public synchronized List<TelemetryEvent> getEvents() {
        return new ArrayList<>(events);
    }
}
