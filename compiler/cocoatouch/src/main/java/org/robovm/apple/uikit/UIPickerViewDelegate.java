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
import org.robovm.apple.usernotifications.*;
import org.robovm.apple.linkpresentation.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ interface /*<name>*/UIPickerViewDelegate/*</name>*/ 
    /*<implements>*/extends NSObjectProtocol/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<methods>*/
    @Method(selector = "pickerView:widthForComponent:")
    @MachineSizedFloat double getComponentWidth(UIPickerView pickerView, @MachineSizedSInt long component);
    @Method(selector = "pickerView:rowHeightForComponent:")
    @MachineSizedFloat double getRowHeight(UIPickerView pickerView, @MachineSizedSInt long component);
    @Method(selector = "pickerView:titleForRow:forComponent:")
    String getRowTitle(UIPickerView pickerView, @MachineSizedSInt long row, @MachineSizedSInt long component);
    @Method(selector = "pickerView:attributedTitleForRow:forComponent:")
    NSAttributedString getAttributedRowTitle(UIPickerView pickerView, @MachineSizedSInt long row, @MachineSizedSInt long component);
    @Method(selector = "pickerView:viewForRow:forComponent:reusingView:")
    UIView getRowView(UIPickerView pickerView, @MachineSizedSInt long row, @MachineSizedSInt long component, UIView view);
    @Method(selector = "pickerView:didSelectRow:inComponent:")
    void didSelectRow(UIPickerView pickerView, @MachineSizedSInt long row, @MachineSizedSInt long component);
    /*</methods>*/
    /*<adapter>*/
    /*</adapter>*/
}
