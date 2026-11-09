package comp3607.telemetry;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassMonitor implements ClassListener, Runnable {

    private static final long INTERVAL_MS = 5000;

    private final BlueJ bluej;
    private final EventBuffer buffer;
    

    private final Map<String, Set<String>> knownClasses = new HashMap<>();

    public ClassMonitor(BlueJ bluej, EventBuffer buffer) {
        this.bluej = bluej;
        this.buffer = buffer;
    }

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

    @Override
    public synchronized void classRemoved(ClassEvent event) {
        String name = event.getBClass().getName();
        buffer.add(TelemetryEvent.CLASS_DELETE, name);

        Set<String> known = knownClasses.get(projectName(event));
        if (known != null) {
            known.remove(name);
        }
    }

    @Override
    public void classStateChanged(ClassEvent event) {
    }

    private String projectName(ClassEvent event) {
        try {
            return event.getPackage().getProject().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
