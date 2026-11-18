package comp3607.telemetry;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;
import comp3607.telemetry.report.AutosaveTimer;
import comp3607.telemetry.report.ExportCommand;
import comp3607.telemetry.report.ExportRunner;
import comp3607.telemetry.report.Exporter;
import comp3607.telemetry.report.JsonExporter;
import comp3607.telemetry.report.PdfExporter;
import comp3607.telemetry.report.TelemetryMenu;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves as the entry point of the BlueJ telemetry plugin and as the Facade for the
 * whole system.
 *
 * BlueJ creates the one instance itself, so other classes should not.
 * BlueJ loads this class and calls {@link #startup(BlueJ)}, which creates and wires up
 * the monitors, the shared {@link EventBuffer}, the exporters, the Tools menu and the
 * autosave thread. Everything else only has to talk to this one class. When BlueJ
 * closes, {@link #terminate()} stops the threads and writes the final report.
 *
 * It is also an Observer of BlueJ's {@link PackageListener}, which it uses to record
 * when projects are opened and closed.
 */
public class TelemetryExtension extends Extension implements PackageListener {

    // how long bluej waits for the final report when it closes
    /** How long BlueJ waits for the final report to be written when it closes, in milliseconds. */
    private static final long FINAL_REPORT_WAIT_MS = 10000;

    /** Shared buffer that every monitor records into and the exporters read from. */
    private final EventBuffer buffer = new EventBuffer();
    /** Background thread running the {@link ClassMonitor} scan. */
    private Thread classSampler;
    /** Background thread running the {@link EditorMonitor} sampling. */
    private Thread editorSampler;

    // part 2, exporting the json and pdf
    /** Folder the JSON and PDF reports are saved in. */
    private File folder;
    /** The exporters used for a full report: JSON and PDF. */
    private List<Exporter> exporters;
    /** The single background thread that writes every report file. */
    private ExportRunner runner;
    /** Background thread that saves the JSON report every 60 seconds in case BlueJ crashes. */
    private Thread autosaver;

    /**
     * Creates the extension. BlueJ calls this once when it loads the plugin. The
     * constructor does nothing else, because all the setup happens in {@link #startup(BlueJ)}.
     */
    public TelemetryExtension() {
    }

    /**
     * Returns the shared buffer, for any class that needs to read or add events.
     *
     * @return the one buffer shared by all the monitors and the exporters
     */
    public EventBuffer getBuffer() {
        return buffer;
    }

    /**
     * Registers the listeners, starts the background threads, and sets up the Tools menu
     * and autosave. BlueJ calls this once when the plugin loads.
     *
     * @param bluej the BlueJ entry point
     */
    @Override
    public void startup(BlueJ bluej) {
        bluej.addPackageListener(this);

        ClassMonitor classMonitor = new ClassMonitor(bluej, buffer);
        bluej.addClassListener(classMonitor);
        classSampler = new Thread(classMonitor, "telemetry-class-sampler");
        classSampler.setDaemon(true);
        classSampler.start();

        bluej.addCompileListener(new CompileMonitor(buffer));
        bluej.addInvocationFinishedListener(new RunMonitor(buffer));

        EditorMonitor editorMonitor = new EditorMonitor(bluej, buffer);
        editorSampler = new Thread(editorMonitor, "telemetry-editor-sampler");
        editorSampler.setDaemon(true);
        editorSampler.start();

        // part 2, reports get saved in a BlueJTelemetry folder in the users home folder
        folder = new File(System.getProperty("user.home"), "BlueJTelemetry");

        JsonExporter json = new JsonExporter();
        PdfExporter pdf = new PdfExporter();

        exporters = new ArrayList<>();
        exporters.add(json);
        exporters.add(pdf);

        // autosave only needs the json
        List<Exporter> autosaveExporters = new ArrayList<>();
        autosaveExporters.add(json);

        // the one background thread that writes every file
        runner = new ExportRunner();
        runner.start();

        // tools menu item for a snapshot at any time
        bluej.setMenuGenerator(new TelemetryMenu(buffer, runner, exporters, folder));

        // json autosave every 60 seconds in case bluej crashes
        autosaver = new Thread(new AutosaveTimer(buffer, runner, autosaveExporters, folder), "telemetry-autosave");
        autosaver.setDaemon(true);
        autosaver.start();

        System.out.println("[Telemetry] plugin started");
    }

    /**
     * Records {@link TelemetryEvent#PROJECT_OPEN} with the project name.
     *
     * @param event the package event from BlueJ
     */
    @Override
    public void packageOpened(PackageEvent event) {
        try {
            String name = event.getPackage().getProject().getName();
            buffer.add(TelemetryEvent.PROJECT_OPEN, name);
            System.out.println("[Telemetry] project opened: " + name);
        } catch (ProjectNotOpenException e) {
            System.out.println("[Telemetry] project closed before it could be read");
        }
    }

    /**
     * Records {@link TelemetryEvent#PROJECT_CLOSE} with the project name, or {@code unknown}
     * if the project can no longer be read.
     *
     * @param event the package event from BlueJ
     */
    @Override
    public void packageClosing(PackageEvent event) {
        String name;
        try {
            name = event.getPackage().getProject().getName();
        } catch (ProjectNotOpenException e) {
            name = "unknown";
        }
        buffer.add(TelemetryEvent.PROJECT_CLOSE, name);
        System.out.println("[Telemetry] project closing");
    }

    /** Stops the background threads and writes the final report. BlueJ calls this once when it closes. */
    @Override
    public void terminate() {
        if (classSampler != null) {
            classSampler.interrupt();
        }
        if (editorSampler != null) {
            editorSampler.interrupt();
        }

        // part 2, stop autosaving then write the final report before bluej closes
        if (autosaver != null) {
            autosaver.interrupt();
        }
        if (runner != null) {
            String name = ExportCommand.nameWithTime("final");
            runner.finish(new ExportCommand(buffer, exporters, folder, name), FINAL_REPORT_WAIT_MS);
        }

        System.out.println("[Telemetry] plugin terminating");
    }

    /**
     * Returns whether this plugin can run with the BlueJ that loaded it. The plugin
     * needs version 3 or newer of BlueJ's extensions API.
     *
     * @return {@code true} if the plugin can run, {@code false} if BlueJ is too old
     */
    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    /**
     * Returns the name BlueJ lists for this plugin.
     *
     * @return the plugin name, {@code BlueJ Telemetry}
     */
    @Override
    public String getName() {
        return "BlueJ Telemetry";
    }

    /**
     * Returns the version number BlueJ shows next to the plugin name.
     *
     * @return the version text
     */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * Returns the one line description BlueJ shows for this plugin.
     *
     * @return the description text
     */
    @Override
    public String getDescription() {
        return "Collects telemetry data for COMP 3607";
    }

    /**
     * Returns the web page BlueJ links to from the plugin's information, which is the
     * project's GitHub page.
     *
     * @return the GitHub page address, or {@code null} if the address cannot be built
     */
    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/RafaelOllivierra21/COMP3607_Project").toURL();
        } catch (Exception e) {
            return null;
        }
    }
}