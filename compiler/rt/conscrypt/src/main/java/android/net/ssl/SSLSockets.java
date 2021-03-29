/*
 * Copyright (C) 2018 The Android Open Source Project
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

package android.net.ssl;

import com.android.org.conscrypt.Conscrypt;
import javax.net.ssl.SSLSocket;
import libcore.util.NonNull;

/**
 * Static utility methods for accessing additional functionality of supported instances of
 * {@link SSLSocket}.  Sockets from the platform TLS provider will be compatible with all
 * methods in this class.
 */
public class SSLSockets {
    private SSLSockets() {}

    /**
     * Returns whether the given socket can be used with the methods in this class.  In general,
     * only sockets from the platform TLS provider are supported.
     */
    public static boolean isSupportedSocket(@NonNull SSLSocket socket) {
        return Conscrypt.isConscrypt(socket);
    }

    private static void checkSupported(@NonNull SSLSocket s) {
        if (!isSupportedSocket(s)) {
            throw new IllegalArgumentException("Socket is not a supported socket.");
        }
    }

    /**
     * Enables or disables the use of session tickets.
     *
     * <p>This function must be called before the handshake is started or it will have no effect.
     *
     * @param socket the socket
     * @param useSessionTickets whether to enable or disable the use of session tickets
     * @throws IllegalArgumentException if the given socket is not a platform socket
     */
    public static void setUseSessionTickets(@NonNull SSLSocket socket, boolean useSessionTickets) {
        checkSupported(socket);
        Conscrypt.setUseSessionTickets(socket, useSessionTickets);
    }
}
