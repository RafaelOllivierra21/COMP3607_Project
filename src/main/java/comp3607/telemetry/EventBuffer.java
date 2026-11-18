package comp3607.telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe list of {@link TelemetryEvent}s shared by the whole plugin.
 *
 * Every monitor (class, compile, run, editor) adds events from its own thread,
 * while the report package reads them from another. The monitors only record
 * into this buffer and never write files themselves.
 */
public class EventBuffer {

    /** All events recorded so far, in the order they were added. */
    private final List<TelemetryEvent> events = new ArrayList<>();

    /** Creates an empty buffer. */
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
     * Gives a snapshot of the events so far. The returned list is a copy, so the
     * caller can read it while other threads keep adding events.
     *
     * @return a copy of the recorded events, oldest first
     */
    public synchronized List<TelemetryEvent> getEvents() {
        return new ArrayList<>(events);
    }
}
