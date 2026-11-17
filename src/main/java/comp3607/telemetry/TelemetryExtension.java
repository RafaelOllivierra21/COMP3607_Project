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
import comp3607.telemetry.report.ReportLocation;
import comp3607.telemetry.report.TelemetryMenu;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point of the BlueJ telemetry plugin and the Facade for the whole system.
 */
public class TelemetryExtension extends Extension implements PackageListener {

    /** How long BlueJ waits for the final report to be written when it closes, in milliseconds. */
    private static final long FINAL_REPORT_WAIT_MS = 10000;

    /** Shared buffer that every monitor records into and the exporters read from. */
    private final EventBuffer buffer = new EventBuffer();
    /** Background thread running the {@link ClassMonitor} scan. */
    private Thread classSampler;
    /** Background thread running the {@link EditorMonitor} sampling. */
    private Thread editorSampler;

    // part 2, exporting the json and pdf
    /** Decides the folder and file names for every report, set to the project folder when a project opens. */
    private ReportLocation location;
    /** The exporters used for a full report: JSON and PDF. */
    private List<Exporter> exporters;
    /** The single background thread that writes every report file. */
    private ExportRunner runner;
    /** Background thread that saves the JSON report every 60 seconds in case BlueJ crashes. */
    private Thread autosaver;

    /** @return the shared buffer holding every event recorded so far */
    public EventBuffer getBuffer() {
        return buffer;
    }

    /**
     * Called once by BlueJ when the plugin loads. Registers the listeners, starts the
     * background threads, and sets up the Tools menu and autosave.
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

        // part 2, reports go in the open project's folder
        // the BlueJTelemetry folder in the users home is only used if no project has been opened yet
        location = new ReportLocation(new File(System.getProperty("user.home"), "BlueJTelemetry"));

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
        bluej.setMenuGenerator(new TelemetryMenu(buffer, runner, exporters, location));

        // json autosave every 60 seconds in case bluej crashes
        autosaver = new Thread(new AutosaveTimer(buffer, runner, autosaveExporters, location), "telemetry-autosave");
        autosaver.setDaemon(true);
        autosaver.start();

        System.out.println("[Telemetry] plugin started");
    }

    /**
     * Records {@link TelemetryEvent#PROJECT_OPEN} with the project name, and tells the
     * {@link ReportLocation} to save reports in this project's folder from now on.
     *
     * @param event the package event from BlueJ
     */
    @Override
    public void packageOpened(PackageEvent event) {
        try {
            String name = event.getPackage().getProject().getName();
            File dir = event.getPackage().getProject().getDir();
            location.setProject(dir, name);
            buffer.add(TelemetryEvent.PROJECT_OPEN, name);
            System.out.println("[Telemetry] project opened: " + name + " (reports go to " + dir.getPath() + ")");
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

    /**
     * Called by BlueJ when it closes. Stops the background threads and writes the final report
     * into the last opened project's folder, named with the project name and the time.
     */
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
            File folder = location.getFolder();
            String name = location.nameWithTime("final");
            runner.finish(new ExportCommand(buffer, exporters, folder, name), FINAL_REPORT_WAIT_MS);
        }

        System.out.println("[Telemetry] plugin terminating");
    }

    /** @return {@code true} if BlueJ's extensions API is version 3 or newer */
    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    /** @return the plugin name shown in BlueJ */
    @Override
    public String getName() {
        return "BlueJ Telemetry";
    }

    /** @return the plugin version shown in BlueJ */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /** @return a short description of the plugin shown in BlueJ */
    @Override
    public String getDescription() {
        return "Collects telemetry data for COMP 3607";
    }

    /** @return the project's GitHub page, or {@code null} if the address cannot be built */
    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/RafaelOllivierra21/COMP3607_Project").toURL();
        } catch (Exception e) {
            return null;
        }
    }
}