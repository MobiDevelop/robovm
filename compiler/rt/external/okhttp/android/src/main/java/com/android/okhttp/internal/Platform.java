/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.okhttp.internal;

import android.net.ssl.SSLSockets;
import com.android.okhttp.Protocol;
import com.android.okhttp.internal.tls.RealTrustRootIndex;
import com.android.okhttp.internal.tls.TrustRootIndex;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import dalvik.system.SocketTagger;
import com.android.okhttp.okio.Buffer;

/**
 * Access to proprietary Android APIs. Avoids use of reflection where possible.
 * @hide This class is not part of the Android public SDK API
 */
// only tests should extend this class
public class Platform {
    private static final AtomicReference<Platform> INSTANCE_HOLDER
            = new AtomicReference<>(new Platform());

    // only for private use and in tests
    protected Platform() {
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    public static Platform get() {
        return INSTANCE_HOLDER.get();
    }

    /**
     * Atomically replaces the Platform instance returned by future
     * invocations of {@link #get()}, for use in tests.
     * Invocations of this method should typically be followed by
     * a try/finally block to reset the previous value:
     *
     * <pre>
     * Platform p = getAndSetForTest(...);
     * try {
     *   ...
     * } finally {
     *   getAndSetForTest(p);
     * }
     * </pre>
     *
     * @return the previous value of {@link #get()}.
     */
    public static Platform getAndSetForTest(Platform platform) {
        if (platform == null) {
            throw new NullPointerException();
        }
        return INSTANCE_HOLDER.getAndSet(platform);
    }

    // NOTE: Prior to Android Q, the standard way of accessing some Conscrypt features was to
    // use reflection to call hidden APIs.  Beginning in Q, there is public API for all of these
    // features.  We attempt to use the public API where possible, but also still call the
    // hidden versions to continue to support old versions of Conscrypt that might be bundled with
    // apps or third-party TLS providers that might have taken advantage of being able to
    // duck-type their way into compatibility.  For more background, see b/128280837.

    /** setUseSessionTickets(boolean) */
    private static final OptionalMethod<Socket> SET_USE_SESSION_TICKETS =
            new OptionalMethod<Socket>(null, "setUseSessionTickets", Boolean.TYPE);
    /** setHostname(String) */
    private static final OptionalMethod<Socket> SET_HOSTNAME =
            new OptionalMethod<Socket>(null, "setHostname", String.class);
    /** byte[] getAlpnSelectedProtocol() */
    private static final OptionalMethod<Socket> GET_ALPN_SELECTED_PROTOCOL =
            new OptionalMethod<Socket>(byte[].class, "getAlpnSelectedProtocol");
    /** setAlpnSelectedProtocol(byte[]) */
    private static final OptionalMethod<Socket> SET_ALPN_PROTOCOLS =
            new OptionalMethod<Socket>(null, "setAlpnProtocols", byte[].class );

    @dalvik.annotation.compat.UnsupportedAppUsage
    public void logW(String warning) {
        System.logW(warning);
    }

    public void tagSocket(Socket socket) throws SocketException {
        SocketTagger.get().tag(socket);
    }

    public void untagSocket(Socket socket) throws SocketException {
        SocketTagger.get().untag(socket);
    }

    public void configureTlsExtensions(
            SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        // All extensions here use both public API and reflective calls, see note above.
        SSLParameters sslParams = sslSocket.getSSLParameters();
        if (hostname != null) {
            // Enable session tickets
            if (SSLSockets.isSupportedSocket(sslSocket)) {
                SSLSockets.setUseSessionTickets(sslSocket, true);
            } else {
                SET_USE_SESSION_TICKETS.invokeOptionalWithoutCheckedException(sslSocket, true);
            }
            try {
                // Enable SNI
                sslParams.setServerNames(
                    Collections.<SNIServerName>singletonList(new SNIHostName(hostname)));
            } catch (IllegalArgumentException ignored) {
                // The hostname isn't a valid SNI value, so ignore the exception and don't set
                // a server name, which results in no SNI extension being sent.
            }
            if (!isPlatformSocket(sslSocket)) {
                SET_HOSTNAME.invokeOptionalWithoutCheckedException(sslSocket, hostname);
            }
        }

        // Enable ALPN, if necessary
        sslParams.setApplicationProtocols(getProtocolIds(protocols));

        if (!isPlatformSocket(sslSocket) && SET_ALPN_PROTOCOLS.isSupported(sslSocket)) {
            Object[] parameters = {concatLengthPrefixed(protocols)};
            SET_ALPN_PROTOCOLS.invokeWithoutCheckedException(sslSocket, parameters);
        }
        sslSocket.setSSLParameters(sslParams);
    }

    /**
     * Called after the TLS handshake to release resources allocated by {@link
     * #configureTlsExtensions}.
     */
    public void afterHandshake(SSLSocket sslSocket) {
    }

    public String getSelectedProtocol(SSLSocket socket) {
        // This API was added in Android Q
        try {
            return socket.getApplicationProtocol();
        } catch (UnsupportedOperationException ignored) {
            // The socket doesn't support this API, try the old reflective method
        }
        // This method was used through Android P, see note above
        boolean alpnSupported = GET_ALPN_SELECTED_PROTOCOL.isSupported(socket);
        if (!alpnSupported) {
            return null;
        }

        byte[] alpnResult =
                (byte[]) GET_ALPN_SELECTED_PROTOCOL.invokeWithoutCheckedException(socket);
        if (alpnResult != null) {
            return new String(alpnResult, Util.UTF_8);
        }
        return null;
    }

    public void connectSocket(Socket socket, InetSocketAddress address,
              int connectTimeout) throws IOException {
        socket.connect(address, connectTimeout);
    }

    /** Prefix used on custom headers. */
    public String getPrefix() {
        return "X-Android";
    }

    /**
     * Stripped down/adapted from OkHttp's {@code Platform.Android.trustManager()}.
     * OkHttp 2.7.5 uses this only for certificate pinning logic that we don't use
     * on Android, so this method should never be called outside of OkHttp's tests.
     * This method has been stripped down to the minimum for OkHttp's tests to pass.
     */
    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        Class sslParametersClass;
        try {
            sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object context = readFieldOrNull(sslSocketFactory, sslParametersClass, "sslParameters");
        return readFieldOrNull(context, X509TrustManager.class, "x509TrustManager");
    }

    /**
     * Stripped down from OkHttp's implementation to the minimum to get OkHttp's tests
     * to pass. OkHttp 2.7.5 uses this for certificate pinning logic which is unused
     * in Android. This method should never be called outside of OkHttp's tests.
     */
    public TrustRootIndex trustRootIndex(X509TrustManager trustManager) {
        return new RealTrustRootIndex(trustManager.getAcceptedIssuers());
    }

    // Helper method from OkHttp's Platform.java
    private static <T> T readFieldOrNull(Object instance, Class<T> fieldType, String fieldName) {
        for (Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value == null || !fieldType.isInstance(value)) return null;
                return fieldType.cast(value);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }

        // Didn't find the field we wanted. As a last gasp attempt, try to find the value on a delegate.
        if (!fieldName.equals("delegate")) {
            Object delegate = readFieldOrNull(instance, Object.class, "delegate");
            if (delegate != null) return readFieldOrNull(delegate, fieldType, fieldName);
        }

        return null;
    }

    private static boolean isPlatformSocket(SSLSocket socket) {
        return socket.getClass().getName().startsWith("com.android.org.conscrypt");
    }

    private static String[] getProtocolIds(List<Protocol> protocols) {
        String[] result = new String[protocols.size()];
        for (int i = 0; i < protocols.size(); i++) {
            result[i] = protocols.get(i).toString();
        }
        return result;
    }

    /**
     * Returns the concatenation of 8-bit, length prefixed protocol names.
     * http://tools.ietf.org/html/draft-agl-tls-nextprotoneg-04#page-4
     */
    static byte[] concatLengthPrefixed(List<Protocol> protocols) {
        Buffer result = new Buffer();
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) continue; // No HTTP/1.0 for ALPN.
            result.writeByte(protocol.toString().length());
            result.writeUtf8(protocol.toString());
        }
        return result.readByteArray();
    }
}
