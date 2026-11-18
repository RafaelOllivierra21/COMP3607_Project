package comp3607.telemetry.report;

import comp3607.telemetry.ErrorCodes;

/**
 * Splits the detail strings that the monitors save in each event into separate fields.
 
 * The monitors store every event's detail as one string so the event model stays simple,
 * and this class takes those strings apart again for the JSON and the totals. The formats are:

 * Every method is static, so there is no need to make a DetailParser.
 * Every method copes with null or badly formed text by giving back a safe default instead
 * of throwing, so one odd event can never stop a report from being written.
 */
public class DetailParser {

    /** Separator between the parts of a compile error detail. */
    private static final String ERROR_SEP = "|";

    /** Separator between the parts of a run detail, with a space on each side. */
    private static final String RUN_SEP = " | ";

    /** Separator between the old and new name in a rename detail. */
    private static final String RENAME_SEP = " -> ";

    // every method is static so nobody needs to make one
    private DetailParser() {
    }

    // compile errors

    /**
     * Gets the error code from a compile error detail.
     *
     * @param detail eg {@code E01|12|cannot find symbol}
     * @return the code, eg {@code E01}, or {@link ErrorCodes#OTHER} if it cannot be found
     */

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

    /**
     * Gets the line number from a compile error detail.
     *
     * @param detail eg {@code E01|12|cannot find symbol}
     * @return the line number, eg 12, or -1 if it is missing or not a number
     */
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

    /**
     * Gets the compiler's message from a compile error detail. Everything after the second bar
     * is kept, so a bar or new line inside the message is not lost.
     *
     * @param detail eg {@code E01|12|cannot find symbol}
     * @return the message, eg {@code cannot find symbol}, or an empty string if it is missing
     */
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

    /**
     * Gets what was run from a run detail.
     *
     * @param detail eg {@code Counter.increment | normal | 5}
     * @return the class and method, eg {@code Counter.increment}, the whole detail if it has
     *         no separator, or an empty string if it is null
     */
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

    /**
     * Gets how a run ended from a run detail.
     *
     * @param detail eg {@code Counter.increment | normal | 5}
     * @return {@code normal}, {@code exception}, {@code terminated}, or {@code unknown}
     *         if the status cannot be found
     */
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

    /**
     * Gets the returned value from a run detail. The run monitor only saves a result when it is
     * a simple value, not an object.
     *
     * @param detail eg {@code Counter.increment | normal | 5}
     * @return the result, eg {@code 5}, or an empty string if there is none
     */
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

    // renames

    /**
     * Gets the class's name before it was renamed.
     *
     * @param detail eg {@code Counter -> Tally}
     * @return the old name, eg {@code Counter}, or an empty string if it cannot be found
     */
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

    /**
     * Gets the class's new name after it was renamed.
     *
     * @param detail eg {@code Counter -> Tally}
     * @return the new name, eg {@code Tally}, the whole detail if it has no arrow,
     *         or an empty string if it is null
     */
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