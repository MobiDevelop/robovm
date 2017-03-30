package org.robovm.debugger.jdwp.events;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.state.instances.VmInstance;
import org.robovm.debugger.state.instances.VmStackTrace;
import org.robovm.debugger.state.instances.VmThread;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

/**
 * @author Demyan Kimitsa
 * Event data of stopped thread (step/breakpoint)
 */
public class JdwpThreadStoppedEventData extends JdwpEventData {

    private final VmStackTrace location;
    private final VmInstance exception;

    public JdwpThreadStoppedEventData(byte eventKind, VmThread thread, VmStackTrace location, VmInstance exception) {
        super(eventKind, thread);
        this.location = location;
        this.exception = exception;
    }

    @Override
    protected void dumpCustomData(ByteBufferPacket packet) {
        // write location
        // TODO: is other types possible like array ?
        packet.writeByte(JdwpConsts.Tag.OBJECT);
        packet.writeLong(location.classInfo().getRefId());
        packet.writeLong(location.methodInfo().getRefId());
        packet.writeLong(location.lineNumber());

        // exception
        if (exception != null) {
            packet.writeByte(JdwpConsts.Tag.OBJECT);
            packet.writeLong(exception.getRefId());

            // exception location
            // TODO: check if it is valid
            packet.writeLong(location.classInfo().getRefId());
            packet.writeLong(location.methodInfo().getRefId());
            packet.writeLong(location.lineNumber());
        }
    }

    @Override
    public boolean cancelIfNotHandled() {
        // don't stop application if there is no interested instance in it
        return true;
    }
}
