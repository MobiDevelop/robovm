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
package org.robovm.apple.foundation;

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
import org.robovm.apple.corefoundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.apple.coretext.*;
import org.robovm.apple.coreanimation.*;
import org.robovm.apple.coredata.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coremedia.*;
import org.robovm.apple.security.*;
import org.robovm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Marshaler(Bits.AsMachineSizedIntMarshaler.class)/*</annotations>*/
public final class /*<name>*/NSJSONWritingOptions/*</name>*/ extends Bits</*<name>*/NSJSONWritingOptions/*</name>*/> {
    /*<values>*/
    public static final NSJSONWritingOptions None = new NSJSONWritingOptions(0L);
    public static final NSJSONWritingOptions PrettyPrinted = new NSJSONWritingOptions(1L);
    /**
     * @since Available in iOS 11.0 and later.
     */
    public static final NSJSONWritingOptions SortedKeys = new NSJSONWritingOptions(2L);
    public static final NSJSONWritingOptions FragmentsAllowed = new NSJSONWritingOptions(4L);
    /**
     * @since Available in iOS 13.0 and later.
     */
    public static final NSJSONWritingOptions WithoutEscapingSlashes = new NSJSONWritingOptions(8L);
    /*</values>*/

    private static final /*<name>*/NSJSONWritingOptions/*</name>*/[] values = _values(/*<name>*/NSJSONWritingOptions/*</name>*/.class);

    public /*<name>*/NSJSONWritingOptions/*</name>*/(long value) { super(value); }
    private /*<name>*/NSJSONWritingOptions/*</name>*/(long value, long mask) { super(value, mask); }
    protected /*<name>*/NSJSONWritingOptions/*</name>*/ wrap(long value, long mask) {
        return new /*<name>*/NSJSONWritingOptions/*</name>*/(value, mask);
    }
    protected /*<name>*/NSJSONWritingOptions/*</name>*/[] _values() {
        return values;
    }
    public static /*<name>*/NSJSONWritingOptions/*</name>*/[] values() {
        return values.clone();
    }
}
