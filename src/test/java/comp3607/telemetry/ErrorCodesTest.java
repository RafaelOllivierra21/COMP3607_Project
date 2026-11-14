package comp3607.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorCodesTest {

    @Test
    void testKnownErrorMessagesMapToCodes() {
        assertEquals("E01", ErrorCodes.codeFor("cannot find symbol: variable x"));
        assertEquals("E02", ErrorCodes.codeFor("';' expected"));
        assertEquals("E04", ErrorCodes.codeFor("incompatible types: String cannot be converted to int"));
        assertEquals("E05", ErrorCodes.codeFor("missing return statement"));
    }

    @Test
    void testUnknownOrNullMessageReturnsOther() {
        assertEquals("E99", ErrorCodes.codeFor(null));
        assertEquals("E99", ErrorCodes.codeFor("some completely random compiler warning"));
    }

    @Test
    void testLabelLookup() {
        assertEquals("cannot find symbol", ErrorCodes.labelFor("E01"));
        assertEquals("missing semicolon", ErrorCodes.labelFor("E02"));
        assertEquals("other error", ErrorCodes.labelFor("E99"));
        assertEquals("unknown", ErrorCodes.labelFor("INVALID_CODE"));
    }
}