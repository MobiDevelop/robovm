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
package org.robovm.apple.gamekit;

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
import org.robovm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 4.1 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("GameKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/GKMatchmakerViewController/*</name>*/ 
    extends /*<extends>*/UINavigationController/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class GKMatchmakerViewControllerPtr extends Ptr<GKMatchmakerViewController, GKMatchmakerViewControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(GKMatchmakerViewController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public GKMatchmakerViewController() {}
    protected GKMatchmakerViewController(Handle h, long handle) { super(h, handle); }
    protected GKMatchmakerViewController(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithMatchRequest:")
    public GKMatchmakerViewController(GKMatchRequest request) { super((SkipInit) null); initObject(init(request)); }
    @Method(selector = "initWithInvite:")
    public GKMatchmakerViewController(GKInvite invite) { super((SkipInit) null); initObject(init(invite)); }
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "initWithNavigationBarClass:toolbarClass:")
    public GKMatchmakerViewController(Class<? extends UINavigationBar> navigationBarClass, Class<? extends UIToolbar> toolbarClass) { super((SkipInit) null); initObject(init(navigationBarClass, toolbarClass)); }
    @Method(selector = "initWithRootViewController:")
    public GKMatchmakerViewController(UIViewController rootViewController) { super((SkipInit) null); initObject(init(rootViewController)); }
    @Method(selector = "initWithNibName:bundle:")
    public GKMatchmakerViewController(String nibNameOrNil, NSBundle nibBundleOrNil) { super((SkipInit) null); initObject(init(nibNameOrNil, nibBundleOrNil)); }
    @Method(selector = "initWithCoder:")
    public GKMatchmakerViewController(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "matchmakerDelegate")
    public native GKMatchmakerViewControllerDelegate getMatchmakerDelegate();
    @Property(selector = "setMatchmakerDelegate:", strongRef = true)
    public native void setMatchmakerDelegate(GKMatchmakerViewControllerDelegate v);
    @Property(selector = "matchRequest")
    public native GKMatchRequest getMatchRequest();
    @Property(selector = "isHosted")
    public native boolean isHosted();
    @Property(selector = "setHosted:")
    public native void setHosted(boolean v);
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "defaultInvitationMessage")
    public native String getDefaultInvitationMessage();
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "setDefaultInvitationMessage:")
    public native void setDefaultInvitationMessage(String v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithMatchRequest:")
    protected native @Pointer long init(GKMatchRequest request);
    @Method(selector = "initWithInvite:")
    protected native @Pointer long init(GKInvite invite);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "addPlayersToMatch:")
    public native void addPlayersToMatch(GKMatch match);
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 8.0. use setHostedPlayer:didConnect:
     */
    @Deprecated
    @Method(selector = "setHostedPlayer:connected:")
    public native void setHostedPlayer(String playerID, boolean connected);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "setHostedPlayer:didConnect:")
    public native void setHostedPlayer(GKPlayer player, boolean connected);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "initWithNavigationBarClass:toolbarClass:")
    protected native @Pointer long init(Class<? extends UINavigationBar> navigationBarClass, Class<? extends UIToolbar> toolbarClass);
    @Method(selector = "initWithRootViewController:")
    protected native @Pointer long init(UIViewController rootViewController);
    @Method(selector = "initWithNibName:bundle:")
    protected native @Pointer long init(String nibNameOrNil, NSBundle nibBundleOrNil);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    /*</methods>*/
}
