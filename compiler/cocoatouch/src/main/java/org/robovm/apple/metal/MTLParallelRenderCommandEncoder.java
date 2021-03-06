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
package org.robovm.apple.metal;

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
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("Metal") @NativeProtocolProxy/*</annotations>*/
/*<visibility>*/public final/*</visibility>*/ class /*<name>*/MTLParallelRenderCommandEncoder/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements MTLCommandEncoder/*</implements>*/ {

    /*<ptr>*/public static class MTLParallelRenderCommandEncoderPtr extends Ptr<MTLParallelRenderCommandEncoder, MTLParallelRenderCommandEncoderPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MTLParallelRenderCommandEncoder.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "device")
    public native MTLDevice getDevice();
    @Property(selector = "label")
    public native String getLabel();
    @Property(selector = "setLabel:")
    public native void setLabel(String v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "renderCommandEncoder")
    public native MTLRenderCommandEncoder newRenderCommandEncoder();
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Method(selector = "setColorStoreAction:atIndex:")
    public native void setColorStoreAction(MTLStoreAction storeAction, @MachineSizedUInt long colorAttachmentIndex);
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Method(selector = "setDepthStoreAction:")
    public native void setDepthStoreAction(MTLStoreAction storeAction);
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Method(selector = "setStencilStoreAction:")
    public native void setStencilStoreAction(MTLStoreAction storeAction);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "setColorStoreActionOptions:atIndex:")
    public native void setColorStoreActionOptions(MTLStoreActionOptions storeActionOptions, @MachineSizedUInt long colorAttachmentIndex);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "setDepthStoreActionOptions:")
    public native void setDepthStoreActionOptions(MTLStoreActionOptions storeActionOptions);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "setStencilStoreActionOptions:")
    public native void setStencilStoreActionOptions(MTLStoreActionOptions storeActionOptions);
    @Method(selector = "endEncoding")
    public native void endEncoding();
    @Method(selector = "insertDebugSignpost:")
    public native void insertDebugSignpost(String string);
    @Method(selector = "pushDebugGroup:")
    public native void pushDebugGroup(String string);
    @Method(selector = "popDebugGroup")
    public native void popDebugGroup();
    /*</methods>*/
}
