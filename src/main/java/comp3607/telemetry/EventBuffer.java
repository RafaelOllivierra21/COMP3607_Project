package comp3607.telemetry;

import java.util.ArrayList;
import java.util.List;

public class EventBuffer {

    private final List<TelemetryEvent> events = new ArrayList<>();

    public synchronized void add(String type, String detail) {
        events.add(new TelemetryEvent(type, detail, System.currentTimeMillis()));
    }

    public synchronized List<TelemetryEvent> getEvents() {
        return new ArrayList<>(events);
    }
}
