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
package org.robovm.apple.corehaptic;

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
import org.robovm.apple.avfoundation.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 13.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("CoreHaptics") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CHHapticEventParameter/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class CHHapticEventParameterPtr extends Ptr<CHHapticEventParameter, CHHapticEventParameterPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(CHHapticEventParameter.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected CHHapticEventParameter() {}
    protected CHHapticEventParameter(Handle h, long handle) { super(h, handle); }
    protected CHHapticEventParameter(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithParameterID:value:")
    public CHHapticEventParameter(CHHapticEventParameterID parameterID, float value) { super((SkipInit) null); initObject(init(parameterID, value)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "parameterID")
    public native CHHapticEventParameterID getParameterID();
    @Property(selector = "value")
    public native float getValue();
    @Property(selector = "setValue:")
    public native void setValue(float v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithParameterID:value:")
    protected native @Pointer long init(CHHapticEventParameterID parameterID, float value);
    /*</methods>*/
}
