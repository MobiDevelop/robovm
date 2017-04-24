package org.robovm.debugger.jdwp.handlers.eventrequest.events.predicates;

import org.robovm.debugger.jdwp.handlers.eventrequest.events.EventData;

/**
 * @author Demyan Kimitsa
 * Predicate for stepping condition
 */
public class EventStepModPredicate extends EventPredicate {
    private final long threadId;
    private final int size;
    private final int depth;

    public EventStepModPredicate(int modifier, long threadId, int size, int depth) {
        super(modifier);
        this.threadId = threadId;
        this.size = size;
        this.depth = depth;
    }

    public long threadId() {
        return threadId;
    }

    @Override
    public boolean test(EventData eventData) {
        // TODO: implement !
        return false;
    }

    @Override
    public String toString() {
        return "step{" + "threadId=" + threadId + ", size=" + size + ", depth=" + depth + "} ";
    }
}
