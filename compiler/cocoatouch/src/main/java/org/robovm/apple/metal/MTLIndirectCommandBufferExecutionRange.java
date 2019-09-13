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
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/MTLIndirectCommandBufferExecutionRange/*</name>*/ 
    extends /*<extends>*/Struct<MTLIndirectCommandBufferExecutionRange>/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class MTLIndirectCommandBufferExecutionRangePtr extends Ptr<MTLIndirectCommandBufferExecutionRange, MTLIndirectCommandBufferExecutionRangePtr> {}/*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public MTLIndirectCommandBufferExecutionRange() {}
    public MTLIndirectCommandBufferExecutionRange(int location, int length) {
        this.setLocation(location);
        this.setLength(length);
    }
    /*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*/
    @StructMember(0) public native int getLocation();
    @StructMember(0) public native MTLIndirectCommandBufferExecutionRange setLocation(int location);
    @StructMember(1) public native int getLength();
    @StructMember(1) public native MTLIndirectCommandBufferExecutionRange setLength(int length);
    /*</members>*/
    /*<methods>*//*</methods>*/
}
