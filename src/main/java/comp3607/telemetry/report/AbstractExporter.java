package comp3607.telemetry.report;

import java.io.File;
import java.io.IOException;

// writing a report always goes in the same order, only how each part gets written changes
// the order is fixed in export() and the subclasses fill in the parts
public abstract class AbstractExporter implements Exporter {

    // the template method
    @Override
    public final File export(SessionSummary summary, File folder, String name) throws IOException {
        folder.mkdirs();
        File file = new File(folder, name + "." + getExtension());

        start(file);
        writeSessionInfo(summary);
        writeCompileInfo(summary);
        writeRunInfo(summary);
        writeTimeline(summary);
        writeEvents(summary);
        finish();

        return file;
    }

    // abstract parts, every exporter must write these
    protected abstract String getExtension();

    protected abstract void start(File file) throws IOException;

    protected abstract void writeSessionInfo(SessionSummary summary) throws IOException;

    protected abstract void writeCompileInfo(SessionSummary summary) throws IOException;

    protected abstract void finish() throws IOException;

    // hooks, they do nothing unless a subclass needs that part
    protected void writeRunInfo(SessionSummary summary) throws IOException {
    }

    protected void writeTimeline(SessionSummary summary) throws IOException {
    }

    protected void writeEvents(SessionSummary summary) throws IOException {
    }
}