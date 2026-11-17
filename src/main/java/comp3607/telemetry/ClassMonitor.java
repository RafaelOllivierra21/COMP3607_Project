package comp3607.telemetry;

import bluej.extensions2.BClass;
import bluej.extensions2.BMethod;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Watches the classes in the open projects. It does two jobs:
 * <ul>
 * <li>As an Observer of BlueJ's {@link ClassListener}, it is called back when a class
 * is renamed, removed, or finishes compiling (which records its methods).</li>
 * <li>As a {@link Runnable} on its own thread, it compares the class list every five
 * seconds to spot newly created classes, because BlueJ gives no callback for that.</li>
 * </ul>
 * Everything is recorded into the {@link EventBuffer}.
 */
public class ClassMonitor implements ClassListener, Runnable {

    /** How long to wait between class list scans, in milliseconds. */
    private static final long INTERVAL_MS = 5000;

    /** Entry point to BlueJ, used to list the open projects and their classes. */
    private final BlueJ bluej;
    /** Shared buffer the class events are written into. */
    private final EventBuffer buffer;
    

    /** Class names seen at the last scan, keyed by project name. */
    private final Map<String, Set<String>> knownClasses = new HashMap<>();

    /**
     * Creates the monitor. It does not start scanning until {@link #run()} is started on a thread.
     *
     * @param bluej the BlueJ entry point
     * @param buffer the shared buffer to record class events into
     */
    public ClassMonitor(BlueJ bluej, EventBuffer buffer) {
        this.bluej = bluej;
        this.buffer = buffer;
    }

    /** Scans the class list every {@link #INTERVAL_MS} milliseconds until the thread is interrupted. */
    @Override
    public void run() {
        while (true) {
            scan();
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Compares the classes now with the classes at the last scan and records
     * {@link TelemetryEvent#CLASS_CREATE} for each new one. A project seen for the first
     * time is only remembered, so its existing classes are not reported as new.
     */
    private synchronized void scan() {
        try {
            Map<String, Set<String>> current = new HashMap<>();
            for (BProject project : bluej.getOpenProjects()) {
                Set<String> names = new HashSet<>();
                for (BPackage pkg : project.getPackages()) {
                    for (BClass c : pkg.getClasses()) {
                        names.add(c.getName());
                    }
                }
                current.put(project.getName(), names);
            }

            for (String project : current.keySet()) {
                Set<String> known = knownClasses.get(project);
                if (known == null) {
                    continue;
                }
                for (String name : current.get(project)) {
                    if (!known.contains(name)) {
                        buffer.add(TelemetryEvent.CLASS_CREATE, name);
                    }
                }
            }

            knownClasses.clear();
            knownClasses.putAll(current);
        } catch (Exception e) {
            System.out.println("[Telemetry] class scan skipped: " + e);
        }
    }

    /**
     * Records {@link TelemetryEvent#CLASS_RENAME} as {@code OldName -> NewName} and updates
     * the remembered class names so the next scan does not report it as a new class.
     *
     * @param event the class event from BlueJ
     */
    @Override
    public synchronized void classNameChanged(ClassEvent event) {
        String oldName = event.getOldName();
        String newName = event.getBClass().getName();
        buffer.add(TelemetryEvent.CLASS_RENAME, oldName + " -> " + newName);

        Set<String> known = knownClasses.get(projectName(event));
        if (known != null) {
            known.remove(oldName);
            known.add(newName);
        }
    }

    /**
     * Records {@link TelemetryEvent#CLASS_DELETE} and forgets the class.
     *
     * @param event the class event from BlueJ
     */
    @Override
    public synchronized void classRemoved(ClassEvent event) {
        String name = event.getBClass().getName();
        buffer.add(TelemetryEvent.CLASS_DELETE, name);

        Set<String> known = knownClasses.get(projectName(event));
        if (known != null) {
            known.remove(name);
        }
    }

    /**
     * When a class has just compiled successfully, records {@link TelemetryEvent#METHOD_DEFINED}
     * as {@code ClassName.methodName} for each method declared in it. The same method is
     * recorded again after every successful compile, so the report removes the repeats.
     *
     * @param event the class event from BlueJ
     */
    @Override
    public void classStateChanged(ClassEvent event) {
        if (!event.isClassCompiled()) {
            return;
        }
        try {
            BClass bClass = event.getBClass();
            for (BMethod method : bClass.getDeclaredMethods()) {
                buffer.add(TelemetryEvent.METHOD_DEFINED,
                        bClass.getName() + "." + method.getName());
            }
        } catch (Exception e) {
            System.out.println("[Telemetry] method read skipped: " + e);
        }
    }

    /**
     * Finds the name of the project a class event belongs to.
     *
     * @param event the class event from BlueJ
     * @return the project name, or {@code null} if the project is no longer open
     */
    private String projectName(ClassEvent event) {
        try {
            return event.getPackage().getProject().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
