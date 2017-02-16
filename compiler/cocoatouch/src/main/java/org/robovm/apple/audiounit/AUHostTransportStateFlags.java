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
package org.robovm.apple.audiounit;

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
import org.robovm.apple.audiotoolbox.*;
import org.robovm.apple.corefoundation.*;
import org.robovm.apple.coreaudio.*;
import org.robovm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Marshaler(Bits.AsMachineSizedIntMarshaler.class)/*</annotations>*/
public final class /*<name>*/AUHostTransportStateFlags/*</name>*/ extends Bits</*<name>*/AUHostTransportStateFlags/*</name>*/> {
    /*<values>*/
    public static final AUHostTransportStateFlags None = new AUHostTransportStateFlags(0L);
    public static final AUHostTransportStateFlags Changed = new AUHostTransportStateFlags(1L);
    public static final AUHostTransportStateFlags Moving = new AUHostTransportStateFlags(2L);
    public static final AUHostTransportStateFlags Recording = new AUHostTransportStateFlags(4L);
    public static final AUHostTransportStateFlags Cycling = new AUHostTransportStateFlags(8L);
    /*</values>*/

    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<methods>*//*</methods>*/

    private static final /*<name>*/AUHostTransportStateFlags/*</name>*/[] values = _values(/*<name>*/AUHostTransportStateFlags/*</name>*/.class);

    public /*<name>*/AUHostTransportStateFlags/*</name>*/(long value) { super(value); }
    private /*<name>*/AUHostTransportStateFlags/*</name>*/(long value, long mask) { super(value, mask); }
    protected /*<name>*/AUHostTransportStateFlags/*</name>*/ wrap(long value, long mask) {
        return new /*<name>*/AUHostTransportStateFlags/*</name>*/(value, mask);
    }
    protected /*<name>*/AUHostTransportStateFlags/*</name>*/[] _values() {
        return values;
    }
    public static /*<name>*/AUHostTransportStateFlags/*</name>*/[] values() {
        return values.clone();
    }
}
