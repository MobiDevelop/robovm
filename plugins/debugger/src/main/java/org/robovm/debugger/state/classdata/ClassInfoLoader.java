package org.robovm.debugger.state.classdata;

import org.robovm.debugger.DebuggerException;
import org.robovm.debugger.runtime.ValueManipulator;
import org.robovm.debugger.state.refid.RefIdHolder;
import org.robovm.debugger.utils.bytebuffer.ByteBufferMemoryReader;
import org.robovm.debugger.utils.macho.MachOException;
import org.robovm.debugger.utils.macho.MachOLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Demyan Kimitsa
 * Loads classes from application binary
 */
public class ClassInfoLoader {

    final ByteBufferMemoryReader reader;

    // signature to data info
    private final Map<String, ClassInfo> signatureToDataInfo = new HashMap<>();
    // class info address (as was read from mach-o) to class info
    private final Map<Long, ClassInfo> classInfoAddrToClassInfo = new HashMap<>();
    // contains array and classes
    private final List<ClassInfo> dataInfos;
    // matches Class object to class info (this object is received once class is loaded)
    private final Map<Long, ClassInfo> classObjAddrToDataInfo = new HashMap<>();

    // reference counters
    final RefIdHolder<ClassInfo> classRefIdHolder;
    final RefIdHolder<MethodInfo> methodsRefIdHolder;
    final RefIdHolder<FieldInfo> fieldRefIdHolder;


    public ClassInfoLoader(RefIdHolder<ClassInfo> classRefIdHolder, RefIdHolder<MethodInfo> methodsRefIdHolder,
                           RefIdHolder<FieldInfo> fieldRefIdHolder, ByteBufferMemoryReader reader,
                           long bcBootClassesHash, long bcClassesHash) {
        this.classRefIdHolder = classRefIdHolder;
        this.methodsRefIdHolder = methodsRefIdHolder;
        this.fieldRefIdHolder = fieldRefIdHolder;
        this.reader = reader;

        // parse
        parseHash(this.reader.setAddress(bcBootClassesHash).readPointer());
        parseHash(this.reader.setAddress(bcClassesHash).readPointer());


        // create flat list of classes
        dataInfos = Collections.unmodifiableList(new ArrayList<>(this.signatureToDataInfo.values()));
    }

    private void parseHash( long hash) {
        reader.setAddress(hash);
        long pointerSize = reader.pointerSize();
        int classInfoCount = reader.readInt32();
        int hashTableSize = reader.readInt32();

        // skip hash table entries end get base for ClassInfoHeaders
        // check @bc.c#getClassInfoBase for details
        long base = hash
                + 4 /* sizeof(uint32_t) count */
                + 4 /* sizeof(uint32_t) size */
                + (hashTableSize << 2)
                + 4 /* sizeof(uint32_t) this is for the last end index in the hash */;
        // Make sure base is properly aligned
        base = (base + pointerSize - 1) & ~(pointerSize - 1);
        for (int i = 0; i < classInfoCount; i++) {
            reader.setAddress(base);
            long classInfoPtr = reader.readPointer();
            reader.setAddress(classInfoPtr);

            ClassInfoImpl classInfo = new ClassInfoImpl();
            classInfo.readClassInfoHeader(reader);
            classRefIdHolder.addObject(classInfo);
            signatureToDataInfo.put(classInfo.signature(), classInfo);
            classInfoAddrToClassInfo.put(classInfoPtr, classInfo);
            base += pointerSize;
        }
    }

    public ClassInfo classInfoBySignature(String signature) {
        return signatureToDataInfo.get(signature);
    }

    public ClassInfo classRefId(long refId) {
        return this.classRefIdHolder.objectById(refId);
    }

    public ClassInfo classByClazzAddr(long classPointer) {
        return classObjAddrToDataInfo.get(classPointer);
    }

    public List<ClassInfo> classes() {
        return dataInfos;
    }

    public ClassInfo dataTypeInfoBySignature(String signature) {
        return signatureToDataInfo.get(signature);
    }



    /**
     * Notification that class was loaded in target
     * @param classInfoPtr pointer to class info structure (machO space )
     * @param clazzPtr pointer to loaded class object (runtime space) -- as there is a mix of primitive classes objects from
     *                 machO and array/object class objects from runtime
     */
    public ClassInfo onClassLoaded(long classInfoPtr, long clazzPtr) {
        // find class info by class its memory location
        ClassInfo classInfo = classInfoAddrToClassInfo.get(classInfoPtr);
        if (classInfo == null) {
            // TODO: warn
            throw new DebuggerException("TODO: unknown class info ptr!");
        }

        // set class info pointer
        classInfo.setClazzPtr(clazzPtr);
        classObjAddrToDataInfo.put(clazzPtr, classInfo);

        return classInfo;
    }

    /**
     * call from runtime class info loader once runtime class is resolved -- e.g. array or generic
     * @param classInfo that was build by runtime loader
     * @param clazzPtr pointer to clazz structure
     */
    public void registerRuntimeClassInfo(ClassInfo classInfo, long clazzPtr) {
        // attach ID to it
        classRefIdHolder.addObject(classInfo);
        signatureToDataInfo.put(classInfo.signature(), classInfo);
        // map pointer to class info
        classObjAddrToDataInfo.put(clazzPtr, classInfo);
    }

    public ClassInfo buildPrimitiveClassInfo(char signature, long clazzPtr) {
        // TODO: here will be more logic once there is field access is implemented
        String signatureStr = String.valueOf(signature);
        switch (signature) {
            case 'Z':
                // boolean
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 1, ValueManipulator.Boolean);

            case 'B':
                // byte
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 1, ValueManipulator.Byte);

            case 'C':
                // char
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 2, ValueManipulator.Character);

            case 'S':
                // short
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 2, ValueManipulator.Short);

            case 'I':
                // int
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 4, ValueManipulator.Integer);

            case 'J':
                // long
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 8, ValueManipulator.Long);

            case 'F':
                // float
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 4, ValueManipulator.Float);

            case 'D':
                // double
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 8, ValueManipulator.Double);

            case 'V':
                // void
                return new ClassInfoPrimitiveImpl(signatureStr, clazzPtr, 0, ValueManipulator.Void);
        }

        return null;
    }

    public FieldInfo[] classFields(ClassInfo classInfo) {
        return classInfo.fields(this);
    }

    public MethodInfo[] classMethods(ClassInfo classInfo) {
        return classInfo.methods(this);
    }


    public static void main(String[] argv) {
        // for debug purpose
        try {
            MachOLoader loader = new MachOLoader(new File(argv[0]), MachOLoader.cpuTypeFromString(argv[1]));
            long bcBootClassesHash = loader.resolveSymbol("_bcBootClassesHash");
            long bcClassesHash = loader.resolveSymbol("_bcClassesHash");
            ClassInfoLoader classInfoLoader = new ClassInfoLoader(
                    new RefIdHolder<>(RefIdHolder.RefIdType.CLASS_TYPE),
                    new RefIdHolder<>(RefIdHolder.RefIdType.METHOD_TYPE),
                    new RefIdHolder<>(RefIdHolder.RefIdType.FIELD_TYPE),
                    loader.readDataSegment(), bcBootClassesHash, bcClassesHash);
            for (ClassInfo info : classInfoLoader.dataInfos)
                ((ClassInfoImpl)info).loadData(classInfoLoader);
            System.out.println("Loaded " + classInfoLoader.signatureToDataInfo.size() + " classes");
        } catch (MachOException e) {
            e.printStackTrace();
        }

    }

}
