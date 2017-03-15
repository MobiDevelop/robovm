package org.robovm.debugger.execution;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.vo.JdwpEventRequest;
import org.robovm.debugger.utils.DbgLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Demyan Kimitsa
 * Combines information received from JDWP and Hooks interface and controlls steps/breakpoints/events
 */
public class ExecutionControlCenter {
    private final DbgLogger log = DbgLogger.get(this.getClass().getSimpleName());

    // counter of request set
    private int eventRequestCounter = 100;

    // list of active event requests
    private List<JdwpEventRequest> eventRequests = new ArrayList<>();


    /**
     * set event request
     * TODO: this method is called in context of JDWP server receiving thread. add synchronization to
     * elements once required
     */
    public int jdwpSetEventRequest(byte eventKind, byte suspendPolicy, long threadID, int caseCount, Set<Long> referenceTypeIDs,
                                   List<String> classMatchPatterns, List<String> classExcludePatterns,
                                   List<JdwpEventRequest.ExceptionMod> exceptions, List<JdwpEventRequest.LocationMod> locations,
                                   Set<Long> instancesIDs, JdwpEventRequest.StepMod stepMod) {
        // TODO: need to validate all refTypes against data in state once it is there
        // meanwhile just adding items to the list
        int requestId = eventRequestCounter += 1;
        JdwpEventRequest request = new JdwpEventRequest(requestId, eventKind, suspendPolicy, threadID, caseCount, referenceTypeIDs,
                classMatchPatterns, classExcludePatterns, exceptions, locations, instancesIDs, stepMod);

        log.debug("jdwpSetEventRequest: " + request);
        eventRequests.add(request);

        return requestId;
    }


    /**
     * Clear previously set event request
     *
     * @param eventKind Event kind to clear
     * @param requestID ID of request to clear
     * @return JDWP error code
     * TODO: synchronization
     */
    public short jdwpClearEventRequest(byte eventKind, int requestID) {
        Iterator<JdwpEventRequest> it = eventRequests.iterator();
        while (it.hasNext()) {
            JdwpEventRequest req = it.next();
            if (req.requestId == requestID) {
                if (req.eventKind != eventKind)
                    return JdwpConsts.Error.INVALID_EVENT_TYPE;
                // TODO: perform device side removal of breakpoint etc
                it.remove();

                return JdwpConsts.Error.NONE;
            }
        }

        // not found
        return JdwpConsts.Error.INVALID_EVENT_TYPE;
    }

    /**
     * remove all breakpoint event types
     * TODO: synchronization
     */
    public void jdwpClearAllBreakpoints() {
        Iterator<JdwpEventRequest> it = eventRequests.iterator();
        while (it.hasNext()) {
            JdwpEventRequest req = it.next();
            if (req.eventKind == JdwpConsts.EventKind.BREAKPOINT) {
                it.remove();
                // TODO: perform device side removal of breakpoint
            }
        }
    }
}
