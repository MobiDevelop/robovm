package org.robovm.debugger.jdwp.handlers.referencetype;

import org.robovm.debugger.jdwp.JdwpConsts;
import org.robovm.debugger.jdwp.protocol.IJdwpRequestHandler;
import org.robovm.debugger.state.VmDebuggerState;
import org.robovm.debugger.state.classdata.ClassInfo;
import org.robovm.debugger.state.classdata.FieldInfo;
import org.robovm.debugger.utils.bytebuffer.ByteBufferPacket;

/**
 * @author Demyan Kimitsa
 * Returns information for each field in a reference type. Inherited fields are not included. The field list will
 * include any synthetic fields created by the compiler. Fields are returned in the order they occur in the class file.
 */
public class JdwpRefTypeFieldsHandler implements IJdwpRequestHandler {

    private final VmDebuggerState state;

    public JdwpRefTypeFieldsHandler(VmDebuggerState state) {
        this.state = state;
    }

    @Override
    public short handle(ByteBufferPacket payload, ByteBufferPacket output) {
        long referenceTypeID = payload.readLong();

        synchronized (state.centralLock()) {
            ClassInfo classInfo = state.classInfoLoader().classInfoByRefId(referenceTypeID);
            if (classInfo == null)
                return JdwpConsts.Error.INVALID_OBJECT;
            FieldInfo[] fields = state.classInfoLoader().classFields(classInfo);

            output.writeInt32(fields.length);
            for (FieldInfo fieldInfo : fields) {
                output.writeLong(fieldInfo.refId());
                output.writeStringWithLen(fieldInfo.name());
                output.writeStringWithLen(fieldInfo.signature());
                String signatureWithGeneric = getGenericSignature();
                if (signatureWithGeneric != null)
                    output.writeStringWithLen(signatureWithGeneric);
                output.writeInt32(fieldInfo.modifiers());
            }
        }

        return JdwpConsts.Error.NONE;
    }

    protected String getGenericSignature() {
        // return null as there is no generic signature is expected in this handler
        return null;
    }

    @Override
    public byte getCommandSet() {
        return 2;
    }

    @Override
    public byte getCommand() {
        return 4;
    }

    @Override
    public String toString() {
        return "ReferenceType(2).Fields(4)";
    }
}
