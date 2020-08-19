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
import org.robovm.apple.usernotifications.*;
import org.robovm.apple.linkpresentation.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 14.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UICollectionViewCellRegistration/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UICollectionViewCellRegistrationPtr extends Ptr<UICollectionViewCellRegistration, UICollectionViewCellRegistrationPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UICollectionViewCellRegistration.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UICollectionViewCellRegistration() {}
    protected UICollectionViewCellRegistration(Handle h, long handle) { super(h, handle); }
    protected UICollectionViewCellRegistration(SkipInit skipInit) { super(skipInit); }
    public UICollectionViewCellRegistration(Class<?> cellClass, @Block VoidBlock3<UICollectionViewCell, NSIndexPath, NSObject> configurationHandler) { super((Handle) null, create(cellClass, configurationHandler)); retain(getHandle()); }
    public UICollectionViewCellRegistration(UINib cellNib, @Block VoidBlock3<UICollectionViewCell, NSIndexPath, NSObject> configurationHandler) { super((Handle) null, create(cellNib, configurationHandler)); retain(getHandle()); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "cellClass")
    public native Class<?> getCellClass();
    @Property(selector = "cellNib")
    public native UINib getCellNib();
    @Property(selector = "configurationHandler")
    public native @Block VoidBlock3<UICollectionViewCell, NSIndexPath, NSObject> getConfigurationHandler();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "registrationWithCellClass:configurationHandler:")
    protected static native @Pointer long create(Class<?> cellClass, @Block VoidBlock3<UICollectionViewCell, NSIndexPath, NSObject> configurationHandler);
    @Method(selector = "registrationWithCellNib:configurationHandler:")
    protected static native @Pointer long create(UINib cellNib, @Block VoidBlock3<UICollectionViewCell, NSIndexPath, NSObject> configurationHandler);
    /*</methods>*/
}
