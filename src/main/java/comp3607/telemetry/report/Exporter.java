package comp3607.telemetry.report;

import java.io.File;
import java.io.IOException;

// every kind of export (json, pdf) has this one method
public interface Exporter {

    // writes the report into the folder and returns the file it made
    File export(SessionSummary summary, File folder, String name) throws IOException;
}