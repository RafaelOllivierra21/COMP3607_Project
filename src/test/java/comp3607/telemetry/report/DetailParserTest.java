package comp3607.telemetry.report;

import comp3607.telemetry.ErrorCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DetailParserTest {

    @Test
    void testParseCompileErrorDetails() {
        String detail = "E01|12|cannot find symbol: variable count";
        assertEquals("E01", DetailParser.errorCode(detail));
        assertEquals(12, DetailParser.errorLine(detail));
        assertEquals("cannot find symbol: variable count", DetailParser.errorMessage(detail));
    }

    @Test
    void testParseInvalidCompileErrorDetails() {
        assertEquals(ErrorCodes.OTHER, DetailParser.errorCode(null));
        assertEquals(-1, DetailParser.errorLine("E01|notANumber|msg"));
        assertEquals("", DetailParser.errorMessage("malformed"));
    }

    @Test
    void testParseRunDetails() {
        String detail = "Counter.increment | normal | 5";
        assertEquals("Counter.increment", DetailParser.runTarget(detail));
        assertEquals("normal", DetailParser.runStatus(detail));
        assertEquals("5", DetailParser.runResult(detail));
        assertEquals("unknown", DetailParser.runStatus("bad_format"));
    }

    @Test
    void testParseRenameDetails() {
        String detail = "Counter -> Tally";
        assertEquals("Counter", DetailParser.oldName(detail));
        assertEquals("Tally", DetailParser.newName(detail));
        assertEquals("", DetailParser.oldName(null));
    }
}