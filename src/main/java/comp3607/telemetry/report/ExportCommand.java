package comp3607.telemetry.report;

import comp3607.telemetry.EventBuffer;

import java.io.File;
import java.util.List;

/**
 * One export job in the Command pattern. When it runs it copies the events recorded so far,
 * works out the session totals with {@link SessionAggregator}, and writes one file per
 * {@link Exporter} into the given folder.
 
 * The command only uses the {@link Exporter} interface, so it works the same way for JSON,
 * PDF or any new format added later (Strategy).
 */

public class ExportCommand implements Command {

    /** Buffer the events are copied from when the command runs. */
    private final EventBuffer buffer;

    /** Exporters to run, each one writes its own file. */
    private final List<Exporter> exporters;

    /** Folder the files are written to. */
    private final File folder;

    /** File name without the extension, each exporter adds its own. */
    private final String name;

    /** Works out the session totals from the copied events. */
    private final SessionAggregator aggregator = new SessionAggregator();

    /**
     * Makes an export job. Nothing is read or written until {@link #execute()} is called.
     *
     * @param buffer the shared buffer of recorded events
     * @param exporters the exporters to run, eg JSON and PDF
     * @param folder where to write the files
     * @param name the file name without an extension, eg TelemetryTest_snapshot_2026-11-17_18-10-05
     */
    public ExportCommand(EventBuffer buffer, List<Exporter> exporters, File folder, String name) {
        this.buffer = buffer;
        this.exporters = exporters;
        this.folder = folder;
        this.name = name;
    }

    /**
     * Copies the events, works out the totals with the session end set to now, and writes
     * every file. If one exporter fails, the error is printed to the BlueJ log and the rest
     * still run, so one bad file never stops the others or kills the export thread.
     */
    @Override
    public void execute() {
        // getEvents gives a copy so new events can keep coming in while we export
        SessionSummary summary = aggregator.summarise(buffer.getEvents(), System.currentTimeMillis());

        for (Exporter exporter : exporters) {
            try {
                File file = exporter.export(summary, folder, name);
                System.out.println("[Telemetry] saved " + file.getPath());
            } catch (Exception e) {
                // one bad export shouldnt stop the others or crash the export thread
                System.out.println("[Telemetry] export failed: " + e);
            }
        }
    }
}