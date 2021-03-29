/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.net;

import android.system.ErrnoException;

import java.io.IOException;
import java.io.FileDescriptor;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import libcore.io.AsynchronousCloseMonitor;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Libcore;

import jdk.net.*;

import static android.system.OsConstants.AF_INET6;
import static android.system.OsConstants.AF_UNIX;
import static android.system.OsConstants.EAGAIN;
import static android.system.OsConstants.EBADF;
import static android.system.OsConstants.EINVAL;
import static android.system.OsConstants.MSG_OOB;
import static android.system.OsConstants.POLLERR;
import static android.system.OsConstants.POLLIN;
import static android.system.OsConstants.SOCK_DGRAM;
import static android.system.OsConstants.SOCK_STREAM;
import static android.system.OsConstants.SHUT_RDWR;
import static sun.net.ExtendedOptionsImpl.*;

// Android-changed: Rewritten to use android.system POSIX calls and assume AF_INET6.
/*
 * On Unix systems we simply delegate to native methods.
 *
 * @author Chris Hegarty
 */

class PlainSocketImpl extends AbstractPlainSocketImpl
{
    // Android-removed: Android doesn't need to call native initProto.
    /*
    static {
        initProto();
    }
    */

    /**
     * Constructs an empty instance.
     */
    PlainSocketImpl() {
        // Android-changed: Let PlainSocketImpl construct its own FileDescriptor.
        this.fd = new FileDescriptor();
    }

