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
package org.robovm.apple.metalps;

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
import org.robovm.apple.metal.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 11.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("MetalPerformanceShaders") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/MPSNNAdditionNode/*</name>*/ 
    extends /*<extends>*/MPSNNBinaryArithmeticNode/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class MPSNNAdditionNodePtr extends Ptr<MPSNNAdditionNode, MPSNNAdditionNodePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MPSNNAdditionNode.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected MPSNNAdditionNode() {}
    protected MPSNNAdditionNode(Handle h, long handle) { super(h, handle); }
    protected MPSNNAdditionNode(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithSources:")
    public MPSNNAdditionNode(NSArray<MPSNNImageNode> sourceNodes) { super((SkipInit) null); initObject(init(sourceNodes)); }
    @Method(selector = "initWithLeftSource:rightSource:")
    public MPSNNAdditionNode(MPSNNImageNode left, MPSNNImageNode right) { super((SkipInit) null); initObject(init(left, right)); }
    /*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithSources:")
    protected native @Pointer long init(NSArray<MPSNNImageNode> sourceNodes);
    @Method(selector = "initWithLeftSource:rightSource:")
    protected native @Pointer long init(MPSNNImageNode left, MPSNNImageNode right);
    /*</methods>*/
}
