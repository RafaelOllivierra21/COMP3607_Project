package comp3607.telemetry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import bluej.debugmgr.ExecutionEvent;
import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.pkgmgr.Package;

public class RunMonitorTest {

    private static ExecutionEvent createStubExecutionEvent() {
        ExecutionEvent ex = new ExecutionEvent((Package) null);
        ex.setResult(ExecutionEvent.Result.values()[0]);
        return ex;
    }

    private static class StubInvocationEvent extends InvocationFinishedEvent {
        private final EventType eventType;
        private final String className;
        private final String methodName;
        private final Object result;

        StubInvocationEvent(EventType eventType, String className, String methodName, Object result) {
            super(createStubExecutionEvent());
            this.eventType = eventType;
            this.className = className;
            this.methodName = methodName;
            this.result = result;
        }

        @Override public EventType getEventType() { return eventType; }
        @Override public String getClassName() { return className; }
        @Override public String getMethodName() { return methodName; }
        @Override public Object getResult() { return result; }
    }

    @Test
    void testAllInvocationExitStatuses() {
        EventBuffer buffer = new EventBuffer();
        RunMonitor monitor = new RunMonitor(buffer);

        monitor.invocationFinished(new StubInvocationEvent(
            InvocationFinishedEvent.EventType.NORMAL_EXIT, "Calc", "add", 10));
        monitor.invocationFinished(new StubInvocationEvent(
            InvocationFinishedEvent.EventType.EXCEPTION_EXIT, "Calc", "divide", null));
        monitor.invocationFinished(new StubInvocationEvent(
            InvocationFinishedEvent.EventType.TERMINATED_EXIT, "Calc", null, null));

        List<TelemetryEvent> events = buffer.getEvents();
        assertEquals(3, events.size());
        assertEquals("Calc.add | normal | 10", events.get(0).getDetail());
        assertEquals("Calc.divide | exception | ", events.get(1).getDetail());
        assertEquals("Calc.unknown | terminated | ", events.get(2).getDetail());
    }
}