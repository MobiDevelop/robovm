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
package org.robovm.apple.audiotoolbox;

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
import org.robovm.apple.corefoundation.*;
import org.robovm.apple.coreaudio.*;
import org.robovm.apple.coremidi.*;
import org.robovm.apple.avfoundation.*;
import org.robovm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AudioUnitParameterEventValues/*</name>*/ 
    extends /*<extends>*/Struct<AudioUnitParameterEventValues>/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class AudioUnitParameterEventValuesPtr extends Ptr<AudioUnitParameterEventValues, AudioUnitParameterEventValuesPtr> {}/*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public AudioUnitParameterEventValues() {}
    public AudioUnitParameterEventValues(AudioUnitParameterEventValuesRamp ramp, AudioUnitParameterEventValuesImmediate immediate) {
        this.setRamp(ramp);
        this.setImmediate(immediate);
    }
    /*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*/
    @StructMember(0) public native @ByVal AudioUnitParameterEventValuesRamp getRamp();
    @StructMember(0) public native AudioUnitParameterEventValues setRamp(@ByVal AudioUnitParameterEventValuesRamp ramp);
    @StructMember(0) public native @ByVal AudioUnitParameterEventValuesImmediate getImmediate();
    @StructMember(0) public native AudioUnitParameterEventValues setImmediate(@ByVal AudioUnitParameterEventValuesImmediate immediate);
    /*</members>*/
    /*<methods>*//*</methods>*/
}
