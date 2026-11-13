package comp3607.telemetry.report;

import comp3607.telemetry.EventBuffer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// one export job, it takes a snapshot of the buffer, works out the totals
// and writes every file in its list of exporters
public class ExportCommand implements Command {

    private final EventBuffer buffer;
    private final List<Exporter> exporters;
    private final File folder;
    private final String name;
    private final SessionAggregator aggregator = new SessionAggregator();

    public ExportCommand(EventBuffer buffer, List<Exporter> exporters, File folder, String name) {
        this.buffer = buffer;
        this.exporters = exporters;
        this.folder = folder;
        this.name = name;
    }

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

    // makes names like snapshot_2026-11-12_23-40-05 so files never overwrite each other
    public static String nameWithTime(String prefix) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return prefix + "_" + format.format(new Date());
    }
}
