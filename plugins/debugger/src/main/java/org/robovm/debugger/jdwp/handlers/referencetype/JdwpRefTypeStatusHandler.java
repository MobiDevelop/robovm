package org.robovm.debugger.jdwp.handlers.referencetype;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.protocol.IJdwpRequestHandler;
import org.robovm.debugger.state.VmDebuggerState;
import org.robovm.debugger.state.classdata.ClassInfo;
import org.robovm.debugger.utils.Converter;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

/**
 * @author Demyan Kimitsa
 * Returns the current status of the reference type.
 */
public class JdwpRefTypeStatusHandler implements IJdwpRequestHandler {
    private final VmDebuggerState state;

    public JdwpRefTypeStatusHandler(VmDebuggerState state) {
        this.state = state;
    }

    @Override
    public short handle(ByteBufferPacket payload, ByteBufferPacket output) {
        long refTypeId = payload.readLong();

        synchronized (state.centralLock()) {
            ClassInfo ci = state.classRefIdHolder().objectById(refTypeId);
            if (ci == null)
                return JdwpConsts.Error.INVALID_OBJECT;
            output.writeInt32(Converter.jdwpClassStatus(ci));
        }

        return JdwpConsts.Error.NONE;
    }

    @Override
    public byte getCommandSet() {
        return 2;
    }

    @Override
    public byte getCommand() {
        return 9;
    }

    @Override
    public String toString() {
        return "ReferenceType(2).Status(9)";
    }

}