package comp3607.telemetry.report;

import comp3607.telemetry.EventBuffer;

import java.io.File;
import java.util.List;

// every 60 seconds it gives the runner a json export called autosave
// the same file gets overwritten each time so there is always a recent copy if bluej crashes
public class AutosaveTimer implements Runnable {

    private static final long INTERVAL_MS = 60 * 1000;

    private final EventBuffer buffer;
    private final ExportRunner runner;
    private final List<Exporter> exporters;
    private final File folder;

    public AutosaveTimer(EventBuffer buffer, ExportRunner runner, List<Exporter> exporters, File folder) {
        this.buffer = buffer;
        this.runner = runner;
        this.exporters = exporters;
        this.folder = folder;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
            runner.submit(new ExportCommand(buffer, exporters, folder, "autosave"));
        }
    }
}