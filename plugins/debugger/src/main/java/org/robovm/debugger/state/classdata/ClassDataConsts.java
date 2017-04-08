package org.robovm.debugger.state.classdata;

/**
 * @author Demyan Kimitsa
 *         contains constants from classinfo.h that are used to manipulate binary data
 */
public class ClassDataConsts {

    public static final class classinfo {
        public static final int PUBLIC = 0x1;
        public static final int FINAL = 0x2;
        public static final int INTERFACE = 0x4;
        public static final int ABSTRACT = 0x8;
        public static final int SYNTHETIC = 0x10;
        public static final int ANNOTATION = 0x20;
        public static final int ENUM = 0x40;
        public static final int ATTRIBUTES = 0x80;
        public static final int ERROR = 0x100;
        public static final int INITIALIZED = 0x200;
        public static final int FINALIZABLE = 0x400;
    }

    public static final class fieldinfo {
        public static final int ACCESS_MASK = 0x3;
        public static final int PUBLIC = 0x1;
        public static final int PRIVATE = 0x2;
        public static final int PROTECTED = 0x3;
        public static final int STATIC = 0x4;
        public static final int FINAL = 0x8;
        public static final int VOLATILE = 0x10;
        public static final int TRANSIENT = 0x20;
        public static final int SYNTHETIC = 0x40;
        public static final int ENUM = 0x80;
        public static final int ATTRIBUTES = 0x100;
    }

    public static final class methodinfo {
        public static final int ACCESS_MASK = 0x3;
        public static final int PUBLIC = 0x1;
        public static final int PRIVATE = 0x2;
        public static final int PROTECTED = 0x3;
        public static final int STATIC = 0x4;
        public static final int FINAL = 0x8;
        public static final int SYNCHRONIZED = 0x10;
        public static final int BRIDGE = 0x20;
        public static final int VARARGS = 0x40;
        public static final int NATIVE = 0x80;
        public static final int ABSTRACT = 0x100;
        public static final int STRICT = 0x200;
        public static final int SYNTHETIC = 0x400;
        public static final int ATTRIBUTES = 0x800;
        public static final int BRO_BRIDGE = 0x1000;
        public static final int BRO_CALLBACK = 0x2000;
        public static final int COMPACT_DESC = 0x4000;
    }

    public static final class desc {
        // as described in classinfo.c
        public static final int B = 1;
        public static final int C = 2;
        public static final int D = 3;
        public static final int F = 4;
        public static final int I = 5;
        public static final int J = 6;
        public static final int S = 7;
        public static final int Z = 8;
        public static final int V = 9;
    }

    public static final class signatures {
        // class names that corresponds to JdwpConsts.tag
        // tag STRING = 115;
        public static final String JAVA_LANG_STRING = "Ljava/lang/String;";
        // tag THREAD = 116;
        public static final String JAVA_LANG_THREAD = "Ljava/lang/Thread;";
        // tag THREAD_GROUP = 103;
        public static final String JAVA_LANG_THREADGROUP = "Ljava/lang/ThreadGroup;";
        // tag CLASS_LOADER = 108;
        public static final String JAVA_LANG_CLASS_LOADER = "Ljava/lang/ClassLoader;";
        // tag CLASS_OBJECT = 99;
        public static final String JAVA_LANG_CLASS = "Ljava/lang/Class;";

    }

    public static final class fields {
        // java.lang.String
        public static final String JAVA_LANG_STRING_VALUE = "value";
        public static final String JAVA_LANG_STRING_OFFSET = "offset";
        public static final String JAVA_LANG_STRING_COUNT = "count";

        // java.lang.Thread
        public static final String JAVA_LANG_THREAD_NAME = "name";
        public static final String JAVA_LANG_THREAD_GROUP = "group";

        // java.lang.ThreadGroup
        public static final String JAVA_LANG_THREADGROUP_NAME = "name";
        public static final String JAVA_LANG_THREADGROUP_PARENT = "parent";
    }
}
