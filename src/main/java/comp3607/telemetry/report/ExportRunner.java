package comp3607.telemetry.report;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// the invoker for the command pattern
// one background thread runs the commands one at a time in the order they came in
// so writing files never freezes the bluej window
public class ExportRunner implements Runnable {

    private final LinkedBlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    private Thread thread;

    // volatile so the export thread sees the change when the bluej thread sets it
    private volatile boolean stopping = false;

    public void start() {
        thread = new Thread(this, "telemetry-export");
        thread.setDaemon(true);
        thread.start();
    }

    // the sender hands over a command and goes straight back to what it was doing
    public void submit(Command command) {
        queue.add(command);
    }

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

    // used when bluej closes, adds the last command then waits for it to be written
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