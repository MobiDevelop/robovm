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
package org.robovm.apple.passkit;

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
import org.robovm.apple.addressbook.*;
import org.robovm.apple.contacts.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coreanimation.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/PKPaymentAuthorizationViewControllerDelegateAdapter/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements PKPaymentAuthorizationViewControllerDelegate/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @NotImplemented("paymentAuthorizationViewControllerDidFinish:")
    public void didFinish(PKPaymentAuthorizationViewController controller) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("paymentAuthorizationViewController:didAuthorizePayment:handler:")
    public void didAuthorizePayment(PKPaymentAuthorizationViewController controller, PKPayment payment, @Block VoidBlock1<PKPaymentAuthorizationResult> completion) {}
    /**
     * @since Available in iOS 8.3 and later.
     */
    @NotImplemented("paymentAuthorizationViewControllerWillAuthorizePayment:")
    public void willAuthorizePayment(PKPaymentAuthorizationViewController controller) {}
    /**
     * @since Available in iOS 14.0 and later.
     */
    @NotImplemented("paymentAuthorizationViewController:didRequestMerchantSessionUpdate:")
    public void didRequestMerchantSessionUpdate(PKPaymentAuthorizationViewController controller, @Block VoidBlock1<PKPaymentRequestMerchantSessionUpdate> handler) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("paymentAuthorizationViewController:didSelectShippingMethod:handler:")
    public void didSelectShippingMethod(PKPaymentAuthorizationViewController controller, PKShippingMethod shippingMethod, @Block VoidBlock1<PKPaymentRequestShippingMethodUpdate> completion) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("paymentAuthorizationViewController:didSelectShippingContact:handler:")
    public void didSelectShippingContact(PKPaymentAuthorizationViewController controller, PKContact contact, @Block VoidBlock1<PKPaymentRequestShippingContactUpdate> completion) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("paymentAuthorizationViewController:didSelectPaymentMethod:handler:")
    public void didSelectPaymentMethod(PKPaymentAuthorizationViewController controller, PKPaymentMethod paymentMethod, @Block VoidBlock1<PKPaymentRequestPaymentMethodUpdate> completion) {}
    /**
     * @deprecated Deprecated in iOS 11.0. Use paymentAuthorizationViewController:didAuthorizePayment:handler: instead to provide more granular errors
     */
    @Deprecated
    @NotImplemented("paymentAuthorizationViewController:didAuthorizePayment:completion:")
    public void didAuthorizePaymentEx(PKPaymentAuthorizationViewController controller, PKPayment payment, @Block VoidBlock1<PKPaymentAuthorizationStatus> completion) {}
    /**
     * @deprecated Deprecated in iOS 11.0. Use paymentAuthorizationViewController:didSelectShippingMethod:handler: instead to provide more granular errors
     */
    @Deprecated
    @NotImplemented("paymentAuthorizationViewController:didSelectShippingMethod:completion:")
    public void didSelectShippingMethod(PKPaymentAuthorizationViewController controller, PKShippingMethod shippingMethod, @Block VoidBlock2<PKPaymentAuthorizationStatus, NSArray<PKPaymentSummaryItem>> completion) {}
    /**
     * @deprecated Deprecated in iOS 9.0. ABRecordRef has been deprecated. Please migrate away from this delegate callback as soon as possible.
     */
    @Deprecated
    @NotImplemented("paymentAuthorizationViewController:didSelectShippingAddress:completion:")
    public void didSelectShippingAddress(PKPaymentAuthorizationViewController controller, ABRecord address, @Block VoidBlock3<PKPaymentAuthorizationStatus, NSArray<PKShippingMethod>, NSArray<PKPaymentSummaryItem>> completion) {}
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use paymentAuthorizationViewController:didSelectShippingContact:handler: instead to provide more granular errors
     */
    @Deprecated
    @NotImplemented("paymentAuthorizationViewController:didSelectShippingContact:completion:")
    public void didSelectShippingContact(PKPaymentAuthorizationViewController controller, PKContact contact, @Block VoidBlock3<PKPaymentAuthorizationStatus, NSArray<PKShippingMethod>, NSArray<PKPaymentSummaryItem>> completion) {}
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use paymentAuthorizationViewController:didSelectPaymentMethod:handler: instead to provide more granular errors
     */
    @Deprecated
    @NotImplemented("paymentAuthorizationViewController:didSelectPaymentMethod:completion:")
    public void didSelectPaymentMethodEx(PKPaymentAuthorizationViewController controller, PKPaymentMethod paymentMethod, @Block VoidBlock1<NSArray<PKPaymentSummaryItem>> completion) {}
    /*</methods>*/
}
