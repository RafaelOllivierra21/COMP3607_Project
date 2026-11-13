package comp3607.telemetry.report;

import comp3607.telemetry.ErrorCodes;
import comp3607.telemetry.TelemetryEvent;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// makes the pdf report (max 2 pages) using openpdf
// the parent class decides the order, this class just fills in each part
public class PdfExporter extends AbstractExporter {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADING_FONT = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font TEXT_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);

    // keeps the timeline short enough for the report to stay within 2 pages
    private static final int MAX_TIMELINE_ROWS = 20;

    private Document document;
    private PdfWriter writer;

    @Override
    protected String getExtension() {
        return "pdf";
    }

    @Override
    protected void start(File file) throws IOException {
        document = new Document(PageSize.A4);
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            // keep the chart in its place instead of letting the timeline jump ahead of it
            writer.setStrictImageSequence(true);
        } catch (DocumentException e) {
            throw new IOException("could not start the pdf", e);
        }
        document.open();
        add(new Paragraph("BlueJ Telemetry Report", TITLE_FONT));
    }

    // table 1 from the scope, active vs idle time with the compile success rate
    @Override
    protected void writeSessionInfo(SessionSummary summary) throws IOException {
        add(new Paragraph("Student ID: " + summary.getStudentId(), TEXT_FONT));
        add(new Paragraph("Session: " + formatTime(summary.getSessionStart())
                + " to " + formatTime(summary.getSessionEnd()), TEXT_FONT));

        add(heading("Active vs idle time"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addRow(table, "Session length", formatDuration(summary.getSessionDuration()));
        addRow(table, "Active coding time", formatDuration(summary.getActiveTime()));
        addRow(table, "Idle time", formatDuration(summary.getIdleTime()));
        addRow(table, "Compile attempts", String.valueOf(summary.getTotalCompilations()));
        addRow(table, "Successful compiles", String.valueOf(summary.getSuccessfulCompilations()));
        addRow(table, "Compile success rate", String.format("%.1f", summary.getCompileSuccessRate()) + "%");
        add(table);
    }

    // table 2 and the bar chart from the scope, how many of each error code happened
    @Override
    protected void writeCompileInfo(SessionSummary summary) throws IOException {
        add(heading("Errors by code"));

        if (summary.getErrorCounts().isEmpty()) {
            add(new Paragraph("No compile errors this session.", TEXT_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell(new Phrase("Code", BOLD_FONT));
        table.addCell(new Phrase("Error", BOLD_FONT));
        table.addCell(new Phrase("Count", BOLD_FONT));
        for (String code : summary.getErrorCounts().keySet()) {
            table.addCell(new Phrase(code, TEXT_FONT));
            table.addCell(new Phrase(ErrorCodes.labelFor(code), TEXT_FONT));
            table.addCell(new Phrase(String.valueOf(summary.getErrorCounts().get(code)), TEXT_FONT));
        }
        add(table);

        // the chart goes right under the table it comes from
        add(new Paragraph(" ", TEXT_FONT));
        add(makeErrorChart(summary));
    }

    // table 3 from the scope, class creations and method defs in the order they happened
    // this is a hook in the parent, only the pdf needs it
    @Override
    protected void writeTimeline(SessionSummary summary) throws IOException {
        add(heading("Class and method timeline"));

        if (summary.getTimeline().isEmpty()) {
            add(new Paragraph("No classes or methods were created this session.", TEXT_FONT));
            return;
        }

        // name column gets half the width so long names dont wrap onto 2 lines
        PdfPTable table = new PdfPTable(new float[] {1, 1, 2});
        table.setWidthPercentage(100);
        table.addCell(new Phrase("Time", BOLD_FONT));
        table.addCell(new Phrase("Event", BOLD_FONT));
        table.addCell(new Phrase("Name", BOLD_FONT));

        // only show the first few rows so we dont go past 2 pages
        int rows = summary.getTimeline().size();
        if (rows > MAX_TIMELINE_ROWS) {
            rows = MAX_TIMELINE_ROWS;
        }

        for (int i = 0; i < rows; i++) {
            TelemetryEvent event = summary.getTimeline().get(i);
            table.addCell(new Phrase(formatClock(event.getTimestamp()), TEXT_FONT));
            table.addCell(new Phrase(describe(event), TEXT_FONT));
            table.addCell(new Phrase(event.getDetail(), TEXT_FONT));
        }
        add(table);

        int left = summary.getTimeline().size() - rows;
        if (left > 0) {
            add(new Paragraph("... and " + left + " more, see the JSON file for all of them.", TEXT_FONT));
        }
    }

    @Override
    protected void finish() {
        document.close();
    }

    // small helpers

    // one bar per error code, the longest bar is the most common error
    private Image makeErrorChart(SessionSummary summary) throws IOException {
        float rowHeight = 15;
        float width = 500;
        float height = summary.getErrorCounts().size() * rowHeight + 10;
        float labelWidth = 50;
        float maxBarWidth = width - labelWidth - 40;

        // find the biggest count so the bars can be scaled to fit
        int max = 0;
        for (String code : summary.getErrorCounts().keySet()) {
            int count = summary.getErrorCounts().get(code);
            if (count > max) {
                max = count;
            }
        }

        PdfTemplate chart = writer.getDirectContent().createTemplate(width, height);

        // pdf y starts at the bottom so the first bar goes at the top
        float y = height - rowHeight;
        for (String code : summary.getErrorCounts().keySet()) {
            int count = summary.getErrorCounts().get(code);
            float barWidth = maxBarWidth * count / max;

            // error code on the left
            ColumnText.showTextAligned(chart, Element.ALIGN_LEFT, new Phrase(code, TEXT_FONT), 0, y + 2, 0);

            // the bar in blue, then back to black for the text
            chart.setRGBColorFill(70, 110, 170);
            chart.rectangle(labelWidth, y, barWidth, rowHeight - 4);
            chart.fill();
            chart.setRGBColorFill(0, 0, 0);

            // the count just after the bar
            ColumnText.showTextAligned(chart, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(count), TEXT_FONT), labelWidth + barWidth + 5, y + 2, 0);

            y = y - rowHeight;
        }

        try {
            return Image.getInstance(chart);
        } catch (DocumentException e) {
            throw new IOException("could not make the error chart", e);
        }
    }

    // openpdf can throw its own DocumentException so this turns it into an IOException like the parent expects
    private void add(Element element) throws IOException {
        try {
            document.add(element);
        } catch (DocumentException e) {
            throw new IOException("could not add to the pdf", e);
        }
    }

    private Paragraph heading(String text) {
        Paragraph heading = new Paragraph(text, HEADING_FONT);
        heading.setSpacingBefore(14);
        heading.setSpacingAfter(6);
        return heading;
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, BOLD_FONT));
        table.addCell(new Phrase(value, TEXT_FONT));
    }

    private String describe(TelemetryEvent event) {
        if (TelemetryEvent.CLASS_CREATE.equals(event.getType())) {
            return "Class created";
        }
        return "Method defined";
    }

    // eg 5 min 30 s
    private String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes + " min " + seconds + " s";
    }

    // eg 12 Nov 2026 7:15 PM
    private String formatTime(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy h:mm a");
        return format.format(new Date(millis));
    }

    // eg 7:15:42 PM, the date is already at the top of the report
    private String formatClock(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm:ss a");
        return format.format(new Date(millis));
    }
}