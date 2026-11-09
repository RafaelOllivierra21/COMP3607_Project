package comp3607.telemetry;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

import java.net.URI;
import java.net.URL;

public class TelemetryExtension extends Extension implements PackageListener {

    // BlueJ calls this once when it loads the plugin
    @Override
    public void startup(BlueJ bluej) {
        bluej.addPackageListener(this);
        System.out.println("[Telemetry] plugin started");
    }

    // BlueJ calls this every time a project (package) is opened
    @Override
    public void packageOpened(PackageEvent event) {
        try {
            String name = event.getPackage().getProject().getName();
            System.out.println("[Telemetry] project opened: " + name);
        } catch (ProjectNotOpenException e) {
            System.out.println("[Telemetry] project closed before it could be read");
        }
    }

    @Override
    public void packageClosing(PackageEvent event) {
        System.out.println("[Telemetry] project closing");
    }

    // BlueJ calls this when it shuts down
    @Override
    public void terminate() {
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