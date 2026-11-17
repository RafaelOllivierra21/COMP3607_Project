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

/**
 * Adds "Export telemetry report" to the BlueJ Tools menu so a JSON and PDF snapshot
 * of the session so far can be saved at any time.
 */
public class TelemetryMenu extends MenuGenerator implements EventHandler<ActionEvent> {

    /** Text shown on the item in the Tools menu. */
    public static final String MENU_TEXT = "Export telemetry report";

    /** Buffer the snapshot is taken from. */
    private final EventBuffer buffer;

    /** Background runner the export command is handed to. */
    private final ExportRunner runner;

    /** Exporters used for a snapshot, JSON and PDF. */
    private final List<Exporter> exporters;

    /** Says which folder to save in and what to call the files. */
    private final ReportLocation location;

    /**
     * Makes the menu generator. Nothing is exported until the item is clicked.
     *
     * @param buffer the shared buffer of recorded events
     * @param runner the background runner that writes the files
     * @param exporters the exporters to run for each snapshot
     * @param location where the files go and how they are named
     */
    public TelemetryMenu(EventBuffer buffer, ExportRunner runner, List<Exporter> exporters, ReportLocation location) {
        this.buffer = buffer;
        this.runner = runner;
        this.exporters = exporters;
        this.location = location;
    }

    /**
     * Makes the Tools menu item. BlueJ can ask for it more than once, eg once per project
     * window, so a new item is made every time.
     *
     * @param bPackage the package whose window the menu is for, not used because
     *                 the report location already knows the open project
     * @return a new menu item with this class handling its clicks
     */
    @Override
    public MenuItem getToolsMenuItem(BPackage bPackage) {
        MenuItem item = new MenuItem(MENU_TEXT);
        item.setOnAction(this);
        return item;
    }

    /**
     * Queues a snapshot export when the item is clicked, then shows a small message saying
     * which files are being saved and where. This runs on the BlueJ window thread, so it only
     * queues the work and returns straight away.
     *
     * @param event the click event from JavaFX, not used
     */
    @Override
    public void handle(ActionEvent event) {
        // read the folder and name once so the message matches the files that get written
        File folder = location.getFolder();
        String name = location.nameWithTime("snapshot");
        runner.submit(new ExportCommand(buffer, exporters, folder, name));

        // show() instead of showAndWait() so the bluej window doesnt get blocked
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Telemetry report export started");
        alert.setContentText("Saving " + name + ".json and .pdf to:\n" + folder.getPath());
        alert.show();
    }
}