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
package org.robovm.apple.cloudkit;

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
import org.robovm.apple.corelocation.*;
import org.robovm.apple.contacts.*;
import org.robovm.apple.fileprovider.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 11.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("CloudKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CKOperationConfiguration/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class CKOperationConfigurationPtr extends Ptr<CKOperationConfiguration, CKOperationConfigurationPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(CKOperationConfiguration.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public CKOperationConfiguration() {}
    protected CKOperationConfiguration(Handle h, long handle) { super(h, handle); }
    protected CKOperationConfiguration(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "container")
    public native CKContainer getContainer();
    @Property(selector = "setContainer:")
    public native void setContainer(CKContainer v);
    @Property(selector = "qualityOfService")
    public native NSQualityOfService getQualityOfService();
    @Property(selector = "setQualityOfService:")
    public native void setQualityOfService(NSQualityOfService v);
    @Property(selector = "allowsCellularAccess")
    public native boolean allowsCellularAccess();
    @Property(selector = "setAllowsCellularAccess:")
    public native void setAllowsCellularAccess(boolean v);
    @Property(selector = "isLongLived")
    public native boolean isLongLived();
    @Property(selector = "setLongLived:")
    public native void setLongLived(boolean v);
    @Property(selector = "timeoutIntervalForRequest")
    public native double getTimeoutIntervalForRequest();
    @Property(selector = "setTimeoutIntervalForRequest:")
    public native void setTimeoutIntervalForRequest(double v);
    @Property(selector = "timeoutIntervalForResource")
    public native double getTimeoutIntervalForResource();
    @Property(selector = "setTimeoutIntervalForResource:")
    public native void setTimeoutIntervalForResource(double v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    
    /*</methods>*/
}
