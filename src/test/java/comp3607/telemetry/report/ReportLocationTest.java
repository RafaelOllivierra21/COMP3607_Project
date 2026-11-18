package comp3607.telemetry.report;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportLocationTest {

    @TempDir
    Path tempDir;

    @Test
    void defaultsToDefaultFolderAndNoProjectBeforeProjectOpens() {
        File defaultFolder = tempDir.toFile();
        ReportLocation location = new ReportLocation(defaultFolder);

        assertEquals(defaultFolder, location.getFolder());
        assertEquals(ReportLocation.NO_PROJECT, location.getProjectName());
        assertEquals("no_project_autosave", location.name("autosave"));
    }

    @Test
    void updatesFolderAndProjectNameWhenProjectOpens() {
        File defaultFolder = tempDir.resolve("default").toFile();
        File projectFolder = tempDir.resolve("MyBlueJProject").toFile();
        ReportLocation location = new ReportLocation(defaultFolder);

        location.setProject(projectFolder, "MyBlueJProject");

        assertEquals(projectFolder, location.getFolder());
        assertEquals("MyBlueJProject", location.getProjectName());
        assertEquals("MyBlueJProject_autosave", location.name("autosave"));
    }

    @Test
    void nameWithTimeAppendsFormattedTimestamp() {
        ReportLocation location = new ReportLocation(tempDir.toFile());
        location.setProject(tempDir.toFile(), "TelemetryTest");

        String fileName = location.nameWithTime("snapshot");

        assertTrue(
            fileName.matches("^TelemetryTest_snapshot_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}$"),
            "Expected timestamped file name, got: " + fileName
        );
    }
}