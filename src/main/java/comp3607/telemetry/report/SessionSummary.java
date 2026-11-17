package comp3607.telemetry.report;

import comp3607.telemetry.TelemetryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds all the totals for one session so they are ready to be written out.
 
 * {@link SessionAggregator#summarise(List, long)} makes one of these and fills it in from the
 * buffer's events, then every {@link Exporter} reads from it. It is only a data holder, so the
 * exporters never have to work anything out themselves.

 * All times are in milliseconds. The start and end are clock times from
 * {@code System.currentTimeMillis()} and the rest are lengths of time.
 * It is not thread safe, but every export makes its own summary so it never gets shared.
 */
public class SessionSummary {

    /** Put in place of the student ID because BlueJ does not know who the student is. */
    public static final String STUDENT_ID_PLACEHOLDER = "STUDENT_ID";

    /** Number of milliseconds in one minute, used for the edit rate. */
    private static final long MILLIS_PER_MINUTE = 60 * 1000;

    // session details

    /** Name of the BlueJ project the session was for, or no_project if none was opened. */
    private String projectName = ReportLocation.NO_PROJECT;

    /** When the session started, which is when the first project opened. */
    private long sessionStart;

    /** When the session ended, which is the export time or when BlueJ closed. */
    private long sessionEnd;

    /** Total of every gap of 2 minutes or more with no edits. */
    private long idleTime;

    // edits

    /**
     * How many EDIT events there were. The editor monitor checks each open class every
     * 5 seconds, so one edit means the class's text changed during that check.
     */
    private int editCount;

    // compiles

    /** Compile attempts, which is successful plus failed compiles. */
    private int totalCompilations;

    /** Compiles that finished with no errors. */
    private int successfulCompilations;

    /** How many times each error code came up, eg E01 to 3. Sorted by code. */
    private Map<String, Integer> errorCounts = new TreeMap<>();

    /** Time between each pair of finished compiles, in the order they happened. */
    private List<Long> compileIntervals = new ArrayList<>();

    // runs

    /** Every run of a method or constructor. */
    private int totalRuns;

    /** Runs that finished normally. */
    private int normalRuns;

    /** Runs that ended with an exception. */
    private int exceptionRuns;

    /** Runs the student stopped before they finished. */
    private int terminatedRuns;

    /** Runs where BlueJ did not give a status. */
    private int unknownRuns;

    // timeline

    /** Class creations and the first time each method was defined, in the order they happened. */
    private List<TelemetryEvent> timeline = new ArrayList<>();

    /** Every event collected in the session, kept so the JSON can list them all. */
    private List<TelemetryEvent> events = new ArrayList<>();

    /**
     * Makes an empty summary with every total at 0 and empty lists.
     * {@link SessionAggregator} fills it in.
     */
    public SessionSummary() {
    }

    /** {@return the student ID placeholder, since the real ID is not known} */
    public String getStudentId() {
        return STUDENT_ID_PLACEHOLDER;
    }

    /** {@return the name of the BlueJ project the session was for, or no_project if none was opened} */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets which BlueJ project the session was for.
     *
     * @param projectName the project's name as BlueJ shows it, eg TelemetryTest
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /** {@return when the session started, as a clock time in milliseconds} */
    public long getSessionStart() {
        return sessionStart;
    }

    /**
     * Sets when the session started.
     *
     * @param sessionStart a clock time in milliseconds
     */
    public void setSessionStart(long sessionStart) {
        this.sessionStart = sessionStart;
    }

    /** {@return when the session ended, as a clock time in milliseconds} */
    public long getSessionEnd() {
        return sessionEnd;
    }

    /**
     * Sets when the session ended.
     *
     * @param sessionEnd a clock time in milliseconds
     */
    public void setSessionEnd(long sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    /** {@return how long the session lasted, end minus start, in milliseconds} */
    public long getSessionDuration() {
        return sessionEnd - sessionStart;
    }

    /** {@return the total idle time in milliseconds} */
    public long getIdleTime() {
        return idleTime;
    }

    /**
     * Sets the total idle time.
     *
     * @param idleTime total of every idle gap, in milliseconds
     */
    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    /** {@return the active coding time, which is the session length minus the idle time, in milliseconds} */
    public long getActiveTime() {
        return getSessionDuration() - idleTime;
    }

    /** {@return how many edits there were in the session} */
    public int getEditCount() {
        return editCount;
    }

    /**
     * Sets how many edits there were.
     *
     * @param editCount the number of EDIT events in the session
     */
    public void setEditCount(int editCount) {
        this.editCount = editCount;
    }

    /**
     * Works out how many edits happened per minute of active time. This is used as the
     * typing cadence because BlueJ's extensions API has no keystroke events.
     * It is worked out each time so it always matches the edit count and active time.
     *
     * @return edits per active minute, or 0 if there was no active time
     */
    public double getEditsPerMinute() {
        long active = getActiveTime();
        if (active <= 0) {
            return 0;
        }
        return editCount * (double) MILLIS_PER_MINUTE / active;
    }

    /** {@return the compile attempts, successful plus failed} */
    public int getTotalCompilations() {
        return totalCompilations;
    }

    /**
     * Sets the number of compile attempts.
     *
     * @param totalCompilations successful plus failed compiles
     */
    public void setTotalCompilations(int totalCompilations) {
        this.totalCompilations = totalCompilations;
    }

    /** {@return the compiles that finished with no errors} */
    public int getSuccessfulCompilations() {
        return successfulCompilations;
    }

    /**
     * Sets the number of compiles that worked.
     *
     * @param successfulCompilations compiles that finished with no errors
     */
    public void setSuccessfulCompilations(int successfulCompilations) {
        this.successfulCompilations = successfulCompilations;
    }

    /**
     * Works out the percentage of compile attempts that worked.
     *
     * @return a value from 0 to 100, or 0 if nothing was compiled so we never divide by 0
     */
    public double getCompileSuccessRate() {
        if (totalCompilations == 0) {
            return 0;
        }
        return successfulCompilations * 100.0 / totalCompilations;
    }

    /** {@return each error code with how many times it came up, sorted by code} */
    public Map<String, Integer> getErrorCounts() {
        return errorCounts;
    }

    /**
     * Adds one to the count for an error code, starting it at 1 the first time it shows up.
     *
     * @param code the error code, eg E01
     */
    public void addError(String code) {
        Integer count = errorCounts.get(code);
        if (count == null) {
            errorCounts.put(code, 1);
        } else {
            errorCounts.put(code, count + 1);
        }
    }

    /** {@return the time between each pair of finished compiles, in milliseconds} */
    public List<Long> getCompileIntervals() {
        return compileIntervals;
    }

    /**
     * Adds the gap since the last finished compile to the end of the list.
     *
     * @param interval time since the last finished compile, in milliseconds
     */
    public void addCompileInterval(long interval) {
        compileIntervals.add(interval);
    }

    /** {@return how many runs there were} */
    public int getTotalRuns() {
        return totalRuns;
    }

    /**
     * Sets how many runs there were.
     *
     * @param totalRuns every run of a method or constructor
     */
    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    /** {@return the runs that finished normally} */
    public int getNormalRuns() {
        return normalRuns;
    }

    /**
     * Sets how many runs finished normally.
     *
     * @param normalRuns runs that finished normally
     */
    public void setNormalRuns(int normalRuns) {
        this.normalRuns = normalRuns;
    }

    /** {@return the runs that ended with an exception} */
    public int getExceptionRuns() {
        return exceptionRuns;
    }

    /**
     * Sets how many runs ended with an exception.
     *
     * @param exceptionRuns runs that ended with an exception
     */
    public void setExceptionRuns(int exceptionRuns) {
        this.exceptionRuns = exceptionRuns;
    }

    /** {@return the runs the student stopped early} */
    public int getTerminatedRuns() {
        return terminatedRuns;
    }

    /**
     * Sets how many runs the student stopped early.
     *
     * @param terminatedRuns runs that were stopped before they finished
     */
    public void setTerminatedRuns(int terminatedRuns) {
        this.terminatedRuns = terminatedRuns;
    }

    /** {@return the runs with no status from BlueJ} */
    public int getUnknownRuns() {
        return unknownRuns;
    }

    /**
     * Sets how many runs had no status.
     *
     * @param unknownRuns runs where BlueJ did not give a status
     */
    public void setUnknownRuns(int unknownRuns) {
        this.unknownRuns = unknownRuns;
    }

    /** {@return the class creations and first method definitions in time order} */
    public List<TelemetryEvent> getTimeline() {
        return timeline;
    }

    /**
     * Adds an event to the end of the timeline.
     *
     * @param event a class creation or a method's first definition
     */
    public void addTimelineEvent(TelemetryEvent event) {
        timeline.add(event);
    }

    /** {@return every event collected in the session} */
    public List<TelemetryEvent> getEvents() {
        return events;
    }

    /**
     * Stores the session's events. A copy is kept so later changes to the given list
     * do not change the summary.
     *
     * @param events every event collected in the session
     */
    public void setEvents(List<TelemetryEvent> events) {
        this.events = new ArrayList<>(events);
    }
}