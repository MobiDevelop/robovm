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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/MPSCNNNeuronReLUNNode/*</name>*/ 
    extends /*<extends>*/MPSCNNNeuronNode/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class MPSCNNNeuronReLUNNodePtr extends Ptr<MPSCNNNeuronReLUNNode, MPSCNNNeuronReLUNNodePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MPSCNNNeuronReLUNNode.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected MPSCNNNeuronReLUNNode() {}
    protected MPSCNNNeuronReLUNNode(Handle h, long handle) { super(h, handle); }
    protected MPSCNNNeuronReLUNNode(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithSource:a:b:")
    public MPSCNNNeuronReLUNNode(MPSNNImageNode sourceNode, float a, float b) { super((SkipInit) null); initObject(init(sourceNode, a, b)); }
    @Method(selector = "initWithSource:")
    public MPSCNNNeuronReLUNNode(MPSNNImageNode sourceNode) { super((SkipInit) null); initObject(initWithSource(sourceNode)); }
    /*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithSource:a:b:")
    protected native @Pointer long init(MPSNNImageNode sourceNode, float a, float b);
    @Method(selector = "initWithSource:")
    protected native @Pointer long initWithSource(MPSNNImageNode sourceNode);
    @Method(selector = "nodeWithSource:")
    public static native MPSCNNNeuronReLUNNode nodeWithSource(MPSNNImageNode sourceNode);
    /*</methods>*/
}
