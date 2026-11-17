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

/**
 * Samples the open code editors every five seconds to record editing and idle time.
 *
 * BlueJ gives no callback when the user types, so this class polls instead of being
 * an Observer. It remembers a hash of each open editor's text: a changed hash means
 * {@link TelemetryEvent#EDIT}, and an unchanged one for two minutes means
 * {@link TelemetryEvent#IDLE}. Idle is reported once per quiet period for each class.
 */
public class EditorMonitor implements Runnable {

    /** How long to wait between samples, in milliseconds. */
    private static final long INTERVAL_MS = 5000;
    /** How long an editor must stay unchanged to count as idle, in milliseconds (two minutes). */
    private static final long IDLE_MS = 2 * 60 * 1000;

    /** What is remembered about one open editor between samples. */
    private static class EditorState {
        /** Hash of the editor text at the last sample. */
        int lastHash;
        /** Time the text last changed, in milliseconds since the epoch. */
        long lastChange;
        /** Whether {@link TelemetryEvent#IDLE} has already been recorded for this quiet period. */
        boolean idleReported;
    }

    /** Entry point to BlueJ, used to list the open projects and their classes. */
    private final BlueJ bluej;
    /** Shared buffer the edit and idle events are written into. */
    private final EventBuffer buffer;
    /** State of each open editor, keyed by {@code projectName:ClassName}. */
    private final Map<String, EditorState> states = new HashMap<>();

    /**
     * Creates the monitor. It does not start sampling until {@link #run()} is started on a thread.
     *
     * @param bluej the BlueJ entry point
     * @param buffer the shared buffer to record edit and idle events into
     */
    public EditorMonitor(BlueJ bluej, EventBuffer buffer) {
        this.bluej = bluej;
        this.buffer = buffer;
    }

    /** Samples the editors every {@link #INTERVAL_MS} milliseconds until the thread is interrupted. */
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

    /** Looks at every class in every open project once, then forgets editors that have been closed. */
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

    /**
     * Checks one class. Classes without an open editor are skipped.
     *
     * @param projectName name of the project the class belongs to
     * @param bclass the class to check
     * @param openEditors collects the keys of the editors found open in this sample
     * @param now the time of this sample, in milliseconds since the epoch
     */
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

    /**
     * Reads the full text of an editor.
     *
     * @param editor the open Java editor
     * @return the whole source text, or an empty string if none could be read
     */
    private String readText(JavaEditor editor) {
        String text = editor.getText(
                editor.getTextLocationFromOffset(0),
                editor.getTextLocationFromOffset(editor.getTextLength()));
        return text == null ? "" : text;
    }

    /**
     * Compares an editor's text with the last sample and records an edit or idle event.
     * The first sample of an editor only remembers it, so opening a file is not an edit.
     *
     * @param key the {@code projectName:ClassName} key for this editor
     * @param className the class name, used as the event detail
     * @param hash hash of the editor text right now
     * @param now the time of this sample, in milliseconds since the epoch
     */
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
