package comp3607.telemetry;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EventBufferTest {

    @Test
    void testAddAndRetrieveEvents() {
        EventBuffer buffer = new EventBuffer();
        buffer.add(TelemetryEvent.PROJECT_OPEN, "Lab1");
        buffer.add(TelemetryEvent.CLASS_CREATE, "Student");

        List<TelemetryEvent> events = buffer.getEvents();
        assertEquals(2, events.size());
        assertEquals(TelemetryEvent.PROJECT_OPEN, events.get(0).getType());
        assertEquals("Lab1", events.get(0).getDetail());
        assertEquals(TelemetryEvent.CLASS_CREATE, events.get(1).getType());
        assertEquals("Student", events.get(1).getDetail());
        assertTrue(events.get(0).getTimestamp() > 0);
    }

    @Test
    void testGetEventsReturnsDefensiveCopy() {
        EventBuffer buffer = new EventBuffer();
        buffer.add(TelemetryEvent.EDIT, "Main");

        List<TelemetryEvent> copy = buffer.getEvents();
        copy.clear();

        assertEquals(1, buffer.getEvents().size());
    }
}