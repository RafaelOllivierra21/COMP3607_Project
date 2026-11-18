package comp3607.telemetry.report;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import comp3607.telemetry.EventBuffer;

class ExportCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void executeRunsAllExportersAndWritesFiles() {
        EventBuffer buffer = new EventBuffer();
        buffer.add("COMPILE_SUCCESS", "Main.java");

        List<Exporter> exporters = List.of(new JsonExporter(), new PdfExporter());
        ExportCommand command = new ExportCommand(buffer, exporters, tempDir.toFile(), "TestProject_snapshot");

        command.execute();

        assertTrue(tempDir.resolve("TestProject_snapshot.json").toFile().exists());
        assertTrue(tempDir.resolve("TestProject_snapshot.pdf").toFile().exists());
    }

    @Test
    void executeContinuesWhenOneExporterThrowsException() {
        EventBuffer buffer = new EventBuffer();
        buffer.add("RUN_END", "normal");

        Exporter failingExporter = (summary, folder, name) -> {
            throw new IOException("Simulated disk error");
        };

        List<Exporter> exporters = List.of(failingExporter, new JsonExporter());
        ExportCommand command = new ExportCommand(buffer, exporters, tempDir.toFile(), "RecoveryTest");

        assertDoesNotThrow(command::execute);
        assertTrue(tempDir.resolve("RecoveryTest.json").toFile().exists());
    }
}