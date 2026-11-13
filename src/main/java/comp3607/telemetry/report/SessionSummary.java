package comp3607.telemetry.report;

import comp3607.telemetry.TelemetryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// stores all the totals for one session, the aggregator fills this in and the exporters read it
public class SessionSummary {

    public static final String STUDENT_ID_PLACEHOLDER = "STUDENT_ID";

    // session times
    private long sessionStart;
    private long sessionEnd;
    private long idleTime;

    // compiles
    private int totalCompilations;
    private int successfulCompilations;
    private Map<String, Integer> errorCounts = new TreeMap<>();
    private List<Long> compileIntervals = new ArrayList<>();

    // runs
    private int totalRuns;
    private int normalRuns;
    private int exceptionRuns;
    private int terminatedRuns;
    private int unknownRuns;

    // class creations and method defs for the timeline
    private List<TelemetryEvent> timeline = new ArrayList<>();

    // every event collected in the session
    private List<TelemetryEvent> events = new ArrayList<>();

    public String getStudentId() {
        return STUDENT_ID_PLACEHOLDER;
    }

    public long getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(long sessionStart) {
        this.sessionStart = sessionStart;
    }

    public long getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(long sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    public long getSessionDuration() {
        return sessionEnd - sessionStart;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public long getActiveTime() {
        return getSessionDuration() - idleTime;
    }

    public int getTotalCompilations() {
        return totalCompilations;
    }

    public void setTotalCompilations(int totalCompilations) {
        this.totalCompilations = totalCompilations;
    }

    public int getSuccessfulCompilations() {
        return successfulCompilations;
    }

    public void setSuccessfulCompilations(int successfulCompilations) {
        this.successfulCompilations = successfulCompilations;
    }

    // returns 0 if nothing was compiled so we dont divide by 0
    public double getCompileSuccessRate() {
        if (totalCompilations == 0) {
            return 0;
        }
        return successfulCompilations * 100.0 / totalCompilations;
    }

    public Map<String, Integer> getErrorCounts() {
        return errorCounts;
    }

    // adds one to the count for this error code
    public void addError(String code) {
        Integer count = errorCounts.get(code);
        if (count == null) {
            errorCounts.put(code, 1);
        } else {
            errorCounts.put(code, count + 1);
        }
    }

    public List<Long> getCompileIntervals() {
        return compileIntervals;
    }

    public void addCompileInterval(long interval) {
        compileIntervals.add(interval);
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getNormalRuns() {
        return normalRuns;
    }

    public void setNormalRuns(int normalRuns) {
        this.normalRuns = normalRuns;
    }

    public int getExceptionRuns() {
        return exceptionRuns;
    }

    public void setExceptionRuns(int exceptionRuns) {
        this.exceptionRuns = exceptionRuns;
    }

    public int getTerminatedRuns() {
        return terminatedRuns;
    }

    public void setTerminatedRuns(int terminatedRuns) {
        this.terminatedRuns = terminatedRuns;
    }

    public int getUnknownRuns() {
        return unknownRuns;
    }

    public void setUnknownRuns(int unknownRuns) {
        this.unknownRuns = unknownRuns;
    }

    public List<TelemetryEvent> getTimeline() {
        return timeline;
    }

    public void addTimelineEvent(TelemetryEvent event) {
        timeline.add(event);
    }

    public List<TelemetryEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TelemetryEvent> events) {
        this.events = new ArrayList<>(events);
    }
}