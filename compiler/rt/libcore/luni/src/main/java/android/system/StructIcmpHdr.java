/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.system;

import static android.system.OsConstants.ICMP6_ECHO_REQUEST;
import static android.system.OsConstants.ICMP_ECHO;

/**
 * Corresponds to C's {@code struct icmphdr} from linux/icmp.h and {@code struct icmp6hdr} from
 * linux/icmpv6.h
 *
 * @hide
 */
public final class StructIcmpHdr {
    private byte[] packet;

    private StructIcmpHdr() {
        packet =  new byte[8];
    }

    /*
     * Echo or Echo Reply Message
     *
     * 0                   1                   2                   3
     * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |     Type      |     Code      |          Checksum             |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |           Identifier          |        Sequence Number        |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |     Data ...
     * +-+-+-+-+-
     */
    public static StructIcmpHdr IcmpEchoHdr(boolean ipv4, int seq) {
        StructIcmpHdr hdr = new StructIcmpHdr();
        hdr.packet[0] = ipv4 ? (byte) ICMP_ECHO : (byte) ICMP6_ECHO_REQUEST;
        // packet[1]: Code is always zero.
        // packet[2,3]: Checksum is computed by kernel.
        // packet[4,5]: ID (= port) inserted by kernel.
        hdr.packet[6] = (byte) (seq >> 8);
        hdr.packet[7] = (byte) seq;
        return hdr;
    }

    public byte[] getBytes() {
        return packet.clone();
    }
}
