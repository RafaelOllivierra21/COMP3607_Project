package comp3607.telemetry.report;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Decides where reports get saved and what they are called.
 
 * Reports go straight into the folder of the BlueJ project being worked on, and every
 * file name starts with the project name, eg {@code TelemetryTest_snapshot_2026-11-17_17-30-05}.
 * Until a project is opened it falls back to a default folder and the name {@code no_project}.
*/

public class ReportLocation {

    /** Name put at the front of report files when no project has been opened yet. */
    public static final String NO_PROJECT = "no_project";

    /** Date and time pattern added to the end of timed file names. */
    private static final String TIME_PATTERN = "yyyy-MM-dd_HH-mm-ss";

    /** Folder used until a project is opened, eg the BlueJTelemetry folder in the user's home. */
    private final File defaultFolder;

    /** Folder of the last project that was opened, null until one opens. */
    private File projectFolder;

    /** Name of the last project that was opened, null until one opens. */
    private String projectName;

    /**
     * Makes a location with no project yet, so reports go to the default folder for now.
     *
     * @param defaultFolder where to save reports if no project has been opened
     */
    public ReportLocation(File defaultFolder) {
        this.defaultFolder = defaultFolder;
    }

    /**
     * Switches reports over to a newly opened project. It is not cleared when the project
     * closes, so the final report still goes to that project when BlueJ shuts down.
     *
     * @param folder the project's folder on disk
     * @param name the project's name, used at the start of every file name
     */
    public synchronized void setProject(File folder, String name) {
        projectFolder = folder;
        projectName = name;
    }

    /**
     * Returns the folder reports should be saved in right now.
     *
     * @return the last opened project's folder, or the default folder if none has opened
     */
    public synchronized File getFolder() {
        if (projectFolder == null) {
            return defaultFolder;
        }
        return projectFolder;
    }

    /**
     * Returns the name used at the start of report files.
     *
     * @return the last opened project's name, or {@link #NO_PROJECT} if none has opened
     */
    public synchronized String getProjectName() {
        if (projectName == null) {
            return NO_PROJECT;
        }
        return projectName;
    }

    /**
     * Builds a file name with no time in it, eg {@code TelemetryTest_autosave}.
     * Used for the autosave so the same file is overwritten each time.
     *
     * @param kind what sort of report it is, eg autosave
     * @return the project name and kind joined with an underscore, no extension
     */
    public synchronized String name(String kind) {
        return getProjectName() + "_" + kind;
    }

    /**
     * Builds a file name that ends with the current date and time,
     * eg {@code TelemetryTest_final_2026-11-17_18-05-37}, so each session's
     * snapshots and final report get their own files and never overwrite each other.
     *
     * @param kind what sort of report it is, eg snapshot or final
     * @return the project name, kind and time joined with underscores, no extension
     */
    public synchronized String nameWithTime(String kind) {
        // a new formatter each time because SimpleDateFormat is not safe to share between threads
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN);
        return name(kind) + "_" + format.format(new Date());
    }
}