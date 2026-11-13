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

public class TelemetryExtension extends Extension implements PackageListener {

    // how long bluej waits for the final report when it closes
    private static final long FINAL_REPORT_WAIT_MS = 10000;

    private final EventBuffer buffer = new EventBuffer();
    private Thread classSampler;
    private Thread editorSampler;

    // part 2, exporting the json and pdf
    private File folder;
    private List<Exporter> exporters;
    private ExportRunner runner;
    private Thread autosaver;

    public EventBuffer getBuffer() {
        return buffer;
    }

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

    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    @Override
    public String getName() {
        return "BlueJ Telemetry";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Collects telemetry data for COMP 3607";
    }

    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/RafaelOllivierra21/COMP3607_Project").toURL();
        } catch (Exception e) {
            return null;
        }
    }
}