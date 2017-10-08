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
package org.robovm.apple.healthkit;

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
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 11.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("HealthKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/HKWorkoutRouteQuery/*</name>*/ 
    extends /*<extends>*/HKQuery/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class HKWorkoutRouteQueryPtr extends Ptr<HKWorkoutRouteQuery, HKWorkoutRouteQueryPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(HKWorkoutRouteQuery.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public HKWorkoutRouteQuery() {}
    protected HKWorkoutRouteQuery(Handle h, long handle) { super(h, handle); }
    protected HKWorkoutRouteQuery(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithRoute:dataHandler:")
    public HKWorkoutRouteQuery(HKWorkoutRoute workoutRoute, @Block VoidBlock4<HKWorkoutRouteQuery, NSArray<CLLocation>, Boolean, NSError> dataHandler) { super((SkipInit) null); initObject(init(workoutRoute, dataHandler)); }
    /*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithRoute:dataHandler:")
    protected native @Pointer long init(HKWorkoutRoute workoutRoute, @Block VoidBlock4<HKWorkoutRouteQuery, NSArray<CLLocation>, Boolean, NSError> dataHandler);
    /*</methods>*/
}
