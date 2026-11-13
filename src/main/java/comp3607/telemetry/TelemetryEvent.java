package comp3607.telemetry;

public final class TelemetryEvent {

    public static final String PROJECT_OPEN = "PROJECT_OPEN";
    public static final String PROJECT_CLOSE = "PROJECT_CLOSE";
    public static final String CLASS_CREATE = "CLASS_CREATE";
    public static final String CLASS_RENAME = "CLASS_RENAME";
    public static final String CLASS_DELETE = "CLASS_DELETE";
    public static final String EDIT = "EDIT";
    public static final String IDLE = "IDLE";

    public static final String COMPILE_START = "COMPILE_START";
    public static final String COMPILE_OK = "COMPILE_OK";
    public static final String COMPILE_ERROR = "COMPILE_ERROR";   
    public static final String COMPILE_FAILED = "COMPILE_FAILED"; 

    public static final String METHOD_DEFINED = "METHOD_DEFINED";

    public static final String RUN = "RUN";

    private final String type;
    private final String detail;
    private final long timestamp;

    public TelemetryEvent(String type, String detail, long timestamp) {
        this.type = type;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
