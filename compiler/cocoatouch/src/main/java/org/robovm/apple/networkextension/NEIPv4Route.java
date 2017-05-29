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
package org.robovm.apple.networkextension;

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
import org.robovm.apple.security.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 9.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("NetworkExtension") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NEIPv4Route/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NEIPv4RoutePtr extends Ptr<NEIPv4Route, NEIPv4RoutePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NEIPv4Route.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NEIPv4Route() {}
    protected NEIPv4Route(Handle h, long handle) { super(h, handle); }
    protected NEIPv4Route(SkipInit skipInit) { super(skipInit); }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "initWithDestinationAddress:subnetMask:")
    public NEIPv4Route(String address, String subnetMask) { super((SkipInit) null); initObject(init(address, subnetMask)); }
    /*</constructors>*/
    /*<properties>*/
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "destinationAddress")
    public native String getDestinationAddress();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "destinationSubnetMask")
    public native String getDestinationSubnetMask();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "gatewayAddress")
    public native String getGatewayAddress();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setGatewayAddress:")
    public native void setGatewayAddress(String v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "initWithDestinationAddress:subnetMask:")
    protected native @Pointer long init(String address, String subnetMask);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "defaultRoute")
    public static native NEIPv4Route defaultRoute();
    /*</methods>*/
}
