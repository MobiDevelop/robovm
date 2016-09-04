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
package org.robovm.apple.uikit;

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
import org.robovm.apple.coreanimation.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coredata.*;
import org.robovm.apple.coreimage.*;
import org.robovm.apple.coretext.*;
import org.robovm.apple.corelocation.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 10.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIGraphicsImageRendererFormat/*</name>*/ 
    extends /*<extends>*/UIGraphicsRendererFormat/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UIGraphicsImageRendererFormatPtr extends Ptr<UIGraphicsImageRendererFormat, UIGraphicsImageRendererFormatPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UIGraphicsImageRendererFormat.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UIGraphicsImageRendererFormat() {}
    protected UIGraphicsImageRendererFormat(Handle h, long handle) { super(h, handle); }
    protected UIGraphicsImageRendererFormat(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "scale")
    public native @MachineSizedFloat double getScale();
    @Property(selector = "setScale:")
    public native void setScale(@MachineSizedFloat double v);
    @Property(selector = "opaque")
    public native boolean isOpaque();
    @Property(selector = "setOpaque:")
    public native void setOpaque(boolean v);
    @Property(selector = "prefersExtendedRange")
    public native boolean prefersExtendedRange();
    @Property(selector = "setPrefersExtendedRange:")
    public native void setPrefersExtendedRange(boolean v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    
    /*</methods>*/
}
