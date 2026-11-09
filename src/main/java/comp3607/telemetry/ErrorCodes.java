package comp3607.telemetry;

public class ErrorCodes {

    public static final String OTHER = "E99";

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

    public static String labelFor(String code) {
        for (String[] row : TABLE) {
            if (row[0].equals(code)) {
                return row[1];
            }
        }
        return "unknown";
    }
}
