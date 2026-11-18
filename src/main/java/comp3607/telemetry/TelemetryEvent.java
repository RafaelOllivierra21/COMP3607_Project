package comp3607.telemetry;

/**
 * Holds one telemetry record: what happened, extra detail about it, and when it happened.
 *
 * Instances are immutable, so they can be shared between threads without locking.
 * The {@code String} constants in this class are the event types used across the
 * plugin. The monitors do not normally call the constructor themselves. They call
 * {@link EventBuffer#add(String, String)}, which stamps the event with the current
 * time, and the report package reads the events back out of the buffer.
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
     * Creates an event with a fixed type, detail and time.
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
     * Returns the type of this event, which tells the report how to read the detail.
     *
     * @return one of the event type constants of this class, such as {@link #COMPILE_OK}
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the extra information stored with this event. What it holds depends on
     * the type, as described on each type constant.
     *
     * @return the detail text, for example a class name or {@code code|line|message}
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Returns the time this event was recorded.
     *
     * @return the time in milliseconds since the epoch, as given by {@code System.currentTimeMillis()}
     */
    public long getTimestamp() {
        return timestamp;
    }
}
