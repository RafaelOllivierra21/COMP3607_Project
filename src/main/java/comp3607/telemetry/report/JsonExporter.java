package comp3607.telemetry.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import comp3607.telemetry.ErrorCodes;
import comp3607.telemetry.TelemetryEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Writes a session out as a pretty printed JSON file using Gson.

 * The top level keys come out in this order: student_id, session_start, session_end,
 * session_duration, active_time, idle_time, edit_count, edits_per_minute, total_compilations,
 * successful_compilations, error_counts, compile_intervals, run_summary and events.
 * Times are in milliseconds.
 
 * Not thread safe. It keeps the file and JSON object between steps, so only one export
 * should use it at a time. The single export thread in {@link ExportRunner} makes sure of that.
 */

public class JsonExporter extends AbstractExporter {

    /** File being written, set in {@link #start(File)}. */
    private File file;

    /** JSON object built up bit by bit as each part runs, then saved in {@link #finish()}. */
    private JsonObject root;

    /** @return {@code json} */
 
    /** Makes a JSON exporter. One is made in {@code TelemetryExtension.startup()} and reused for every export. */
    public JsonExporter() {
    }
    
    @Override
    protected String getExtension() {
        return "json";
    }

    /**
     * Remembers the file and starts a new empty JSON object. Nothing is written to disk yet.
     *
     * @param file the file that {@link #finish()} will write to
     */
    @Override
    protected void start(File file) {
        this.file = file;
        root = new JsonObject();
    }

    /**
     * Adds the student ID placeholder, the session times, active and idle time,
     * and the edit count and edits per active minute.
     *
     * @param summary the session totals
     */
    @Override
    protected void writeSessionInfo(SessionSummary summary) {
        root.addProperty("student_id", summary.getStudentId());
        root.addProperty("session_start", summary.getSessionStart());
        root.addProperty("session_end", summary.getSessionEnd());
        root.addProperty("session_duration", summary.getSessionDuration());
        root.addProperty("active_time", summary.getActiveTime());
        root.addProperty("idle_time", summary.getIdleTime());
        root.addProperty("edit_count", summary.getEditCount());
        root.addProperty("edits_per_minute", roundTwoPlaces(summary.getEditsPerMinute()));
    }

    /**
     * Adds the compile attempts, successful compiles, how many times each error code came up,
     * and the time between finished compiles.
     *
     * @param summary the session totals
     */
    @Override
    protected void writeCompileInfo(SessionSummary summary) {
        root.addProperty("total_compilations", summary.getTotalCompilations());
        root.addProperty("successful_compilations", summary.getSuccessfulCompilations());

        // eg "E01": 3
        JsonObject errors = new JsonObject();
        for (String code : summary.getErrorCounts().keySet()) {
            errors.addProperty(code, summary.getErrorCounts().get(code));
        }
        root.add("error_counts", errors);

        JsonArray intervals = new JsonArray();
        for (Long interval : summary.getCompileIntervals()) {
            intervals.add(interval);
        }
        root.add("compile_intervals", intervals);
    }

    /**
     * Adds run_summary, which is the console output summary: how many runs there were
     * and how many ended normally, with an exception, terminated or unknown.
     *
     * @param summary the session totals
     */
    @Override
    protected void writeRunInfo(SessionSummary summary) {
        JsonObject runs = new JsonObject();
        runs.addProperty("total", summary.getTotalRuns());
        runs.addProperty("normal", summary.getNormalRuns());
        runs.addProperty("exception", summary.getExceptionRuns());
        runs.addProperty("terminated", summary.getTerminatedRuns());
        runs.addProperty("unknown", summary.getUnknownRuns());
        root.add("run_summary", runs);
    }

    /**
     * Adds every event in the session to the events list, with compile errors, runs and
     * renames split into their own fields.
     *
     * @param summary the session totals
     */
    @Override
    protected void writeEvents(SessionSummary summary) {
        JsonArray list = new JsonArray();
        for (TelemetryEvent event : summary.getEvents()) {
            list.add(eventToJson(event));
        }
        root.add("events", list);
    }

    /**
     * Writes the finished JSON to the file in UTF-8, replacing the file if it is already there.
     *
     * @throws IOException if the file cannot be written
     */
    @Override
    protected void finish() throws IOException {
        // pretty printing so its easy to read, html escaping off so < and > stay as they are
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String text = gson.toJson(root);
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
    }

    // turns one event into json and splits the detail into proper fields where we can
    private JsonObject eventToJson(TelemetryEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("timestamp", event.getTimestamp());
        json.addProperty("type", event.getType());

        String type = event.getType();
        String detail = event.getDetail();

        if (TelemetryEvent.COMPILE_ERROR.equals(type)) {
            String code = DetailParser.errorCode(detail);
            json.addProperty("error_code", code);
            json.addProperty("error_label", ErrorCodes.labelFor(code));
            json.addProperty("line_number", DetailParser.errorLine(detail));
            json.addProperty("message", DetailParser.errorMessage(detail));
        } else if (TelemetryEvent.RUN.equals(type)) {
            json.addProperty("target", DetailParser.runTarget(detail));
            json.addProperty("exit_status", DetailParser.runStatus(detail));
            json.addProperty("result", DetailParser.runResult(detail));
        } else if (TelemetryEvent.CLASS_RENAME.equals(type)) {
            json.addProperty("old_name", DetailParser.oldName(detail));
            json.addProperty("new_name", DetailParser.newName(detail));
        } else {
            // project, class, edit, idle, compile ok/failed and method events just keep the detail
            json.addProperty("detail", detail);
        }
        return json;
    }

    // eg 5.454545 becomes 5.45 so the json stays easy to read
    private double roundTwoPlaces(double value) {
        return Math.round(value * 100) / 100.0;
    }
}