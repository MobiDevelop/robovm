/*
 * Copyright (c) 2007,2011, Oracle and/or its affiliates. All rights reserved.
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
import android.system.StructGroupReq;

import java.io.IOException;
import libcore.io.IoBridge;
import libcore.io.Libcore;
import libcore.util.EmptyArray;

import jdk.net.*;

import static android.system.OsConstants.AF_INET6;
import static android.system.OsConstants.AF_UNSPEC;
import static android.system.OsConstants.IPPROTO_IP;
import static android.system.OsConstants.IP_MULTICAST_ALL;
import static android.system.OsConstants.MSG_PEEK;
import static android.system.OsConstants.POLLERR;
import static android.system.OsConstants.POLLIN;
import static android.system.OsConstants.SOCK_DGRAM;
import static libcore.io.IoBridge.JAVA_IP_MULTICAST_TTL;
import static libcore.io.IoBridge.JAVA_MCAST_JOIN_GROUP;
import static libcore.io.IoBridge.JAVA_MCAST_LEAVE_GROUP;
import static sun.net.ExtendedOptionsImpl.*;

/*
 * On Unix systems we simply delegate to native methods.
 *
 * @author Chris Hegarty
 */

class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    // Android-removed: init method has been removed
    // static {
    //     init();
    // }

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            super.setOption(name, value);
        } else {
            if (isClosed()) {
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
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        checkGetOptionPermission(name);
        SocketFlow flow = SocketFlow.create();
        getFlowOption(getFileDescriptor(), flow);
        return (T)flow;
    }

    protected void socketSetOption(int opt, Object val) throws SocketException {
        try {
            socketSetOption0(opt, val);
        } catch (SocketException se) {
            if (!connected)
                throw se;
        }
    }

    // BEGIN Android-changed: Rewrote on top of Libcore.io.
    protected synchronized void bind0(int lport, InetAddress laddr) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.bind(fd, laddr, lport);

        if (lport == 0) {
            // Now that we're a connected socket, let's extract the port number that the system
            // chose for us and store it in the Socket object.
            localPort = IoBridge.getLocalInetSocketAddress(fd).getPort();
        } else {
            localPort = lport;
        }
    }

    protected void send(DatagramPacket p) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        if (p.getData() == null || p.getAddress() == null) {
            throw new NullPointerException("null buffer || null address");
        }

        int port = connected ? 0 : p.getPort();
        InetAddress address = connected ? null : p.getAddress();
        IoBridge.sendto(fd, p.getData(), p.getOffset(), p.getLength(), 0, address, port);
    }

    protected synchronized int peek(InetAddress i) throws IOException {
        DatagramPacket p = new DatagramPacket(EmptyArray.BYTE, 0);
        doRecv(p, MSG_PEEK);
        i.holder().address = p.getAddress().holder().address;
        return p.getPort();
    }

    protected synchronized int peekData(DatagramPacket p) throws IOException {
        doRecv(p, MSG_PEEK);
        return p.getPort();
    }

    protected synchronized void receive0(DatagramPacket p) throws IOException {
        doRecv(p, 0);
    }

    private void doRecv(DatagramPacket p, int flags) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        if (timeout != 0) {
            IoBridge.poll(fd, POLLIN | POLLERR, timeout);
        }

        IoBridge.recvfrom(false, fd, p.getData(), p.getOffset(), p.bufLength, flags, p,
                connected);
    }

    protected void setTimeToLive(int ttl) throws IOException {
        IoBridge.setSocketOption(fd, JAVA_IP_MULTICAST_TTL, ttl);
    }

    protected int getTimeToLive() throws IOException {
        return (Integer) IoBridge.getSocketOption(fd, JAVA_IP_MULTICAST_TTL);
    }

    protected void setTTL(byte ttl) throws IOException {
        setTimeToLive((int) ttl & 0xff);
    }

    protected byte getTTL() throws IOException {
        return (byte) getTimeToLive();
    }

    private static StructGroupReq makeGroupReq(InetAddress gr_group,
            NetworkInterface networkInterface) {
        int gr_interface = (networkInterface != null) ? networkInterface.getIndex() : 0;
        return new StructGroupReq(gr_interface, gr_group);
    }

    protected void join(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.setSocketOption(fd, JAVA_MCAST_JOIN_GROUP, makeGroupReq(inetaddr, netIf));
    }

    protected void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.setSocketOption(fd, JAVA_MCAST_LEAVE_GROUP, makeGroupReq(inetaddr, netIf));
    }

    protected void datagramSocketCreate() throws SocketException {
        fd = IoBridge.socket(AF_INET6, SOCK_DGRAM, 0);
        IoBridge.setSocketOption(fd, SO_BROADCAST, true);

        try {
            Libcore.os.setsockoptInt(fd, IPPROTO_IP, IP_MULTICAST_ALL, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    protected void datagramSocketClose() {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException ignored) { }
    }

    protected void socketSetOption0(int opt, Object val) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.setSocketOption(fd, opt, val);
    }

    protected Object socketGetOption(int opt) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        return IoBridge.getSocketOption(fd, opt);
    }

    protected void connect0(InetAddress address, int port) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }

        IoBridge.connect(fd, address, port);
    }

    protected void disconnect0(int family) {
        if (isClosed()) {
            return;
        }

        InetAddress inetAddressUnspec = new InetAddress();
        inetAddressUnspec.holder().family = AF_UNSPEC;

        try {
            IoBridge.connect(fd, inetAddressUnspec, 0);
        } catch (SocketException ignored) { }
    }
    // END Android-changed: Rewrote on top of Libcore.io.

    // Android-removed: JNI has been removed
    // /**
    //  * Perform class load-time initializations.
    //  */
    // private native static void init();
}
