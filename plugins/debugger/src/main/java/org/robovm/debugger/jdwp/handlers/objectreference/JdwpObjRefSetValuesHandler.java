package org.robovm.debugger.jdwp.handlers.objectreference;

import org.robovm.debugger.DebuggerException;
import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.protocol.IJdwpRequestHandler;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

/**
 * @author Demyan Kimitsa
 * Sets the value of one or more instance fields. Each field must be member of the object's type or one of its superclasses, superinterfaces, or implemented interface
 */
public class JdwpObjRefSetValuesHandler implements IJdwpRequestHandler {
    private final IJdwpInstanceDelegate delegate;

    public JdwpObjRefSetValuesHandler(IJdwpInstanceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public short handle(ByteBufferPacket payload, ByteBufferPacket output) {
        long objectId = payload.readLong();
        int count = payload.readInt32();


        // set values to target
        try {
            // all field ids and values are multiplexed in data flow, so it will be handled in delegate
            delegate.jdwpFieldSetValues(objectId, count, false, payload);
        } catch (DebuggerException e) {
            if (e.getCode() < 0)
                throw e;
            return (short) e.getCode();
        }

        return JdwpConsts.Error.NONE;
    }

    @Override
    public byte getCommandSet() {
        return 9;
    }

    @Override
    public byte getCommand() {
        return 3;
    }

    @Override
    public String toString() {
        return "ObjectReference(9).SetValues(3)";
    }

}
