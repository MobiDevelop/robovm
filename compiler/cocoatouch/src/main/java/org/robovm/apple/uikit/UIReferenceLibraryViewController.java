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
package org.robovm.apple.uikit;

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
import org.robovm.apple.coreanimation.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.coredata.*;
import org.robovm.apple.coreimage.*;
import org.robovm.apple.coretext.*;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.cloudkit.*;
import org.robovm.apple.fileprovider.*;
import org.robovm.apple.intents.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 5.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIReferenceLibraryViewController/*</name>*/ 
    extends /*<extends>*/UIViewController/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UIReferenceLibraryViewControllerPtr extends Ptr<UIReferenceLibraryViewController, UIReferenceLibraryViewControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UIReferenceLibraryViewController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected UIReferenceLibraryViewController() {}
    protected UIReferenceLibraryViewController(Handle h, long handle) { super(h, handle); }
    protected UIReferenceLibraryViewController(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithTerm:")
    public UIReferenceLibraryViewController(String term) { super((SkipInit) null); initObject(init(term)); }
    @Method(selector = "initWithCoder:")
    public UIReferenceLibraryViewController(NSCoder coder) { super((SkipInit) null); initObject(init(coder)); }
    /*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithTerm:")
    protected native @Pointer long init(String term);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder coder);
    @Method(selector = "dictionaryHasDefinitionForTerm:")
    public static native boolean dictionaryHasDefinitionForTerm(String term);
    /*</methods>*/
}
