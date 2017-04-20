package org.robovm.debugger.jdwp.handlers.thread;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.protocol.IJdwpRequestHandler;
import org.robovm.debugger.state.VmDebuggerState;
import org.robovm.debugger.state.instances.VmStackTrace;
import org.robovm.debugger.state.instances.VmThread;
import org.robovm.debugger.utils.Converter;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

/**
 * @author Demyan Kimitsa
 */
public class JdwpThreadFramesHandler implements IJdwpRequestHandler {

    private final VmDebuggerState state;

    public JdwpThreadFramesHandler(VmDebuggerState state) {
        this.state = state;
    }

    @Override
    public short handle(ByteBufferPacket payload, ByteBufferPacket output) {
        long threadId = payload.readLong();
        int startFrame = payload.readInt32();
        int length = payload.readInt32();

        synchronized (state.centralLock()) {
            VmThread thread;
            try {
                thread = state.referenceRefIdHolder().instanceById(threadId);
            } catch (ClassCastException e){
                return JdwpConsts.Error.INVALID_THREAD;
            }

            if (thread == null)
                return JdwpConsts.Error.INVALID_THREAD;

            if (thread.suspendCount() == 0)
                return JdwpConsts.Error.THREAD_NOT_SUSPENDED;

            VmStackTrace[] stack = thread.stack();
            int lastFrame = length < 0 ? stack.length - 1 : startFrame + length - 1;
            if (startFrame > stack.length - 1 || lastFrame > stack.length - 1 || startFrame > lastFrame)
                return JdwpConsts.Error.INTERNAL;

            // frames The number of frames retreived
            output.writeInt32(lastFrame - startFrame + 1);

            // dump each frame location
            for (int i = startFrame; i <= lastFrame; i++) {
                VmStackTrace frame = stack[i];
                // frame id
                output.writeLong(frame.refId());
                // location
                output.writeByte(Converter.jdwpTypeTag(frame.classInfo()));
                output.writeLong(frame.classInfo().refId());
                output.writeLong(frame.methodInfo().refId());
                output.writeLong(frame.lineNumber());
            }
        }

        return JdwpConsts.Error.NONE;
    }

    @Override
    public byte getCommandSet() {
        return 11;
    }

    @Override
    public byte getCommand() {
        return 6;
    }

    @Override
    public String toString() {
        return "ThreadReference(11).Frames(6)";
    }

}
