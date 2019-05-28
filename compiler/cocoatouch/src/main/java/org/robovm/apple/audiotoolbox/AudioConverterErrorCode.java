/*
 * Copyright (C) 2013-2015 RoboVM AB
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
package org.robovm.apple.audiotoolbox;

/*<imports>*/
import java.io.*;
import java.nio.*;
import java.util.*;
import org.robovm.objc.*;
import org.robovm.objc.annotation.*;
import org.robovm.objc.block.*;
import org.robovm.rt.*;
import org.robovm.rt.annotation.*;
import org.robovm.rt.bro.*;
import org.robovm.rt.bro.annotation.*;
import org.robovm.rt.bro.ptr.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.corefoundation.*;
import org.robovm.apple.coreaudio.*;
import org.robovm.apple.coremidi.*;
import org.robovm.apple.avfoundation.*;
import org.robovm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Marshaler(ValuedEnum.AsMachineSizedSIntMarshaler.class)/*</annotations>*/
public enum /*<name>*/AudioConverterErrorCode/*</name>*/ implements ValuedEnum {
    /*<values>*/
    FormatNotSupported(1718449215L),
    OperationNotSupported(1869627199L),
    PropertyNotSupported(1886547824L),
    InvalidInputSize(1768846202L),
    InvalidOutputSize(1869902714L),
    UnspecifiedError(2003329396L),
    BadPropertySizeError(561211770L),
    RequiresPacketDescriptionsError(561015652L),
    InputSampleRateOutOfRange(560558962L),
    OutputSampleRateOutOfRange(560952178L),
    HardwareInUse(1752656245L),
    NoHardwarePermission(1885696621L);
    /*</values>*/

    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<methods>*//*</methods>*/

    private final long n;

    private /*<name>*/AudioConverterErrorCode/*</name>*/(long n) { this.n = n; }
    public long value() { return n; }
    public static /*<name>*/AudioConverterErrorCode/*</name>*/ valueOf(long n) {
        for (/*<name>*/AudioConverterErrorCode/*</name>*/ v : values()) {
            if (v.n == n) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + n + " found in " 
            + /*<name>*/AudioConverterErrorCode/*</name>*/.class.getName());
    }
}
