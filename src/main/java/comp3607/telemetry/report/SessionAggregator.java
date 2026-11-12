package comp3607.telemetry.report;

import comp3607.telemetry.TelemetryEvent;

import java.util.List;

// works out all the session totals from the list of events in the buffer
public class SessionAggregator {

    // 2 minutes with no edits counts as idle
    public static final long IDLE_LIMIT = 2 * 60 * 1000;

    // TODO change to TelemetryEvent.METHOD_DEFINED once Jon-Paul pushes it
    private static final String METHOD_DEFINED = "METHOD_DEFINED";

    // endTime is when the export happens, or when BlueJ closes for the final report
    public SessionSummary summarise(List<TelemetryEvent> events, long endTime) {
        SessionSummary summary = new SessionSummary();
        summary.setEvents(events);

        long start = findStart(events, endTime);
        summary.setSessionStart(start);
        summary.setSessionEnd(endTime);
        summary.setIdleTime(findIdleTime(events, start, endTime));

        countCompiles(events, summary);
        countRuns(events, summary);
        buildTimeline(events, summary);

        return summary;
    }

    // the session starts when the first project opens
    private long findStart(List<TelemetryEvent> events, long endTime) {
        for (TelemetryEvent event : events) {
            if (TelemetryEvent.PROJECT_OPEN.equals(event.getType())) {
                return event.getTimestamp();
            }
        }
        // no project opened so fall back to the first event, or the end time if there are none
        if (events.isEmpty()) {
            return endTime;
        }
        return events.get(0).getTimestamp();
    }

    // any gap of 2 mins or more between edits counts as idle, the whole gap counts
    // the session start and end are treated like edits so the edges get checked too
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

    // attempts are COMPILE_OK plus COMPILE_FAILED
    // COMPILE_ERROR fires once per error so it only goes into the error counts
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

    // class creations and method defs go in the timeline in the order they happened
    private void buildTimeline(List<TelemetryEvent> events, SessionSummary summary) {
        for (TelemetryEvent event : events) {
            String type = event.getType();
            if (TelemetryEvent.CLASS_CREATE.equals(type) || METHOD_DEFINED.equals(type)) {
                summary.addTimelineEvent(event);
            }
        }
    }
}