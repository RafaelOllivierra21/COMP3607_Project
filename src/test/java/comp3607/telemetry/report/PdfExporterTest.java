package comp3607.telemetry.report;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import comp3607.telemetry.TelemetryEvent;

public class PdfExporterTest {

    @Test
    void testExportGeneratesNonEmptyPdf(@TempDir Path tempDir) throws Exception {
        List<TelemetryEvent> events = List.of(
            new TelemetryEvent(TelemetryEvent.PROJECT_OPEN, "LabProject", 1000L),
            new TelemetryEvent(TelemetryEvent.CLASS_CREATE, "Student", 2000L),
            new TelemetryEvent(TelemetryEvent.METHOD_DEFINED, "Student.getName", 3000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_FAILED, "Student.java", 4000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_ERROR, "E02|5|';' expected", 4000L),
            new TelemetryEvent(TelemetryEvent.COMPILE_OK, "Student.java", 5000L),
            new TelemetryEvent(TelemetryEvent.RUN, "Student.getName | normal | Alice", 6000L)
        );

        SessionSummary summary = new SessionAggregator().summarise(events, 7000L);
        PdfExporter exporter = new PdfExporter();
        File pdfFile = exporter.export(summary, tempDir.toFile(), "session_report");

        assertTrue(pdfFile.exists());
        assertEquals("session_report.pdf", pdfFile.getName());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testExportHandlesEmptySession(@TempDir Path tempDir) throws Exception {
        SessionSummary emptySummary = new SessionAggregator().summarise(List.of(), 5000L);
        PdfExporter exporter = new PdfExporter();
        File pdfFile = exporter.export(emptySummary, tempDir.toFile(), "empty_report");

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}