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
package org.robovm.apple.gameplaykit;

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

/*</javadoc>*/
/*<annotations>*/@Marshaler(Bits.AsMachineSizedIntMarshaler.class)/*</annotations>*/
public final class /*<name>*/GKMeshGraphTriangulationMode/*</name>*/ extends Bits</*<name>*/GKMeshGraphTriangulationMode/*</name>*/> {
    /*<values>*/
    public static final GKMeshGraphTriangulationMode None = new GKMeshGraphTriangulationMode(0L);
    public static final GKMeshGraphTriangulationMode Vertices = new GKMeshGraphTriangulationMode(1L);
    public static final GKMeshGraphTriangulationMode Centers = new GKMeshGraphTriangulationMode(2L);
    public static final GKMeshGraphTriangulationMode EdgeMidpoints = new GKMeshGraphTriangulationMode(4L);
    /*</values>*/

    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<methods>*//*</methods>*/

    private static final /*<name>*/GKMeshGraphTriangulationMode/*</name>*/[] values = _values(/*<name>*/GKMeshGraphTriangulationMode/*</name>*/.class);

    public /*<name>*/GKMeshGraphTriangulationMode/*</name>*/(long value) { super(value); }
    private /*<name>*/GKMeshGraphTriangulationMode/*</name>*/(long value, long mask) { super(value, mask); }
    protected /*<name>*/GKMeshGraphTriangulationMode/*</name>*/ wrap(long value, long mask) {
        return new /*<name>*/GKMeshGraphTriangulationMode/*</name>*/(value, mask);
    }
    protected /*<name>*/GKMeshGraphTriangulationMode/*</name>*/[] _values() {
        return values;
    }
    public static /*<name>*/GKMeshGraphTriangulationMode/*</name>*/[] values() {
        return values.clone();
    }
}
