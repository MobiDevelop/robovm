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
package org.robovm.apple.corefoundation;

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
import org.robovm.apple.dispatch.*;
import org.robovm.apple.coreservices.*;
import org.robovm.apple.coremedia.*;
import org.robovm.apple.uikit.*;
import org.robovm.apple.coretext.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("CoreFoundation")/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CFStreamPropertyValues/*</name>*/ 
    extends /*<extends>*/CocoaUtility/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/static { Bro.bind(CFStreamPropertyValues.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamErrorDomainSOCKS", optional=true)
    public static native int ErrorDomainSOCKS();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamSocketSOCKSVersion4", optional=true)
    public static native String SOCKSVersion4();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamSocketSOCKSVersion5", optional=true)
    public static native String SOCKSVersion5();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamErrorDomainSSL", optional=true)
    public static native int ErrorDomainSSL();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamSocketSecurityLevelNone", optional=true)
    public static native String SecurityLevelNone();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 10.0.
     */
    @Deprecated
    @GlobalValue(symbol="kCFStreamSocketSecurityLevelSSLv2", optional=true)
    public static native String SecurityLevelSSLv2();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 10.0.
     */
    @Deprecated
    @GlobalValue(symbol="kCFStreamSocketSecurityLevelSSLv3", optional=true)
    public static native String SecurityLevelSSLv3();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamSocketSecurityLevelTLSv1", optional=true)
    public static native String SecurityLevelTLSv1();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @GlobalValue(symbol="kCFStreamSocketSecurityLevelNegotiatedSSL", optional=true)
    public static native String SecurityLevelNegotiatedSSL();
    /*</methods>*/
}