package comp3607.telemetry.report;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import comp3607.telemetry.TelemetryEvent;

public class SessionAggregatorTest {

    @Test
    void testSessionCalculationsAndIdleTime() {
        long start = 1_000_000L;
        long threeMinutesLater = start + 180_000L;
        long end = threeMinutesLater + 30_000L; 

        List<TelemetryEvent> events = List.of(
            new TelemetryEvent(TelemetryEvent.PROJECT_OPEN, "ProjectA", start),
            new TelemetryEvent(TelemetryEvent.EDIT, "Main", threeMinutesLater),
            new TelemetryEvent(TelemetryEvent.COMPILE_FAILED, "Main.java", threeMinutesLater + 5_000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_ERROR, "E02|10|';' expected", threeMinutesLater + 5_000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_OK, "Main.java", threeMinutesLater + 25_000L),
            new TelemetryEvent(TelemetryEvent.RUN, "Main.main | normal | done", threeMinutesLater + 28_000L),
            new TelemetryEvent(TelemetryEvent.RUN, "Main.main | exception | NPE", threeMinutesLater + 29_000L)
        );

        SessionAggregator aggregator = new SessionAggregator();
        SessionSummary summary = aggregator.summarise(events, end);

        assertEquals(start, summary.getSessionStart());
        assertEquals(end, summary.getSessionEnd());
        assertEquals(180_000L, summary.getIdleTime());
        assertEquals(2, summary.getTotalCompilations());
        assertEquals(1, summary.getSuccessfulCompilations());
        assertEquals(1, summary.getErrorCounts().get("E02"));
        assertEquals(List.of(20_000L), summary.getCompileIntervals());
        assertEquals(2, summary.getTotalRuns());
        assertEquals(1, summary.getNormalRuns());
        assertEquals(1, summary.getExceptionRuns());
    }

    @Test
    void testTimelineDeduplicatesRepeatedMethods() {
        List<TelemetryEvent> events = List.of(
            new TelemetryEvent(TelemetryEvent.CLASS_CREATE, "Calculator", 1000L),
            new TelemetryEvent(TelemetryEvent.METHOD_DEFINED, "Calculator.add", 2000L),
            new TelemetryEvent(TelemetryEvent.METHOD_DEFINED, "Calculator.add", 3000L),
            new TelemetryEvent(TelemetryEvent.METHOD_DEFINED, "Calculator.subtract", 4000L)
        );

        SessionSummary summary = new SessionAggregator().summarise(events, 5000L);
        assertEquals(3, summary.getTimeline().size());
    }
}