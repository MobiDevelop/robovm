package org.robovm.debugger.state.classdata;

import org.robovm.debugger.utils.Converter;
import org.robovm.debugger.utils.bytebuffer.ByteBufferMemoryReader;

/**
 * @author Demyan Kimitsa
 * This is java representation for FieldInfo struct from classinfo.h
 * it is being parsed right from application
 * class info hash is produced in Linker.java
 */
public class FieldInfo extends BaseModifiersInfo {
    //typedef struct {
    //    jint flags;
    //    jint access;
    //    const char* name;
    //    const char* desc;
    //    void* attributes;
    //    jint offset;
    //} FieldInfo;


    private int flags;
    private String name;
    private String signature;
    private int offset;
    private ClassInfo typeInfo;

    public void readFieldInfo(ByteBufferMemoryReader reader) {
        flags = reader.readInt16();

        name = reader.readStringZAtPtr(reader.readPointer());

        if ((flags >> 12) != 0) {
            switch ((flags >> 12) & 0xf) {
                case ClassDataConsts.desc.B: signature = "B"; break;
                case ClassDataConsts.desc.C: signature = "C"; break;
                case ClassDataConsts.desc.D: signature = "D"; break;
                case ClassDataConsts.desc.F: signature = "F"; break;
                case ClassDataConsts.desc.I: signature = "I"; break;
                case ClassDataConsts.desc.J: signature = "J"; break;
                case ClassDataConsts.desc.S: signature = "S"; break;
                case ClassDataConsts.desc.Z: signature = "Z"; break;
            }
        } else {
            signature = reader.readStringZAtPtr(reader.readPointer());
        }

        offset = reader.readInt32();

        if ((flags & ClassDataConsts.fieldinfo.ATTRIBUTES) != 0) {
            // TODO: skip attributes for now
            reader.skip(reader.pointerSize());
        }
    }

    public String name() {
        return name;
    }

    public String signature() {
        return signature;
    }

    public int offset() {
        return offset;
    }

    public ClassInfo typeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(ClassInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    @Override
    protected int convertModifiers() {
        return Converter.fieldModifiers(flags);
    }
}