    /**
     * Constructs an instance with the given file descriptor.
     */
    // Android-removed: Let PlainSocketImpl construct its own FileDescriptor.
    /*
    PlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }
    */

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            super.setOption(name, value);
        } else {
            if (isClosedOrPending()) {
                throw new SocketException("Socket closed");
            }
            checkSetOptionPermission(name);
            checkValueType(value, SocketFlow.class);
            setFlowOption(getFileDescriptor(), (SocketFlow)value);
        }
    }

    protected <T> T getOption(SocketOption<T> name) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            return super.getOption(name);
        }
        if (isClosedOrPending()) {
            throw new SocketException("Socket closed");
        }
        checkGetOptionPermission(name);
        SocketFlow flow = SocketFlow.create();
        getFlowOption(getFileDescriptor(), flow);
        return (T)flow;
    }

    // BEGIN Android-changed: Rewrote on top of Libcore.io.
    protected void socketSetOption(int opt, Object val) throws SocketException {
        try {
            socketSetOption0(opt, val);
        } catch (SocketException se) {
            if (socket == null || !socket.isConnected())
                throw se;
        }
    }

    void socketCreate(boolean isStream) throws IOException {
        // The fd object must not change after calling bind, because we rely on this undocumented
        // behaviour. See libcore.java.net.SocketTest#testFileDescriptorStaysSame.
        fd.setInt$(IoBridge.socket(AF_INET6, isStream ? SOCK_STREAM : SOCK_DGRAM, 0).getInt$());
        IoUtils.setFdOwner(fd, this);

        if (serverSocket != null) {
            IoUtils.setBlocking(fd, false);
            IoBridge.setSocketOption(fd, SO_REUSEADDR, true);
        }
    }

    void socketConnect(InetAddress address, int port, int timeout) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.connect(fd, address, port, timeout);

        this.address = address;
        this.port = port;

        if (localport == 0) {
            // If socket is pending close, fd becomes an AF_UNIX socket and calling
            // getLocalInetSocketAddress will fail.
            // http://b/34645743
            if (!isClosedOrPending()) {
                localport = IoBridge.getLocalInetSocketAddress(fd).getPort();
            }
        }
    }

    void socketBind(InetAddress address, int port) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.bind(fd, address, port);

        this.address = address;
        if (port == 0) {
            // Now that we're a connected socket, let's extract the port number that the system
            // chose for us and store it in the Socket object.
            localport = IoBridge.getLocalInetSocketAddress(fd).getPort();
        } else {
            localport = port;
        }
    }

    void socketListen(int count) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("Socket closed");
        }

        try {
            Libcore.os.listen(fd, count);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    void socketAccept(SocketImpl s) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("Socket closed");
        }

        // poll() with a timeout of 0 means "poll for zero millis", but a Socket timeout == 0 means
        // "wait forever". When timeout == 0 we pass -1 to poll.
        if (timeout <= 0) {
            IoBridge.poll(fd, POLLIN | POLLERR, -1);
        } else {
            IoBridge.poll(fd, POLLIN | POLLERR, timeout);
        }

        InetSocketAddress peerAddress = new InetSocketAddress();
        try {
            FileDescriptor newfd = Libcore.os.accept(fd, peerAddress);

            s.fd.setInt$(newfd.getInt$());
            IoUtils.setFdOwner(s.fd, s);
            s.address = peerAddress.getAddress();
            s.port = peerAddress.getPort();
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == EAGAIN) {
                SocketTimeoutException e = new SocketTimeoutException();
                e.initCause(errnoException);
                throw e;
            } else if (errnoException.errno == EINVAL || errnoException.errno == EBADF) {
                throw new SocketException("Socket closed");
            }
            errnoException.rethrowAsSocketException();
        }

        s.localport = IoBridge.getLocalInetSocketAddress(s.fd).getPort();
    }

    int socketAvailable() throws IOException {
        return IoBridge.available(fd);
    }

    void socketClose0(boolean useDeferredClose) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("socket already closed");
        }

        FileDescriptor markerFD = null;
        if (useDeferredClose) {
            markerFD = getMarkerFD();
        }

        if (useDeferredClose && markerFD != null) {
            try {
                Libcore.os.dup2(markerFD, fd.getInt$());
                Libcore.os.close(markerFD);

                // This effectively closes the socket, needs to signal threads that blocks on this
                // file descriptor.
                AsynchronousCloseMonitor.signalBlockedThreads(fd);
            } catch (ErrnoException errnoException) {
                // close should not throw
            }
        } else {
            // If requested or a markerFD cannot be created, a non-deferred close is performed
            // instead.
            IoBridge.closeAndSignalBlockedThreads(fd);
        }
    }

    /*
     * Create the marker file descriptor by establishing a loopback connection which we shutdown but
     * do not close the fd. The result is an fd that can be used for read/write.
     *
     * The purpose is to keep hold of the raw fd handle until we are sure it is not used in any
     * thread. Otherwise if we close the file descriptor directly, the system might reuse the raw fd
     * number and threads holding old fd value might behave incorrectly.
     */
    private FileDescriptor getMarkerFD() throws SocketException {
        FileDescriptor fd1 = new FileDescriptor();
        FileDescriptor fd2 = new FileDescriptor();
        try {
            Libcore.os.socketpair(AF_UNIX, SOCK_STREAM, 0, fd1, fd2);

            // Shutdown fd1, any reads to this fd will get EOF; any writes will get an error.
            Libcore.os.shutdown(fd1, SHUT_RDWR);
            Libcore.os.close(fd2);
        } catch (ErrnoException errnoException) {
            // We might have reached the maximum file descriptor number and socketpair(2) would
            // fail. In this case, return null and let caller to fall back to an alternative method
            // that does not allocate more file descriptors.
            return null;
        }
        return fd1;
    }

    void socketShutdown(int howto) throws IOException {
        try {
            Libcore.os.shutdown(fd, howto);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    void socketSetOption0(int cmd, Object value) throws SocketException {
        // OpenJDK does not set SO_TIMEOUT on Linux.
        if (cmd == SO_TIMEOUT) {
            return;
        }

        IoBridge.setSocketOption(fd, cmd, value);
    }

    Object socketGetOption(int opt) throws SocketException {
        return IoBridge.getSocketOption(fd, opt);
    }

    void socketSendUrgentData(int data) throws IOException {
        if (fd == null || !fd.valid()) {
            throw new SocketException("Socket closed");
        }

        try {
            byte[] buffer = new byte[] { (byte) data };
            Libcore.os.sendto(fd, buffer, 0, 1, MSG_OOB, null, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }
    // END Android-changed: Rewrote on top of Libcore.io.

}
