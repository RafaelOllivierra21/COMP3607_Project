package comp3607.telemetry.report;

import comp3607.telemetry.EventBuffer;

import java.util.List;

/**
 * Saves a JSON backup of the session every 60 seconds in case BlueJ crashes
 * before the final report is written.
 
 * The backup is called {@code <project>_autosave.json} and is saved in the open project's folder.
 * It has no time in its name, so the same file is overwritten each time and the project folder
 * does not fill up with backups.
 */
public class AutosaveTimer implements Runnable {

    /** Time between backups, in milliseconds. */
    private static final long INTERVAL_MS = 60 * 1000;

    /** Buffer each backup is taken from. */
    private final EventBuffer buffer;

    /** Background runner the export command is handed to. */
    private final ExportRunner runner;

    /** Exporters used for the backup, normally just JSON. */
    private final List<Exporter> exporters;

    /** Says which folder to save in and what to call the file. */
    private final ReportLocation location;

    /**
     * Makes the timer. Nothing happens until it is run on a thread.
     *
     * @param buffer the shared buffer of recorded events
     * @param runner the background runner that writes the files
     * @param exporters the exporters to run for each backup
     * @param location where the backup goes and how it is named
     */
    public AutosaveTimer(EventBuffer buffer, ExportRunner runner, List<Exporter> exporters, ReportLocation location) {
        this.buffer = buffer;
        this.runner = runner;
        this.exporters = exporters;
        this.location = location;
    }

    /**
     * Waits 60 seconds, queues a backup, and repeats until the thread is interrupted.
     * The folder and name are read on every tick, so the backup follows the project
     * if a different one is opened.
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                // terminate() interrupts this thread when bluej closes
                return;
            }
            runner.submit(new ExportCommand(buffer, exporters, location.getFolder(), location.name("autosave")));
        }
    }
}