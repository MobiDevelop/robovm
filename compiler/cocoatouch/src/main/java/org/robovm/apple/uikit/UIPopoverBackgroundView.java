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
import org.robovm.apple.cloudkit.*;
import org.robovm.apple.fileprovider.*;
import org.robovm.apple.intents.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 5.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIPopoverBackgroundView/*</name>*/ 
    extends /*<extends>*/UIView/*</extends>*/ 
    /*<implements>*/implements UIPopoverBackgroundViewMethods/*</implements>*/ {

    /*<ptr>*/public static class UIPopoverBackgroundViewPtr extends Ptr<UIPopoverBackgroundView, UIPopoverBackgroundViewPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UIPopoverBackgroundView.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UIPopoverBackgroundView() {}
    protected UIPopoverBackgroundView(Handle h, long handle) { super(h, handle); }
    protected UIPopoverBackgroundView(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithFrame:")
    public UIPopoverBackgroundView(@ByVal CGRect frame) { super((SkipInit) null); initObject(init(frame)); }
    @Method(selector = "initWithCoder:")
    public UIPopoverBackgroundView(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    public UIPopoverBackgroundView(CGRect frame) {
        super(frame);
    }
    public UIPopoverBackgroundView(NSCoder decoder) {
        super(decoder);
    }
    /*<properties>*/
    @Property(selector = "arrowOffset")
    public native @MachineSizedFloat double getArrowOffset();
    @Property(selector = "setArrowOffset:")
    public native void setArrowOffset(@MachineSizedFloat double v);
    @Property(selector = "arrowDirection")
    public native UIPopoverArrowDirection getArrowDirection();
    @Property(selector = "setArrowDirection:")
    public native void setArrowDirection(UIPopoverArrowDirection v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "wantsDefaultContentAppearance")
    public static native boolean wantsDefaultContentAppearance();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithFrame:")
    protected native @Pointer long init(@ByVal CGRect frame);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    @Method(selector = "arrowBase")
    public static native @MachineSizedFloat double getArrowBase();
    @Method(selector = "contentViewInsets")
    public static native @ByVal UIEdgeInsets getContentViewInsets();
    @Method(selector = "arrowHeight")
    public static native @MachineSizedFloat double getArrowHeight();
    /*</methods>*/
}
