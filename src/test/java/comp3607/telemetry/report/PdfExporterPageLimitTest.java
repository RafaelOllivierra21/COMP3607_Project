package comp3607.telemetry.report;

import comp3607.telemetry.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.pdf.PdfReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the PDF report stays within 2 pages even for the busiest session we planned for:
 * every error code, runs of every kind, and more timeline entries than the PDF shows.
 */
public class PdfExporterPageLimitTest {

    @Test
    void testWorstCaseReportFitsOnTwoPages(@TempDir Path tempDir) throws Exception {
        List<TelemetryEvent> events = new ArrayList<>();
        long time = 1000;
        events.add(new TelemetryEvent(TelemetryEvent.PROJECT_OPEN, "WorstCaseProject", time));

        // every error code E01 to E18 plus E99, so the errors table and chart are as long as they get
        for (int i = 1; i <= 18; i++) {
            time = time + 1000;
            String code = String.format("E%02d", i);
            events.add(new TelemetryEvent(TelemetryEvent.COMPILE_FAILED, "Big.java", time));
            events.add(new TelemetryEvent(TelemetryEvent.COMPILE_ERROR, code + "|" + i + "|some error", time));
        }
        time = time + 1000;
        events.add(new TelemetryEvent(TelemetryEvent.COMPILE_ERROR, "E99|99|something else", time));

        // one run of every kind so the runs table gets drawn
        time = time + 1000;
        events.add(new TelemetryEvent(TelemetryEvent.RUN, "Big.go | normal | 1", time));
        events.add(new TelemetryEvent(TelemetryEvent.RUN, "Big.go | exception | ", time));
        events.add(new TelemetryEvent(TelemetryEvent.RUN, "Big.go | terminated | ", time));
        events.add(new TelemetryEvent(TelemetryEvent.RUN, "Big.go | unknown | ", time));

        // 30 classes, more than the timeline shows, so the "and N more" note gets drawn too
        for (int i = 1; i <= 30; i++) {
            time = time + 1000;
            events.add(new TelemetryEvent(TelemetryEvent.CLASS_CREATE, "ClassNumber" + i, time));
        }

        SessionSummary summary = new SessionAggregator().summarise(events, time + 1000);
        File pdf = new PdfExporter().export(summary, tempDir.toFile(), "worst_case");

        // read the pdf as bytes so the file is not kept open and windows can delete the temp folder after
        byte[] bytes = Files.readAllBytes(pdf.toPath());
        PdfReader reader = new PdfReader(bytes);
        int pages = reader.getNumberOfPages();
        reader.close();

        assertTrue(pages <= 2, "the report should fit on 2 pages but it took " + pages);
    }
}