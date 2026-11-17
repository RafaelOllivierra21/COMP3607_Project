package comp3607.telemetry;

/**
 * Turns raw Java compiler messages into short error codes (E01 to E18, and E99
 * for anything else) and turns codes back into readable labels.
 *
 * The report uses these codes to count how often each kind of error happened.
 */
public class ErrorCodes {

    /** Code used when a compiler message matches none of the known errors. */
    public static final String OTHER = "E99";

    /**
     * Lookup table. Each row is {@code {code, readable label, lowercase text to find
     * in the compiler message}}. The last row has no text to match, so it is never
     * chosen by {@link #codeFor(String)} and only provides the label for E99.
     */
    private static final String[][] TABLE = {
        {"E01", "cannot find symbol", "cannot find symbol"},
        {"E02", "missing semicolon", "';' expected"},
        {"E03", "identifier expected", "<identifier> expected"},
        {"E04", "incompatible types", "incompatible types"},
        {"E05", "missing return statement", "missing return statement"},
        {"E06", "variable might not be initialized", "might not have been initialized"},
        {"E07", "variable already defined", "already defined"},
        {"E08", "unreachable statement", "unreachable statement"},
        {"E09", "not a statement", "not a statement"},
        {"E10", "reached end of file while parsing", "reached end of file"},
        {"E11", "illegal start of expression or type", "illegal start of"},
        {"E12", "package does not exist", "does not exist"},
        {"E13", "method cannot be applied to arguments", "cannot be applied to"},
        {"E14", "unreported exception", "unreported exception"},
        {"E15", "non-static reference from static context", "non-static"},
        {"E16", "cannot be dereferenced", "cannot be dereferenced"},
        {"E17", "class name does not match file", "should be declared in a file"},
        {"E18", "abstract method not implemented", "does not override abstract method"},
        {"E99", "other error", ""}
    };

    /**
     * Finds the error code for a compiler message by looking for the known text in it.
     *
     * @param message the message from the Java compiler, may be {@code null}
     * @return the matching code such as {@code E02}, or {@link #OTHER} if none match
     */
    public static String codeFor(String message) {
        if (message == null) {
            return OTHER;
        }
        String lower = message.toLowerCase();
        for (String[] row : TABLE) {
            if (!row[2].isEmpty() && lower.contains(row[2])) {
                return row[0];
            }
        }
        return OTHER;
    }

    /**
     * Looks up the readable description for an error code.
     *
     * @param code an error code such as {@code E02}
     * @return the label such as {@code missing semicolon}, or {@code unknown} if the code is not in the table
     */
    public static String labelFor(String code) {
        for (String[] row : TABLE) {
            if (row[0].equals(code)) {
                return row[1];
            }
        }
        return "unknown";
    }
}
