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
 * @since Available in iOS 6.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UICollectionReusableView/*</name>*/ 
    extends /*<extends>*/UIView/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UICollectionReusableViewPtr extends Ptr<UICollectionReusableView, UICollectionReusableViewPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UICollectionReusableView.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UICollectionReusableView() {}
    protected UICollectionReusableView(Handle h, long handle) { super(h, handle); }
    protected UICollectionReusableView(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithFrame:")
    public UICollectionReusableView(@ByVal CGRect frame) { super((SkipInit) null); initObject(init(frame)); }
    @Method(selector = "initWithCoder:")
    public UICollectionReusableView(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    public UICollectionReusableView(CGRect frame) {
        super(frame);
    }
    public UICollectionReusableView(NSCoder decoder) {
        super(decoder);
    }
    /*<properties>*/
    @Property(selector = "reuseIdentifier")
    public native String getReuseIdentifier();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "prepareForReuse")
    public native void prepareForReuse();
    @Method(selector = "applyLayoutAttributes:")
    public native void applyLayoutAttributes(UICollectionViewLayoutAttributes layoutAttributes);
    @Method(selector = "willTransitionFromLayout:toLayout:")
    public native void willTransition(UICollectionViewLayout oldLayout, UICollectionViewLayout newLayout);
    @Method(selector = "didTransitionFromLayout:toLayout:")
    public native void didTransition(UICollectionViewLayout oldLayout, UICollectionViewLayout newLayout);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "preferredLayoutAttributesFittingAttributes:")
    public native UICollectionViewLayoutAttributes getPreferredLayoutAttributesFittingAttributes(UICollectionViewLayoutAttributes layoutAttributes);
    @Method(selector = "initWithFrame:")
    protected native @Pointer long init(@ByVal CGRect frame);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    /*</methods>*/
}
