/*
 * Copyright (C) 2011 The Android Open Source Project
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

package dalvik.system;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.io.FileDescriptor;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Callbacks for socket assignment and reassignment.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public abstract class SocketTagger {

    private static SocketTagger tagger = new SocketTagger() {
        @Override public void tag(FileDescriptor socketDescriptor) throws SocketException {}
        @Override public void untag(FileDescriptor socketDescriptor) throws SocketException {}
    };

    @libcore.api.CorePlatformApi
    public SocketTagger() {
    }

    /**
     * Notified when {@code socketDescriptor} is either assigned to the current
     * thread. The socket is either newly connected or reused from a connection
     * pool. Implementations of this method should be thread-safe.
     */
    @libcore.api.CorePlatformApi
    public abstract void tag(FileDescriptor socketDescriptor) throws SocketException;

    /**
     * Notified when {@code socketDescriptor} is released from the current
     * thread to a connection pool. Implementations of this method should be
     * thread-safe.
     *
     * <p><strong>Note:</strong> this method will not be invoked when the socket
     * is closed.
     */
    @libcore.api.CorePlatformApi
    public abstract void untag(FileDescriptor socketDescriptor) throws SocketException;

    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public final void tag(Socket socket) throws SocketException {
        if (!socket.isClosed()) {
            tag(socket.getFileDescriptor$());
        }
    }

    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public final void untag(Socket socket) throws SocketException {
        if (!socket.isClosed()) {
            untag(socket.getFileDescriptor$());
        }
    }

    @libcore.api.CorePlatformApi
    public final void tag(DatagramSocket socket) throws SocketException {
        if (!socket.isClosed()) {
            tag(socket.getFileDescriptor$());
        }
    }

    @libcore.api.CorePlatformApi
    public final void untag(DatagramSocket socket) throws SocketException {
        if (!socket.isClosed()) {
            untag(socket.getFileDescriptor$());
        }
    }

    /**
     * Sets this process' socket tagger to {@code tagger}.
     */
    @libcore.api.CorePlatformApi
    public static synchronized void set(SocketTagger tagger) {
        if (tagger == null) {
            throw new NullPointerException("tagger == null");
        }
        SocketTagger.tagger = tagger;
    }

    /**
     * Returns this process socket tagger.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static synchronized SocketTagger get() {
        return tagger;
    }
}
