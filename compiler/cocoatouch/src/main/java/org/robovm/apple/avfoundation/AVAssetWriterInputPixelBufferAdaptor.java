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
package org.robovm.apple.avfoundation;

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
import org.robovm.apple.dispatch.*;
import org.robovm.apple.coreanimation.*;
import org.robovm.apple.coreimage.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coreaudio.*;
import org.robovm.apple.coremedia.*;
import org.robovm.apple.corevideo.*;
import org.robovm.apple.mediatoolbox.*;
import org.robovm.apple.audiotoolbox.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 4.1 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAssetWriterInputPixelBufferAdaptor/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class AVAssetWriterInputPixelBufferAdaptorPtr extends Ptr<AVAssetWriterInputPixelBufferAdaptor, AVAssetWriterInputPixelBufferAdaptorPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(AVAssetWriterInputPixelBufferAdaptor.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected AVAssetWriterInputPixelBufferAdaptor() {}
    protected AVAssetWriterInputPixelBufferAdaptor(Handle h, long handle) { super(h, handle); }
    protected AVAssetWriterInputPixelBufferAdaptor(SkipInit skipInit) { super(skipInit); }
    @WeaklyLinked
    @Method(selector = "initWithAssetWriterInput:sourcePixelBufferAttributes:")
    public AVAssetWriterInputPixelBufferAdaptor(AVAssetWriterInput input, CVPixelBufferAttributes sourcePixelBufferAttributes) { super((SkipInit) null); initObject(init(input, sourcePixelBufferAttributes)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "assetWriterInput")
    public native AVAssetWriterInput getAssetWriterInput();
    @WeaklyLinked
    @Property(selector = "sourcePixelBufferAttributes")
    public native CVPixelBufferAttributes getSourcePixelBufferAttributes();
    @WeaklyLinked
    @Property(selector = "pixelBufferPool")
    public native CVPixelBufferPool getPixelBufferPool();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @WeaklyLinked
    @Method(selector = "initWithAssetWriterInput:sourcePixelBufferAttributes:")
    protected native @Pointer long init(AVAssetWriterInput input, CVPixelBufferAttributes sourcePixelBufferAttributes);
    @WeaklyLinked
    @Method(selector = "appendPixelBuffer:withPresentationTime:")
    public native boolean appendPixelBuffer(CVPixelBuffer pixelBuffer, @ByVal CMTime presentationTime);
    /*</methods>*/
}
