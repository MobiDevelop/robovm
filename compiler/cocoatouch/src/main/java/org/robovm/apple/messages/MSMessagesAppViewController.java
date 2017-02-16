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
package org.robovm.apple.messages;

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
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("Messages") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/MSMessagesAppViewController/*</name>*/ 
    extends /*<extends>*/UIViewController/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class MSMessagesAppViewControllerPtr extends Ptr<MSMessagesAppViewController, MSMessagesAppViewControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MSMessagesAppViewController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public MSMessagesAppViewController() {}
    protected MSMessagesAppViewController(Handle h, long handle) { super(h, handle); }
    protected MSMessagesAppViewController(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "activeConversation")
    public native MSConversation getActiveConversation();
    @Property(selector = "presentationStyle")
    public native MSMessagesAppPresentationStyle getPresentationStyle();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "requestPresentationStyle:")
    public native void requestPresentationStyle(MSMessagesAppPresentationStyle presentationStyle);
    @Method(selector = "dismiss")
    public native void dismiss();
    @Method(selector = "willBecomeActiveWithConversation:")
    public native void willBecomeActiveWithConversation(MSConversation conversation);
    @Method(selector = "didBecomeActiveWithConversation:")
    public native void didBecomeActiveWithConversation(MSConversation conversation);
    @Method(selector = "willResignActiveWithConversation:")
    public native void willResignActiveWithConversation(MSConversation conversation);
    @Method(selector = "didResignActiveWithConversation:")
    public native void didResignActiveWithConversation(MSConversation conversation);
    @Method(selector = "willSelectMessage:conversation:")
    public native void willSelectMessage(MSMessage message, MSConversation conversation);
    @Method(selector = "didSelectMessage:conversation:")
    public native void didSelectMessage(MSMessage message, MSConversation conversation);
    @Method(selector = "didReceiveMessage:conversation:")
    public native void didReceiveMessage(MSMessage message, MSConversation conversation);
    @Method(selector = "didStartSendingMessage:conversation:")
    public native void didStartSendingMessage(MSMessage message, MSConversation conversation);
    @Method(selector = "didCancelSendingMessage:conversation:")
    public native void didCancelSendingMessage(MSMessage message, MSConversation conversation);
    @Method(selector = "willTransitionToPresentationStyle:")
    public native void willTransitionToPresentationStyle(MSMessagesAppPresentationStyle presentationStyle);
    @Method(selector = "didTransitionToPresentationStyle:")
    public native void didTransitionToPresentationStyle(MSMessagesAppPresentationStyle presentationStyle);
    /*</methods>*/
}
