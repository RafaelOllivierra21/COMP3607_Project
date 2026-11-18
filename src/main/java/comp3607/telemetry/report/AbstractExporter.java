package comp3607.telemetry.report;

import java.io.File;
import java.io.IOException;

/**
 * The Template Method pattern base class for every report exporter.

 * Writing a report always happens in the same order, only how each part gets written changes.
 * {@link #export} is the template method. It is final, so the order is fixed in one place and a
 * subclass cannot change it. Subclasses only fill in the steps, and the base class calls them,
 * not the other way round (the Hollywood principle, "don't call us, we'll call you").
 
 * Required steps are abstract, so every exporter must write them. Hooks do nothing unless a
 * subclass overrides them, so an exporter only writes the parts that make sense for its format.
 * For example the JSON writes the run summary and every event, while the PDF writes the timeline.
 */
public abstract class AbstractExporter implements Exporter {

    /** Only called by subclasses through super(), since an abstract class cannot be made directly. */
    protected AbstractExporter() {
    }

    /**
     * The template method. Makes the folder if needed, then runs every step in the fixed order
     * and returns the finished file.
     *
     * @param summary the session totals to write
     * @param folder where to save the file
     * @param name the file name without an extension, the extension comes from {@link #getExtension()}
     * @return the file that was written
     * @throws IOException if any step cannot write
     */
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

    // required steps, every exporter must write these

    /**
     * Gives the file extension for this format, without the dot.
     *
     * @return eg {@code json} or {@code pdf}
     */
    protected abstract String getExtension();

    /**
     * First step. Gets ready to write to the file, eg opens a document or starts an empty object.
     * Called once per export, before any write step.
     *
     * @param file the file this export will write to
     * @throws IOException if the file cannot be opened
     */
    protected abstract void start(File file) throws IOException;

    /**
     * Writes the session details: the student ID placeholder, session times, active and idle time,
     * and the edit numbers.
     *
     * @param summary the session totals
     * @throws IOException if this part cannot be written
     */
    protected abstract void writeSessionInfo(SessionSummary summary) throws IOException;

    /**
     * Writes the compile details: attempts, successes and how many times each error code came up.
     *
     * @param summary the session totals
     * @throws IOException if this part cannot be written
     */
    protected abstract void writeCompileInfo(SessionSummary summary) throws IOException;

    /**
     * Last step. Saves and closes everything, so the file is complete on disk once this returns.
     * Called once per export, after every write step.
     *
     * @throws IOException if the file cannot be saved
     */
    protected abstract void finish() throws IOException;

    // hooks, they do nothing unless a subclass needs that part

    /**
     * Hook for the run summary. Does nothing here, override it to write how the runs ended.
     *
     * @param summary the session totals
     * @throws IOException if this part cannot be written
     */
    protected void writeRunInfo(SessionSummary summary) throws IOException {
    }

    /**
     * Hook for the class and method timeline. Does nothing here, override it to write the timeline.
     *
     * @param summary the session totals
     * @throws IOException if this part cannot be written
     */
    protected void writeTimeline(SessionSummary summary) throws IOException {
    }

    /**
     * Hook for the full list of events. Does nothing here, override it to write every event.
     *
     * @param summary the session totals
     * @throws IOException if this part cannot be written
     */
    protected void writeEvents(SessionSummary summary) throws IOException {
    }
}