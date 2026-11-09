package comp3607.telemetry;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.editor.JavaEditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EditorMonitor implements Runnable {

    private static final long INTERVAL_MS = 5000;
    private static final long IDLE_MS = 2 * 60 * 1000;

    private static class EditorState {
        int lastHash;
        long lastChange;
        boolean idleReported;
    }

    private final BlueJ bluej;
    private final EventBuffer buffer;
    private final Map<String, EditorState> states = new HashMap<>();

    public EditorMonitor(BlueJ bluej, EventBuffer buffer) {
        this.bluej = bluej;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (true) {
            sample();
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void sample() {
        try {
            long now = System.currentTimeMillis();
            Set<String> openEditors = new HashSet<>();

            for (BProject project : bluej.getOpenProjects()) {
                for (BPackage pkg : project.getPackages()) {
                    for (BClass bclass : pkg.getClasses()) {
                        sampleClass(project.getName(), bclass, openEditors, now);
                    }
                }
            }

            states.keySet().retainAll(openEditors);
        } catch (Exception e) {
            System.out.println("[Telemetry] editor sample skipped: " + e);
        }
    }

    private void sampleClass(String projectName, BClass bclass, Set<String> openEditors, long now) {
        String className = bclass.getName();
        String key = projectName + ":" + className;
        try {
            JavaEditor editor = bclass.getJavaEditorIfOpen();
            if (editor == null) {
                return;
            }
            openEditors.add(key);
            check(key, className, readText(editor).hashCode(), now);
        } catch (Exception e) {
            System.out.println("[Telemetry] editor sample skipped for " + className + ": " + e);
        }
    }

    private String readText(JavaEditor editor) {
        String text = editor.getText(
                editor.getTextLocationFromOffset(0),
                editor.getTextLocationFromOffset(editor.getTextLength()));
        return text == null ? "" : text;
    }

    private void check(String key, String className, int hash, long now) {
        EditorState state = states.get(key);
        if (state == null) {
            state = new EditorState();
            state.lastHash = hash;
            state.lastChange = now;
            states.put(key, state);
            return;
        }

        if (hash != state.lastHash) {
            state.lastHash = hash;
            state.lastChange = now;
            state.idleReported = false;
            buffer.add(TelemetryEvent.EDIT, className);
        } else if (!state.idleReported && now - state.lastChange >= IDLE_MS) {
            state.idleReported = true;
            buffer.add(TelemetryEvent.IDLE, className);
        }
    }
}
