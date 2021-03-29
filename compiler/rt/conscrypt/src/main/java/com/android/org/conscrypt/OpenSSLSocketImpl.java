/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.org.conscrypt;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.PrivateKey;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Public shim allowing us to stay backward-compatible with legacy applications which were using
 * Conscrypt's extended socket API before the introduction of the {@link Conscrypt} class.
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
@Internal
public abstract class OpenSSLSocketImpl extends AbstractConscryptSocket {
    OpenSSLSocketImpl() throws IOException {
    }

    OpenSSLSocketImpl(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    OpenSSLSocketImpl(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    OpenSSLSocketImpl(String hostname, int port, InetAddress clientAddress, int clientPort)
        throws IOException {
        super(hostname, port, clientAddress, clientPort);
    }

    OpenSSLSocketImpl(InetAddress address, int port, InetAddress clientAddress,
        int clientPort)
        throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    OpenSSLSocketImpl(Socket socket, String hostname, int port, boolean autoClose)
        throws IOException {
        super(socket, hostname, port, autoClose);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    public String getHostname() {
        return super.getHostname();
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    public void setHostname(String hostname) {
        super.setHostname(hostname);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    public String getHostnameOrIP() {
        return super.getHostnameOrIP();
    }

    @Override
    public FileDescriptor getFileDescriptor$() {
        return super.getFileDescriptor$();
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        super.setSoWriteTimeout(writeTimeoutMilliseconds);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    public int getSoWriteTimeout() throws SocketException {
        return super.getSoWriteTimeout();
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        super.setHandshakeTimeout(handshakeTimeoutMilliseconds);
    }

    @Override
    public abstract SSLSession getHandshakeSession();

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    public abstract void setUseSessionTickets(boolean useSessionTickets);

    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    public abstract void setChannelIdEnabled(boolean enabled);

    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    public abstract byte[] getChannelId() throws SSLException;

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    public abstract void setChannelIdPrivateKey(PrivateKey privateKey);

    /**
     * @deprecated NPN is not supported
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    @Deprecated
    public final byte[] getNpnSelectedProtocol() {
        return super.getNpnSelectedProtocol();
    }

    /**
     * @deprecated NPN is not supported
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    @Deprecated
    public final void setNpnProtocols(byte[] npnProtocols) {
        super.setNpnProtocols(npnProtocols);
    }

    /**
     * @deprecated use {@link #setApplicationProtocols(String[])} instead.
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @Override
    @Deprecated
    public final void setAlpnProtocols(String[] alpnProtocols) {
        setApplicationProtocols(alpnProtocols == null ? EmptyArray.STRING : alpnProtocols);
    }

    /**
     * @deprecated use {@link #getApplicationProtocol()} instead.
     */
    @dalvik.annotation.
    compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    @Deprecated
    public final byte[] getAlpnSelectedProtocol() {
        return SSLUtils.toProtocolBytes(getApplicationProtocol());
    }

    /**
     * @deprecated Use {@link #setAlpnProtocols(String[])} instead.
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @Override
    @Deprecated
    public final void setAlpnProtocols(byte[] protocols) {
        setApplicationProtocols(SSLUtils.decodeProtocols(protocols == null ? EmptyArray.BYTE : protocols));
    }
}
