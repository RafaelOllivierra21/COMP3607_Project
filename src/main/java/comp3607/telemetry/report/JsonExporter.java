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

// writes the session out as a json file using gson
// the parent class decides the order, this class just fills in each part
public class JsonExporter extends AbstractExporter {

    // built up bit by bit as each part runs, then saved in finish()
    private File file;
    private JsonObject root;

    @Override
    protected String getExtension() {
        return "json";
    }

    @Override
    protected void start(File file) {
        this.file = file;
        root = new JsonObject();
    }

    @Override
    protected void writeSessionInfo(SessionSummary summary) {
        root.addProperty("student_id", summary.getStudentId());
        root.addProperty("session_start", summary.getSessionStart());
        root.addProperty("session_end", summary.getSessionEnd());
        root.addProperty("session_duration", summary.getSessionDuration());
        root.addProperty("active_time", summary.getActiveTime());
        root.addProperty("idle_time", summary.getIdleTime());
    }

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

    // the console output summary, built from the run results
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

    @Override
    protected void writeEvents(SessionSummary summary) {
        JsonArray list = new JsonArray();
        for (TelemetryEvent event : summary.getEvents()) {
            list.add(eventToJson(event));
        }
        root.add("events", list);
    }

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
}