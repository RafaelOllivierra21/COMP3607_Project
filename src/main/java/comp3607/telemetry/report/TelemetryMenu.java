package comp3607.telemetry.report;

import bluej.extensions2.BPackage;
import bluej.extensions2.MenuGenerator;
import comp3607.telemetry.EventBuffer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.util.List;

// adds "Export telemetry report" to the bluej tools menu
// clicking it is a sender in the command pattern, it makes a command and gives it to the runner
public class TelemetryMenu extends MenuGenerator implements EventHandler<ActionEvent> {

    private final EventBuffer buffer;
    private final ExportRunner runner;
    private final List<Exporter> exporters;
    private final File folder;

    public TelemetryMenu(EventBuffer buffer, ExportRunner runner, List<Exporter> exporters, File folder) {
        this.buffer = buffer;
        this.runner = runner;
        this.exporters = exporters;
        this.folder = folder;
    }

    // bluej can ask for the item more than once so we make a new one every time
    @Override
    public MenuItem getToolsMenuItem(BPackage bPackage) {
        MenuItem item = new MenuItem("Export telemetry report");
        item.setOnAction(this);
        return item;
    }

    // runs on the bluej window thread when the item is clicked, so it only queues the job and returns
    @Override
    public void handle(ActionEvent event) {
        String name = ExportCommand.nameWithTime("snapshot");
        runner.submit(new ExportCommand(buffer, exporters, folder, name));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Telemetry report export started");
        alert.setContentText("A JSON and PDF snapshot is being saved to:\n" + folder.getPath());
        alert.show();
    }
}