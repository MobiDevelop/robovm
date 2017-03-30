package org.robovm.debugger.jdwp.handlers.vm;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.protocol.IJdwpRequestHandler;
import org.robovm.debugger.state.VmDebuggerState;
import org.robovm.debugger.state.classdata.ClassInfo;
import org.robovm.debugger.utils.Converter;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

import java.util.List;

/**
 * @author Demyan Kimitsa
 * Returns reference types for all classes currently loaded by the target VM. Both the JNI signature and the generic
 * signature are returned for each class
 */
public class JdwpVmAllClassesWithGenericsHandler implements IJdwpRequestHandler {

    private final VmDebuggerState state;

    public JdwpVmAllClassesWithGenericsHandler(VmDebuggerState state) {
        this.state = state;
    }

    @Override
    public short handle(ByteBufferPacket payload, ByteBufferPacket output) {

        synchronized (state.centralLock()) {
            List<ClassInfo> classes = this.state.classInfoLoader().classes();

            output.writeInt32(classes.size());
            for (ClassInfo classInfo : classes) {
                // refTypeTag
                output.writeByte(Converter.jdwpTypeTag(classInfo));
                // typeID
                output.writeLong(classInfo.getRefId());
                // signature
                output.writeStringWithLen(classInfo.getSignature());
                // genericSignature - The generic signature, an empty string if there is none.
                output.writeStringWithLen("");
                // status
                output.writeInt32(Converter.jdwpClassStatus(classInfo));
            }
        }

        return JdwpConsts.Error.NONE;
    }

    @Override
    public byte getCommandSet() {
        return 1;
    }

    @Override
    public byte getCommand() {
        return 20;
    }

    @Override
    public String toString() {
        return "VirtualMachine(1).AllClassesWithGeneric(20)";
    }
}
