package comp3607.telemetry.report;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import comp3607.telemetry.TelemetryEvent;

public class JsonExporterTest {

    @Test
    void testExportProducesValidJsonSchema(@TempDir Path tempDir) throws Exception {
        List<TelemetryEvent> events = List.of(
            new TelemetryEvent(TelemetryEvent.PROJECT_OPEN, "DemoProject", 1000L),
            new TelemetryEvent(TelemetryEvent.CLASS_RENAME, "OldClass -> NewClass", 2000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_ERROR, "E02|8|';' expected", 3000L),
            new TelemetryEvent(TelemetryEvent.RUN, "NewClass.test | normal | 42", 4000L)
        );

        SessionSummary summary = new SessionAggregator().summarise(events, 5000L);

        JsonExporter exporter = new JsonExporter();
        File outputFile = exporter.export(summary, tempDir.toFile(), "telemetry_output");

        assertTrue(outputFile.exists());
        assertEquals("telemetry_output.json", outputFile.getName());

        // Parse back using Gson to verify valid JSON structure and values
        String rawJson = Files.readString(outputFile.toPath());
        JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();

        assertTrue(root.has("student_id"));
        assertEquals(4000L, root.get("session_duration").getAsLong());
        assertEquals(4, root.getAsJsonArray("events").size());
        assertEquals("E02", root.getAsJsonArray("events").get(2).getAsJsonObject().get("error_code").getAsString());
        assertEquals("normal", root.getAsJsonArray("events").get(3).getAsJsonObject().get("exit_status").getAsString());
    }
}