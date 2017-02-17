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
package org.robovm.apple.scenekit;

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
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coreanimation.*;
import org.robovm.apple.dispatch.*;
import org.robovm.apple.glkit.*;
import org.robovm.apple.spritekit.*;
import org.robovm.apple.opengles.*;
import org.robovm.apple.metal.*;
import org.robovm.apple.avfoundation.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("SceneKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/SCNMaterial/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements SCNAnimatable, SCNShadable/*</implements>*/ {

    /*<ptr>*/public static class SCNMaterialPtr extends Ptr<SCNMaterial, SCNMaterialPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(SCNMaterial.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public SCNMaterial() {}
    protected SCNMaterial(Handle h, long handle) { super(h, handle); }
    protected SCNMaterial(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "name")
    public native String getName();
    @Property(selector = "setName:")
    public native void setName(String v);
    @Property(selector = "diffuse")
    public native SCNMaterialProperty getDiffuse();
    @Property(selector = "ambient")
    public native SCNMaterialProperty getAmbient();
    @Property(selector = "specular")
    public native SCNMaterialProperty getSpecular();
    @Property(selector = "emission")
    public native SCNMaterialProperty getEmission();
    @Property(selector = "transparent")
    public native SCNMaterialProperty getTransparent();
    @Property(selector = "reflective")
    public native SCNMaterialProperty getReflective();
    @Property(selector = "multiply")
    public native SCNMaterialProperty getMultiply();
    @Property(selector = "normal")
    public native SCNMaterialProperty getNormal();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "ambientOcclusion")
    public native SCNMaterialProperty getAmbientOcclusion();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "selfIllumination")
    public native SCNMaterialProperty getSelfIllumination();
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Property(selector = "metalness")
    public native SCNMaterialProperty getMetalness();
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Property(selector = "roughness")
    public native SCNMaterialProperty getRoughness();
    @Property(selector = "shininess")
    public native @MachineSizedFloat double getShininess();
    @Property(selector = "setShininess:")
    public native void setShininess(@MachineSizedFloat double v);
    @Property(selector = "transparency")
    public native @MachineSizedFloat double getTransparency();
    @Property(selector = "setTransparency:")
    public native void setTransparency(@MachineSizedFloat double v);
    @Property(selector = "lightingModelName")
    public native String getLightingModelName();
    @Property(selector = "setLightingModelName:")
    public native void setLightingModelName(String v);
    @Property(selector = "isLitPerPixel")
    public native boolean isLitPerPixel();
    @Property(selector = "setLitPerPixel:")
    public native void setLitPerPixel(boolean v);
    @Property(selector = "isDoubleSided")
    public native boolean isDoubleSided();
    @Property(selector = "setDoubleSided:")
    public native void setDoubleSided(boolean v);
    @Property(selector = "cullMode")
    public native SCNCullMode getCullMode();
    @Property(selector = "setCullMode:")
    public native void setCullMode(SCNCullMode v);
    @Property(selector = "transparencyMode")
    public native SCNTransparencyMode getTransparencyMode();
    @Property(selector = "setTransparencyMode:")
    public native void setTransparencyMode(SCNTransparencyMode v);
    @Property(selector = "locksAmbientWithDiffuse")
    public native boolean locksAmbientWithDiffuse();
    @Property(selector = "setLocksAmbientWithDiffuse:")
    public native void setLocksAmbientWithDiffuse(boolean v);
    @Property(selector = "writesToDepthBuffer")
    public native boolean writesToDepthBuffer();
    @Property(selector = "setWritesToDepthBuffer:")
    public native void setWritesToDepthBuffer(boolean v);
    @Property(selector = "readsFromDepthBuffer")
    public native boolean readsFromDepthBuffer();
    @Property(selector = "setReadsFromDepthBuffer:")
    public native void setReadsFromDepthBuffer(boolean v);
    @Property(selector = "fresnelExponent")
    public native @MachineSizedFloat double getFresnelExponent();
    @Property(selector = "setFresnelExponent:")
    public native void setFresnelExponent(@MachineSizedFloat double v);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "blendMode")
    public native SCNBlendMode getBlendMode();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setBlendMode:")
    public native void setBlendMode(SCNBlendMode v);
    @Property(selector = "animationKeys")
    public native NSArray<?> getAnimationKeys();
    @Property(selector = "program")
    public native SCNProgram getProgram();
    @Property(selector = "setProgram:")
    public native void setProgram(SCNProgram v);
    @Property(selector = "shaderModifiers")
    public native @org.robovm.rt.bro.annotation.Marshaler(SCNShaderModifierEntryPoint.AsStringMapMarshaler.class) Map<SCNShaderModifierEntryPoint, String> getShaderModifiers();
    @Property(selector = "setShaderModifiers:")
    public native void setShaderModifiers(@org.robovm.rt.bro.annotation.Marshaler(SCNShaderModifierEntryPoint.AsStringMapMarshaler.class) Map<SCNShaderModifierEntryPoint, String> v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "material")
    public static native SCNMaterial create();
    @Method(selector = "addAnimation:forKey:")
    public native void addAnimation(CAAnimation animation, String key);
    @Method(selector = "removeAllAnimations")
    public native void removeAllAnimations();
    @Method(selector = "removeAnimationForKey:")
    public native void removeAnimation(String key);
    @Method(selector = "animationForKey:")
    public native CAAnimation getAnimation(String key);
    @Method(selector = "pauseAnimationForKey:")
    public native void pauseAnimation(String key);
    @Method(selector = "resumeAnimationForKey:")
    public native void resumeAnimation(String key);
    @Method(selector = "isAnimationForKeyPaused:")
    public native boolean isAnimationPaused(String key);
    @Method(selector = "removeAnimationForKey:fadeOutDuration:")
    public native void removeAnimation(String key, @MachineSizedFloat double duration);
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Method(selector = "setSpeed:forAnimationKey:")
    public native void setSpeed(@MachineSizedFloat double speed, String key);
    @Method(selector = "handleBindingOfSymbol:usingBlock:")
    public native void handleBindingOfSymbol(String symbol, @Block VoidBlock4<Integer, Integer, SCNNode, SCNRenderer> block);
    @Method(selector = "handleUnbindingOfSymbol:usingBlock:")
    public native void handleUnbindingOfSymbol(String symbol, @Block VoidBlock4<Integer, Integer, SCNNode, SCNRenderer> block);
    /*</methods>*/
}
