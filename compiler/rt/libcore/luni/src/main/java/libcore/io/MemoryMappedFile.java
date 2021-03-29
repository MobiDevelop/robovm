/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import android.system.ErrnoException;
import dalvik.annotation.compat.UnsupportedAppUsage;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.NioUtils;
import libcore.io.Libcore;
import libcore.io.Memory;
import static android.system.OsConstants.*;

/**
 * A memory-mapped file. Use {@link #mmapRO} to map a file, {@link #close} to unmap a file,
 * and either {@link #bigEndianIterator} or {@link #littleEndianIterator} to get a seekable
 * {@link BufferIterator} over the mapped data. This class is not thread safe.
 */
public final class MemoryMappedFile implements AutoCloseable {
    private boolean closed;
    private final long address;
    private final int size;

    /** Public for layoutlib only. */
    public MemoryMappedFile(long address, long size) {
        this.address = address;
        // For simplicity when bounds checking, only sizes up to Integer.MAX_VALUE are supported.
        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Unsupported file size=" + size);
        }
        this.size = (int) size;
    }

    /**
     * Use this to mmap the whole file read-only.
     */
    @UnsupportedAppUsage
    public static MemoryMappedFile mmapRO(String path) throws ErrnoException {
        FileDescriptor fd = Libcore.os.open(path, O_RDONLY, 0);
        try {
            long size = Libcore.os.fstat(fd).st_size;
            long address = Libcore.os.mmap(0L, size, PROT_READ, MAP_SHARED, fd, 0);
            return new MemoryMappedFile(address, size);
        } finally {
            Libcore.os.close(fd);
        }
    }

    /**
     * Unmaps this memory-mapped file using munmap(2). This is a no-op if close has already been
     * called. Note that this class does <i>not</i> use finalization; you must call {@code close}
     * yourself.
     *
     * Calling this method invalidates any iterators over this {@code MemoryMappedFile}. It is an
     * error to use such an iterator after calling {@code close}.
     */
    public void close() throws ErrnoException {
        if (!closed) {
            closed = true;
            Libcore.os.munmap(address, size);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns a new iterator that treats the mapped data as big-endian.
     */
    @UnsupportedAppUsage
    public BufferIterator bigEndianIterator() {
        return new NioBufferIterator(
                this, address, size, ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
    }

    /**
     * Returns a new iterator that treats the mapped data as little-endian.
     */
    public BufferIterator littleEndianIterator() {
        return new NioBufferIterator(
                this, this.address, this.size, ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN);
    }

    /** Throws {@link IllegalStateException} if the file is closed. */
    void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("MemoryMappedFile is closed");
        }
    }

    /**
     * Returns the size in bytes of the memory-mapped region.
     */
    public int size() {
        checkNotClosed();
        return size;
    }
}
