package comp3607.telemetry.report;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The invoker in the Command pattern. Runs export commands one at a time on its own
 * background thread, in the order they were submitted.
 
 * Senders ({@link TelemetryMenu}, {@link AutosaveTimer} and {@code TelemetryExtension.terminate()})
 * call {@link #submit(Command)} and go straight back to what they were doing, so writing files
 * never freezes the BlueJ window. Because there is only one export thread, two reports are never
 * written at the same time, so the exporters do not have to be thread safe.

 * How to use it: one runner is made in {@code TelemetryExtension.startup()}, {@link #start()} is
 * called once, commands are submitted while BlueJ is open, and {@link #finish(Command, long)} is
 * called once when BlueJ closes.
 */


public class ExportRunner implements Runnable {

    /** Name given to the background thread, shown in thread dumps and debuggers. */
    public static final String THREAD_NAME = "telemetry-export";

    /** Commands waiting to run. Safe for the BlueJ, autosave and export threads to use at the same time. */
    private final LinkedBlockingQueue<Command> queue = new LinkedBlockingQueue<>();

    /** The background thread that runs the commands, made in {@link #start()}. */
    private Thread thread;

    /**
     * Set to true by {@link #finish(Command, long)} so the thread stops once the queue is empty.
     * Volatile so the export thread sees the change straight away when the BlueJ thread sets it.
     */
    private volatile boolean stopping = false;

    /** Makes a runner with an empty queue. The thread does not start until {@link #start()} is called. */
    public ExportRunner() {
    }

    /**
     * Starts the background thread. Call this once, before submitting anything.
     * The thread is a daemon, so it never keeps BlueJ open by itself.
     */

    public void start() {
        thread = new Thread(this, THREAD_NAME);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Adds a command to the end of the queue and returns straight away without waiting for it to run.
     * Safe to call from any thread.
     *
     * @param command the job to run on the export thread
     */
    public void submit(Command command) {
        queue.add(command);
    }

    /**
     * The export thread's loop, started by {@link #start()} and not meant to be called directly.
     * Takes commands off the queue and runs them in order, and keeps going until
     * {@link #finish(Command, long)} has been called and the queue is empty.
     */
    @Override
    public void run() {
        // keep going until we are told to stop and there is nothing left to do
        while (!stopping || !queue.isEmpty()) {
            try {
                // waits up to 1 second for a command so it can notice when stopping gets set
                Command command = queue.poll(1, TimeUnit.SECONDS);
                if (command != null) {
                    command.execute();
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Queues the last command, tells the thread to stop once the queue is empty, then waits
     * for it to finish. Used when BlueJ closes so the final report is written before shutdown.
     * Any commands already in the queue, eg a waiting autosave, still run first.
     * Call it once, after {@link #start()}.
     *
     * @param lastCommand the final job to run, normally the final report
     * @param maxWaitMillis the longest time to wait in milliseconds, so a stuck export
     *                      can never stop BlueJ from closing
     */
    public void finish(Command lastCommand, long maxWaitMillis) {
        queue.add(lastCommand);
        stopping = true;
        try {
            thread.join(maxWaitMillis);
        } catch (InterruptedException e) {
            System.out.println("[Telemetry] stopped waiting for the final report");
        }
    }
}