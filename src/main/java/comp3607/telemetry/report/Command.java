package comp3607.telemetry.report;

/**
 * The Command pattern interface: one job that the export thread can run later.

 * Senders ({@link TelemetryMenu}, {@link AutosaveTimer} and {@code TelemetryExtension.terminate()})
 * make a command and hand it to {@link ExportRunner}, the invoker, which runs commands one at a time
 * on its own thread. The sender never waits for the work to finish, so the BlueJ window never freezes.
 * {@link ExportCommand} is the one concrete command.
 */

public interface Command {

    /**
     * Does the job. This is called on the export thread, not the BlueJ thread.
     * Implementations should catch and log their own errors, because an exception
     * thrown out of here would stop the export thread and no more reports would be saved.
     */
    void execute();
}