package comp3607.telemetry;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

import java.net.URI;
import java.net.URL;

public class TelemetryExtension extends Extension implements PackageListener {

    private final EventBuffer buffer = new EventBuffer();
    private Thread classSampler;

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