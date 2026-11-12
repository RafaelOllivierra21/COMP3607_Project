package comp3607.telemetry.report;

import comp3607.telemetry.ErrorCodes;

// splits up the detail strings that the monitors save in each event
// compile errors look like  E01|12|cannot find symbol
// runs look like            Counter.increment | normal | 5
// renames look like         Counter -> Tally
public class DetailParser {

    private static final String ERROR_SEP = "|";
    private static final String RUN_SEP = " | ";
    private static final String RENAME_SEP = " -> ";

    // compile errors

    public static String errorCode(String detail) {
        if (detail == null) {
            return ErrorCodes.OTHER;
        }
        int first = detail.indexOf(ERROR_SEP);
        if (first == -1) {
            return ErrorCodes.OTHER;
        }
        return detail.substring(0, first);
    }

    // gives -1 if the line number is missing or not a number
    public static int errorLine(String detail) {
        if (detail == null) {
            return -1;
        }
        int first = detail.indexOf(ERROR_SEP);
        if (first == -1) {
            return -1;
        }
        int second = detail.indexOf(ERROR_SEP, first + 1);
        if (second == -1) {
            return -1;
        }
        try {
            return Integer.parseInt(detail.substring(first + 1, second));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // everything after the second bar, so a | inside the message is kept
    public static String errorMessage(String detail) {
        if (detail == null) {
            return "";
        }
        int first = detail.indexOf(ERROR_SEP);
        if (first == -1) {
            return "";
        }
        int second = detail.indexOf(ERROR_SEP, first + 1);
        if (second == -1) {
            return "";
        }
        return detail.substring(second + 1);
    }

    // runs

    // the Class.method part
    public static String runTarget(String detail) {
        if (detail == null) {
            return "";
        }
        int first = detail.indexOf(RUN_SEP);
        if (first == -1) {
            return detail;
        }
        return detail.substring(0, first);
    }

    // normal, exception, terminated or unknown
    public static String runStatus(String detail) {
        if (detail == null) {
            return "unknown";
        }
        int first = detail.indexOf(RUN_SEP);
        if (first == -1) {
            return "unknown";
        }
        int second = detail.indexOf(RUN_SEP, first + RUN_SEP.length());
        if (second == -1) {
            return "unknown";
        }
        return detail.substring(first + RUN_SEP.length(), second);
    }

    public static String runResult(String detail) {
        if (detail == null) {
            return "";
        }
        int first = detail.indexOf(RUN_SEP);
        if (first == -1) {
            return "";
        }
        int second = detail.indexOf(RUN_SEP, first + RUN_SEP.length());
        if (second == -1) {
            return "";
        }
        return detail.substring(second + RUN_SEP.length());
    }

    // rename
    public static String oldName(String detail) {
        if (detail == null) {
            return "";
        }
        int arrow = detail.indexOf(RENAME_SEP);
        if (arrow == -1) {
            return "";
        }
        return detail.substring(0, arrow);
    }

    public static String newName(String detail) {
        if (detail == null) {
            return "";
        }
        int arrow = detail.indexOf(RENAME_SEP);
        if (arrow == -1) {
            return detail;
        }
        return detail.substring(arrow + RENAME_SEP.length());
    }
}