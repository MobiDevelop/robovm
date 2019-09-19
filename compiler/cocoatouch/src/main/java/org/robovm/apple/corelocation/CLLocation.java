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
package org.robovm.apple.corelocation;

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
import org.robovm.apple.addressbook.*;
import org.robovm.apple.corebluetooth.*;
import org.robovm.apple.contacts.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("CoreLocation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CLLocation/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSSecureCoding/*</implements>*/ {

    /*<ptr>*/public static class CLLocationPtr extends Ptr<CLLocation, CLLocationPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(CLLocation.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public CLLocation() {}
    protected CLLocation(Handle h, long handle) { super(h, handle); }
    protected CLLocation(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithLatitude:longitude:")
    public CLLocation(double latitude, double longitude) { super((SkipInit) null); initObject(init(latitude, longitude)); }
    @Method(selector = "initWithCoordinate:altitude:horizontalAccuracy:verticalAccuracy:timestamp:")
    public CLLocation(@ByVal CLLocationCoordinate2D coordinate, double altitude, double hAccuracy, double vAccuracy, NSDate timestamp) { super((SkipInit) null); initObject(init(coordinate, altitude, hAccuracy, vAccuracy, timestamp)); }
    @Method(selector = "initWithCoordinate:altitude:horizontalAccuracy:verticalAccuracy:course:speed:timestamp:")
    public CLLocation(@ByVal CLLocationCoordinate2D coordinate, double altitude, double hAccuracy, double vAccuracy, double course, double speed, NSDate timestamp) { super((SkipInit) null); initObject(init(coordinate, altitude, hAccuracy, vAccuracy, course, speed, timestamp)); }
    @Method(selector = "initWithCoder:")
    public CLLocation(NSCoder coder) { super((SkipInit) null); initObject(init(coder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "coordinate")
    public native @ByVal CLLocationCoordinate2D getCoordinate();
    @Property(selector = "altitude")
    public native double getAltitude();
    @Property(selector = "horizontalAccuracy")
    public native double getHorizontalAccuracy();
    @Property(selector = "verticalAccuracy")
    public native double getVerticalAccuracy();
    @Property(selector = "course")
    public native double getCourse();
    @Property(selector = "speed")
    public native double getSpeed();
    @Property(selector = "timestamp")
    public native NSDate getTimestamp();
    @Property(selector = "floor")
    public native CLFloor getFloor();
    @Property(selector = "supportsSecureCoding")
    public static native boolean supportsSecureCoding();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @GlobalValue(symbol="CLLocationDistanceMax", optional=true)
    public static native double getLocationDistanceMax();
    
    @Method(selector = "initWithLatitude:longitude:")
    protected native @Pointer long init(double latitude, double longitude);
    @Method(selector = "initWithCoordinate:altitude:horizontalAccuracy:verticalAccuracy:timestamp:")
    protected native @Pointer long init(@ByVal CLLocationCoordinate2D coordinate, double altitude, double hAccuracy, double vAccuracy, NSDate timestamp);
    @Method(selector = "initWithCoordinate:altitude:horizontalAccuracy:verticalAccuracy:course:speed:timestamp:")
    protected native @Pointer long init(@ByVal CLLocationCoordinate2D coordinate, double altitude, double hAccuracy, double vAccuracy, double course, double speed, NSDate timestamp);
    @Method(selector = "distanceFromLocation:")
    public native double getDistanceTo(CLLocation location);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder coder);
    /*</methods>*/
}
