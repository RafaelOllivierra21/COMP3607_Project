package comp3607.telemetry.report;

import java.io.File;
import java.io.IOException;

/**
 * The Strategy pattern interface: one way of saving a session's report to a file.

 * {@link JsonExporter} and {@link PdfExporter} are the two strategies. {@link ExportCommand}
 * only knows this interface, so it saves every format the same way, and a new format
 * (eg CSV) can be added by writing a new class without changing the command.

 * Both current strategies extend {@link AbstractExporter}, which fixes the order the
 * parts of a report are written in.
 */
public interface Exporter {

    /**
     * Writes the report for a session into a folder. The folder is made if it does not exist,
     * and a file with the same name is replaced.
     *
     * @param summary the session totals to write, which are only read and never changed
     * @param folder where to save the file
     * @param name the file name without an extension, eg TelemetryTest_final_2026-11-17_18-35-09,
     *             each exporter adds its own extension
     * @return the file that was written
     * @throws IOException if the folder or file cannot be written
     */
    File export(SessionSummary summary, File folder, String name) throws IOException;
}