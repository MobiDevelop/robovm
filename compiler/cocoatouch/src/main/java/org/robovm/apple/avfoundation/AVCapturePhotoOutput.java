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
import org.robovm.apple.audiounit.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 10.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVCapturePhotoOutput/*</name>*/ 
    extends /*<extends>*/AVCaptureOutput/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class AVCapturePhotoOutputPtr extends Ptr<AVCapturePhotoOutput, AVCapturePhotoOutputPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(AVCapturePhotoOutput.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public AVCapturePhotoOutput() {}
    protected AVCapturePhotoOutput(Handle h, long handle) { super(h, handle); }
    protected AVCapturePhotoOutput(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "preparedPhotoSettingsArray")
    public native NSArray<AVCapturePhotoSettings> getPreparedPhotoSettingsArray();
    @Property(selector = "availablePhotoPixelFormatTypes")
    public native NSArray<NSNumber> getAvailablePhotoPixelFormatTypes();
    @Property(selector = "availablePhotoCodecTypes")
    public native NSArray<?> getAvailablePhotoCodecTypes();
    @Property(selector = "availableRawPhotoPixelFormatTypes")
    public native NSArray<NSNumber> getAvailableRawPhotoPixelFormatTypes();
    @Property(selector = "isStillImageStabilizationSupported")
    public native boolean isStillImageStabilizationSupported();
    @Property(selector = "isStillImageStabilizationScene")
    public native boolean isStillImageStabilizationScene();
    /**
     * @since Available in iOS 10.2 and later.
     */
    @Property(selector = "isDualCameraFusionSupported")
    public native boolean isDualCameraFusionSupported();
    @Property(selector = "supportedFlashModes")
    public native NSArray<NSNumber> getSupportedFlashModes();
    @Property(selector = "isFlashScene")
    public native boolean isFlashScene();
    @Property(selector = "photoSettingsForSceneMonitoring")
    public native AVCapturePhotoSettings getPhotoSettingsForSceneMonitoring();
    @Property(selector = "setPhotoSettingsForSceneMonitoring:")
    public native void setPhotoSettingsForSceneMonitoring(AVCapturePhotoSettings v);
    @Property(selector = "isHighResolutionCaptureEnabled")
    public native boolean isHighResolutionCaptureEnabled();
    @Property(selector = "setHighResolutionCaptureEnabled:")
    public native void setHighResolutionCaptureEnabled(boolean v);
    @Property(selector = "maxBracketedCapturePhotoCount")
    public native @MachineSizedUInt long getMaxBracketedCapturePhotoCount();
    @Property(selector = "isLensStabilizationDuringBracketedCaptureSupported")
    public native boolean isLensStabilizationDuringBracketedCaptureSupported();
    @Property(selector = "isLivePhotoCaptureSupported")
    public native boolean isLivePhotoCaptureSupported();
    @Property(selector = "isLivePhotoCaptureEnabled")
    public native boolean isLivePhotoCaptureEnabled();
    @Property(selector = "setLivePhotoCaptureEnabled:")
    public native void setLivePhotoCaptureEnabled(boolean v);
    @Property(selector = "isLivePhotoCaptureSuspended")
    public native boolean isLivePhotoCaptureSuspended();
    @Property(selector = "setLivePhotoCaptureSuspended:")
    public native void setLivePhotoCaptureSuspended(boolean v);
    @Property(selector = "isLivePhotoAutoTrimmingEnabled")
    public native boolean isLivePhotoAutoTrimmingEnabled();
    @Property(selector = "setLivePhotoAutoTrimmingEnabled:")
    public native void setLivePhotoAutoTrimmingEnabled(boolean v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "capturePhotoWithSettings:delegate:")
    public native void capturePhotoWithSettings(AVCapturePhotoSettings settings, AVCapturePhotoCaptureDelegate delegate);
    @Method(selector = "setPreparedPhotoSettingsArray:completionHandler:")
    public native void setPreparedPhotoSettingsArray(NSArray<AVCapturePhotoSettings> preparedPhotoSettingsArray, @Block VoidBlock2<Boolean, NSError> completionHandler);
    @Method(selector = "JPEGPhotoDataRepresentationForJPEGSampleBuffer:previewPhotoSampleBuffer:")
    public static native NSData JPEGPhotoDataRepresentationForJPEGSampleBuffer(CMSampleBuffer JPEGSampleBuffer, CMSampleBuffer previewPhotoSampleBuffer);
    @Method(selector = "DNGPhotoDataRepresentationForRawSampleBuffer:previewPhotoSampleBuffer:")
    public static native NSData DNGPhotoDataRepresentationForRawSampleBuffer(CMSampleBuffer rawSampleBuffer, CMSampleBuffer previewPhotoSampleBuffer);
    /*</methods>*/
}
