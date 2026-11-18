package comp3607.telemetry;

/**
 * One telemetry record: what happened, extra detail about it, and when it happened.
 *
 * Instances are immutable, so they are safe to share between threads. The
 * {@code String} constants below are the event types used across the plugin.
 * The monitors create events through {@link EventBuffer#add(String, String)} and
 * the report package reads them back out of the buffer.
 */
public final class TelemetryEvent {

    /** Event type: a BlueJ project was opened. Detail is the project name. */
    public static final String PROJECT_OPEN = "PROJECT_OPEN";
    /** Event type: a BlueJ project was closed. Detail is the project name. */
    public static final String PROJECT_CLOSE = "PROJECT_CLOSE";
    /** Event type: a new class appeared in a project. Detail is the class name. */
    public static final String CLASS_CREATE = "CLASS_CREATE";
    /** Event type: a class was renamed. Detail is {@code OldName -> NewName}. */
    public static final String CLASS_RENAME = "CLASS_RENAME";
    /** Event type: a class was removed from a project. Detail is the class name. */
    public static final String CLASS_DELETE = "CLASS_DELETE";
    /** Event type: the text in a class's open editor changed. Detail is the class name. */
    public static final String EDIT = "EDIT";
    /** Event type: an open editor has not changed for two minutes. Detail is the class name. */
    public static final String IDLE = "IDLE";

    /** Event type: a user-triggered compile began. Detail is the comma separated file names. */
    public static final String COMPILE_START = "COMPILE_START";
    /** Event type: a user-triggered compile succeeded. Detail is the comma separated file names. */
    public static final String COMPILE_OK = "COMPILE_OK";
    /** Event type: the compiler reported an error. Detail is {@code code|line|message}, e.g. {@code E02|12|';' expected}. */
    public static final String COMPILE_ERROR = "COMPILE_ERROR";
    /** Event type: a user-triggered compile finished with errors. Detail is the comma separated file names. */
    public static final String COMPILE_FAILED = "COMPILE_FAILED";

    /** Event type: a method was found in a class that compiled successfully. Detail is {@code ClassName.methodName}. */
    public static final String METHOD_DEFINED = "METHOD_DEFINED";

    /** Event type: a method or constructor was run from BlueJ. Detail is {@code Class.method | status | result}. */
    public static final String RUN = "RUN";

    /** One of the event type constants, such as {@link #COMPILE_OK}. */
    private final String type;
    /** Extra information whose format depends on the event type. */
    private final String detail;
    /** When the event was recorded, in milliseconds since the epoch. */
    private final long timestamp;

    /**
     * Creates an event.
     *
     * @param type one of the event type constants
     * @param detail extra information for this event type
     * @param timestamp when the event happened, in milliseconds since the epoch
     */
    public TelemetryEvent(String type, String detail, long timestamp) {
        this.type = type;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    /**
     * Gets the type of this event.
     *
     * @return the event type constant
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the extra information stored with this event.
     *
     * @return the extra information for this event
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Gets the time this event was recorded.
     *
     * @return the time the event was recorded, in milliseconds since the epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
}
