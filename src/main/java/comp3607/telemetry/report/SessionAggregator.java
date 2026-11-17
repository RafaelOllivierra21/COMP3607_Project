package comp3607.telemetry.report;

import comp3607.telemetry.TelemetryEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Works out all the session totals from the list of events in the buffer.
 */
public class SessionAggregator {

    /** A gap with no edits this long or longer counts as idle, 2 minutes in milliseconds. */
    public static final long IDLE_LIMIT = 2 * 60 * 1000;

    /**
     * Builds a summary of the session from its events. The list is only read, never changed.
     *
     * @param events the session's events in the order they happened
     * @param endTime when the session ends, which is the export time or when BlueJ closes
     * @return a new summary with every total filled in
     */
    public SessionSummary summarise(List<TelemetryEvent> events, long endTime) {
        SessionSummary summary = new SessionSummary();
        summary.setEvents(events);

        long start = findStart(events, endTime);
        summary.setSessionStart(start);
        summary.setSessionEnd(endTime);
        summary.setIdleTime(findIdleTime(events, start, endTime));
        summary.setEditCount(countEdits(events, start));

        countCompiles(events, summary);
        countRuns(events, summary);
        buildTimeline(events, summary);

        return summary;
    }

    /**
     * Finds when the session started. Uses the first PROJECT_OPEN, or the first event if no
     * project was opened, or the end time if there are no events at all.
     */
    private long findStart(List<TelemetryEvent> events, long endTime) {
        for (TelemetryEvent event : events) {
            if (TelemetryEvent.PROJECT_OPEN.equals(event.getType())) {
                return event.getTimestamp();
            }
        }
        if (events.isEmpty()) {
            return endTime;
        }
        return events.get(0).getTimestamp();
    }

    /**
     * Adds up every gap of 2 minutes or more between edits. The session start and end are
     * treated like edits so the time before the first edit and after the last one gets checked too.
     */
    private long findIdleTime(List<TelemetryEvent> events, long start, long end) {
        long idle = 0;
        long lastEdit = start;

        for (TelemetryEvent event : events) {
            if (TelemetryEvent.EDIT.equals(event.getType()) && event.getTimestamp() >= start) {
                long gap = event.getTimestamp() - lastEdit;
                if (gap >= IDLE_LIMIT) {
                    idle = idle + gap;
                }
                lastEdit = event.getTimestamp();
            }
        }

        long lastGap = end - lastEdit;
        if (lastGap >= IDLE_LIMIT) {
            idle = idle + lastGap;
        }
        return idle;
    }

    /**
     * Counts the EDIT events from the session start onwards. It uses the same start as
     * the idle time so the edit count and the idle time always cover the same stretch.
     */
    private int countEdits(List<TelemetryEvent> events, long start) {
        int edits = 0;
        for (TelemetryEvent event : events) {
            if (TelemetryEvent.EDIT.equals(event.getType()) && event.getTimestamp() >= start) {
                edits++;
            }
        }
        return edits;
    }

    /**
     * Counts compile attempts and successes, the gaps between finished compiles, and each
     * error code. COMPILE_ERROR fires once per error, so it only goes into the error counts.
     */
    private void countCompiles(List<TelemetryEvent> events, SessionSummary summary) {
        int total = 0;
        int successful = 0;
        long lastCompile = -1;

        for (TelemetryEvent event : events) {
            String type = event.getType();

            if (TelemetryEvent.COMPILE_OK.equals(type) || TelemetryEvent.COMPILE_FAILED.equals(type)) {
                total++;
                if (TelemetryEvent.COMPILE_OK.equals(type)) {
                    successful++;
                }
                // gap since the last compile, skipped for the very first one
                if (lastCompile != -1) {
                    summary.addCompileInterval(event.getTimestamp() - lastCompile);
                }
                lastCompile = event.getTimestamp();

            } else if (TelemetryEvent.COMPILE_ERROR.equals(type)) {
                summary.addError(DetailParser.errorCode(event.getDetail()));
            }
        }

        summary.setTotalCompilations(total);
        summary.setSuccessfulCompilations(successful);
    }

    /** Counts the runs by how they ended: normal, exception, terminated or unknown. */
    private void countRuns(List<TelemetryEvent> events, SessionSummary summary) {
        int total = 0;
        int normal = 0;
        int exception = 0;
        int terminated = 0;
        int unknown = 0;

        for (TelemetryEvent event : events) {
            if (TelemetryEvent.RUN.equals(event.getType())) {
                total++;
                String status = DetailParser.runStatus(event.getDetail());
                if (status.equals("normal")) {
                    normal++;
                } else if (status.equals("exception")) {
                    exception++;
                } else if (status.equals("terminated")) {
                    terminated++;
                } else {
                    unknown++;
                }
            }
        }

        summary.setTotalRuns(total);
        summary.setNormalRuns(normal);
        summary.setExceptionRuns(exception);
        summary.setTerminatedRuns(terminated);
        summary.setUnknownRuns(unknown);
    }

    /**
     * Puts class creations and method definitions into the timeline in the order they happened.
     * The class monitor reports every method again after each compile, so a method only goes
     * in the first time it shows up.
     */
    private void buildTimeline(List<TelemetryEvent> events, SessionSummary summary) {
        Set<String> methodsSeen = new HashSet<>();

        for (TelemetryEvent event : events) {
            String type = event.getType();
            if (TelemetryEvent.CLASS_CREATE.equals(type)) {
                summary.addTimelineEvent(event);
            } else if (TelemetryEvent.METHOD_DEFINED.equals(type)) {
                if (!methodsSeen.contains(event.getDetail())) {
                    methodsSeen.add(event.getDetail());
                    summary.addTimelineEvent(event);
                }
            }
        }
    }
}