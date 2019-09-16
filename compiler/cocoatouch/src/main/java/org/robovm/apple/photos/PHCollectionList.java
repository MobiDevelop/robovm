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
package org.robovm.apple.photos;

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
import org.robovm.apple.uikit.*;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.avfoundation.*;
import org.robovm.apple.coreimage.*;
import org.robovm.apple.coremedia.*;
import org.robovm.apple.imageio.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 8.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("Photos") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/PHCollectionList/*</name>*/ 
    extends /*<extends>*/PHCollection/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class PHCollectionListPtr extends Ptr<PHCollectionList, PHCollectionListPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(PHCollectionList.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public PHCollectionList() {}
    protected PHCollectionList(Handle h, long handle) { super(h, handle); }
    protected PHCollectionList(SkipInit skipInit) { super(skipInit); }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public PHCollectionList(NSArray<PHCollection> collections, String title) { super((Handle) null, createTransientCollectionList(collections, title)); retain(getHandle()); }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public PHCollectionList(PHFetchResult<PHCollection> fetchResult, String title) { super((Handle) null, createTransientCollectionList(fetchResult, title)); retain(getHandle()); }
    /*</constructors>*/
    /*<properties>*/
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "collectionListType")
    public native PHCollectionListType getCollectionListType();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "collectionListSubtype")
    public native PHCollectionListSubtype getCollectionListSubtype();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "startDate")
    public native NSDate getStartDate();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "endDate")
    public native NSDate getEndDate();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "localizedLocationNames")
    public native @org.robovm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> getLocalizedLocationNames();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "fetchCollectionListsContainingCollection:options:")
    public static native PHFetchResult<PHCollectionList> fetchCollectionListsContainingCollection(PHCollection collection, PHFetchOptions options);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "fetchCollectionListsWithLocalIdentifiers:options:")
    public static native PHFetchResult<PHCollectionList> fetchCollectionListsWithLocalIdentifiers(@org.robovm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> identifiers, PHFetchOptions options);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "fetchCollectionListsWithType:subtype:options:")
    public static native PHFetchResult<PHCollectionList> fetchCollectionListsWithType(PHCollectionListType collectionListType, PHCollectionListSubtype subtype, PHFetchOptions options);
    /**
     * @since Available in iOS 8.0 and later.
     * @deprecated Deprecated in iOS 13.0. Will be removed in a future release
     */
    @Deprecated
    @Method(selector = "fetchMomentListsWithSubtype:containingMoment:options:")
    public static native PHFetchResult<PHCollectionList> fetchMomentListsWithSubtypeContainingMoment(PHCollectionListSubtype momentListSubtype, PHAssetCollection moment, PHFetchOptions options);
    /**
     * @since Available in iOS 8.0 and later.
     * @deprecated Deprecated in iOS 13.0. Will be removed in a future release
     */
    @Deprecated
    @Method(selector = "fetchMomentListsWithSubtype:options:")
    public static native PHFetchResult<PHCollectionList> fetchMomentListsWithSubtype(PHCollectionListSubtype momentListSubtype, PHFetchOptions options);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "transientCollectionListWithCollections:title:")
    protected static native @Pointer long createTransientCollectionList(NSArray<PHCollection> collections, String title);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "transientCollectionListWithCollectionsFetchResult:title:")
    protected static native @Pointer long createTransientCollectionList(PHFetchResult<PHCollection> fetchResult, String title);
    /*</methods>*/
}
