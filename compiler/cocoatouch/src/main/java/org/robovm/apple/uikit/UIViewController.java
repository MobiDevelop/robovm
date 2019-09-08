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
import org.robovm.apple.iad.ADInterstitialPresentationPolicy;
import org.robovm.apple.mediaplayer.MPMoviePlayerViewController;

/*<javadoc>*/
/**
 * @since Available in iOS 2.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIViewController/*</name>*/ 
    extends /*<extends>*/UIResponder/*</extends>*/ 
    /*<implements>*/implements NSCoding, UIAppearanceContainer, UITraitEnvironment, UIContentContainer, UIFocusEnvironment, UIStateRestoring, NSExtensionRequestHandling/*</implements>*/ {

    public static class Notifications {
        /**
         * @since Available in iOS 8.0 and later.
         */
        public static NSObject observeDidEnterBackground(UIViewController object, final VoidBlock1<UIViewController> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(ShowDetailTargetDidChangeNotification(), null, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((UIViewController)a.getObject());
                }
            });
        }
    }
    
    /*<ptr>*/public static class UIViewControllerPtr extends Ptr<UIViewController, UIViewControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UIViewController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UIViewController() {}
    @Deprecated protected UIViewController(long handle) { super(handle); }
    protected UIViewController(Handle h, long handle) { super(h, handle); }
    protected UIViewController(SkipInit skipInit) { super(skipInit); }
    @Method(selector = "initWithNibName:bundle:")
    public UIViewController(String nibNameOrNil, NSBundle nibBundleOrNil) { super((SkipInit) null); initObject(init(nibNameOrNil, nibBundleOrNil)); }
    @Method(selector = "initWithCoder:")
    public UIViewController(NSCoder coder) { super((SkipInit) null); initObject(init(coder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "view")
    public native UIView getView();
    @Property(selector = "setView:")
    public native void setView(UIView v);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "viewIfLoaded")
    public native UIView getViewIfLoaded();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "isViewLoaded")
    public native boolean isViewLoaded();
    @Property(selector = "nibName")
    public native String getNibName();
    @Property(selector = "nibBundle")
    public native NSBundle getNibBundle();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "storyboard")
    public native UIStoryboard getStoryboard();
    @Property(selector = "title")
    public native String getTitle();
    @Property(selector = "setTitle:")
    public native void setTitle(String v);
    @Property(selector = "parentViewController")
    public native UIViewController getParentViewController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "presentedViewController")
    public native UIViewController getPresentedViewController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "presentingViewController")
    public native UIViewController getPresentingViewController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "definesPresentationContext")
    public native boolean definesPresentationContext();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setDefinesPresentationContext:")
    public native void setDefinesPresentationContext(boolean v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "providesPresentationContextTransitionStyle")
    public native boolean providesPresentationContextTransitionStyle();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setProvidesPresentationContextTransitionStyle:")
    public native void setProvidesPresentationContextTransitionStyle(boolean v);
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Property(selector = "restoresFocusAfterTransition")
    public native boolean isRestoresFocusAfterTransition();
    /**
     * @since Available in iOS 10.0 and later.
     */
    @Property(selector = "setRestoresFocusAfterTransition:")
    public native void setRestoresFocusAfterTransition(boolean v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "isBeingPresented")
    public native boolean isBeingPresented();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "isBeingDismissed")
    public native boolean isBeingDismissed();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "isMovingToParentViewController")
    public native boolean isMovingToParentViewController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "isMovingFromParentViewController")
    public native boolean isMovingFromParentViewController();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "modalTransitionStyle")
    public native UIModalTransitionStyle getModalTransitionStyle();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "setModalTransitionStyle:")
    public native void setModalTransitionStyle(UIModalTransitionStyle v);
    /**
     * @since Available in iOS 3.2 and later.
     */
    @Property(selector = "modalPresentationStyle")
    public native UIModalPresentationStyle getModalPresentationStyle();
    /**
     * @since Available in iOS 3.2 and later.
     */
    @Property(selector = "setModalPresentationStyle:")
    public native void setModalPresentationStyle(UIModalPresentationStyle v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "modalPresentationCapturesStatusBarAppearance")
    public native boolean modalPresentationCapturesStatusBarAppearance();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setModalPresentationCapturesStatusBarAppearance:")
    public native void setModalPresentationCapturesStatusBarAppearance(boolean v);
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "disablesAutomaticKeyboardDismissal")
    public native boolean disablesAutomaticKeyboardDismissal();
    /**
     * @since Available in iOS 3.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "wantsFullScreenLayout")
    public native boolean wantsFullScreenLayout();
    /**
     * @since Available in iOS 3.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "setWantsFullScreenLayout:")
    public native void setWantsFullScreenLayout(boolean v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "edgesForExtendedLayout")
    public native UIRectEdge getEdgesForExtendedLayout();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setEdgesForExtendedLayout:")
    public native void setEdgesForExtendedLayout(UIRectEdge v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "extendedLayoutIncludesOpaqueBars")
    public native boolean extendedLayoutIncludesOpaqueBars();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setExtendedLayoutIncludesOpaqueBars:")
    public native void setExtendedLayoutIncludesOpaqueBars(boolean v);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use UIScrollView's contentInsetAdjustmentBehavior instead
     */
    @Deprecated
    @Property(selector = "automaticallyAdjustsScrollViewInsets")
    public native boolean automaticallyAdjustsScrollViewInsets();
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use UIScrollView's contentInsetAdjustmentBehavior instead
     */
    @Deprecated
    @Property(selector = "setAutomaticallyAdjustsScrollViewInsets:")
    public native void setAutomaticallyAdjustsScrollViewInsets(boolean v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "preferredContentSize")
    public native @ByVal CGSize getPreferredContentSize();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setPreferredContentSize:")
    public native void setPreferredContentSize(@ByVal CGSize v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "preferredStatusBarStyle")
    public native UIStatusBarStyle getPreferredStatusBarStyle();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "prefersStatusBarHidden")
    public native boolean prefersStatusBarHidden();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "preferredStatusBarUpdateAnimation")
    public native UIStatusBarAnimation getPreferredStatusBarUpdateAnimation();
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Property(selector = "overrideUserInterfaceStyle")
    public native UIUserInterfaceStyle getOverrideUserInterfaceStyle();
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Property(selector = "setOverrideUserInterfaceStyle:")
    public native void setOverrideUserInterfaceStyle(UIUserInterfaceStyle v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "shouldAutorotate")
    public native boolean shouldAutorotate();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "supportedInterfaceOrientations")
    public native UIInterfaceOrientationMask getSupportedInterfaceOrientations();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "preferredInterfaceOrientationForPresentation")
    public native UIInterfaceOrientation getPreferredInterfaceOrientationForPresentation();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Property(selector = "interfaceOrientation")
    public native UIInterfaceOrientation getInterfaceOrientation();
    @Property(selector = "isEditing")
    public native boolean isEditing();
    @Property(selector = "setEditing:")
    public native void setEditing(boolean v);
    @Property(selector = "editButtonItem")
    public native UIBarButtonItem getEditButtonItem();
    /**
     * @since Available in iOS 3.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Property(selector = "searchDisplayController")
    public native UISearchDisplayController getSearchDisplayController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "childViewControllers")
    public native NSArray<UIViewController> getChildViewControllers();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "childViewControllerForStatusBarStyle")
    public native UIViewController getChildViewControllerForStatusBarStyle();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "childViewControllerForStatusBarHidden")
    public native UIViewController getChildViewControllerForStatusBarHidden();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "shouldAutomaticallyForwardAppearanceMethods")
    public native boolean shouldAutomaticallyForwardAppearanceMethods();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "restorationIdentifier")
    public native String getRestorationIdentifier();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setRestorationIdentifier:")
    public native void setRestorationIdentifier(String v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "restorationClass")
    public native ObjCClass getRestorationClass();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setRestorationClass:", strongRef = true)
    public native void setRestorationClass(ObjCClass v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "transitioningDelegate")
    public native UIViewControllerTransitioningDelegate getTransitioningDelegate();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setTransitioningDelegate:", strongRef = true)
    public native void setTransitioningDelegate(UIViewControllerTransitioningDelegate v);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use view.safeAreaLayoutGuide.topAnchor instead of topLayoutGuide.bottomAnchor
     */
    @Deprecated
    @Property(selector = "topLayoutGuide")
    public native UILayoutSupport getTopLayoutGuide();
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 11.0. Use view.safeAreaLayoutGuide.bottomAnchor instead of bottomLayoutGuide.topAnchor
     */
    @Deprecated
    @Property(selector = "bottomLayoutGuide")
    public native UILayoutSupport getBottomLayoutGuide();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "additionalSafeAreaInsets")
    public native @ByVal UIEdgeInsets getAdditionalSafeAreaInsets();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "setAdditionalSafeAreaInsets:")
    public native void setAdditionalSafeAreaInsets(@ByVal UIEdgeInsets v);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "systemMinimumLayoutMargins")
    public native @ByVal NSDirectionalEdgeInsets getSystemMinimumLayoutMargins();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "viewRespectsSystemMinimumLayoutMargins")
    public native boolean isViewRespectsSystemMinimumLayoutMargins();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "setViewRespectsSystemMinimumLayoutMargins:")
    public native void setViewRespectsSystemMinimumLayoutMargins(boolean v);
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Property(selector = "performsActionsWhilePresentingModally")
    public native boolean performsActionsWhilePresentingModally();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "extensionContext")
    public native NSExtensionContext getExtensionContext();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "presentationController")
    public native UIPresentationController getPresentationController();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "popoverPresentationController")
    public native UIPopoverPresentationController getPopoverPresentationController();
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Property(selector = "isModalInPresentation")
    public native boolean isModalInPresentation();
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Property(selector = "setModalInPresentation:")
    public native void setModalInPresentation(boolean v);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "childViewControllerForScreenEdgesDeferringSystemGestures")
    public native UIViewController getChildViewControllerForScreenEdgesDeferringSystemGestures();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "preferredScreenEdgesDeferringSystemGestures")
    public native UIRectEdge getPreferredScreenEdgesDeferringSystemGestures();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "childViewControllerForHomeIndicatorAutoHidden")
    public native UIViewController getChildViewControllerForHomeIndicatorAutoHidden();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Property(selector = "prefersHomeIndicatorAutoHidden")
    public native boolean prefersHomeIndicatorAutoHidden();
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 13.0. UIViewControllerPreviewing is deprecated. Please use UIContextMenuInteraction.
     */
    @Deprecated
    @Property(selector = "previewActionItems")
    public native @org.robovm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class) List<UIPreviewActionItem> getPreviewActionItems();
    @Property(selector = "navigationItem")
    public native UINavigationItem getNavigationItem();
    @Property(selector = "hidesBottomBarWhenPushed")
    public native boolean hidesBottomBarWhenPushed();
    @Property(selector = "setHidesBottomBarWhenPushed:")
    public native void setHidesBottomBarWhenPushed(boolean v);
    @Property(selector = "navigationController")
    public native UINavigationController getNavigationController();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "toolbarItems")
    public native NSArray<UIBarButtonItem> getToolbarItems();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "setToolbarItems:")
    public native void setToolbarItems(NSArray<UIBarButtonItem> v);
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 13.0.
     */
    @Deprecated
    @Property(selector = "isModalInPopover")
    public native boolean isModalInPopover();
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 13.0.
     */
    @Deprecated
    @Property(selector = "setModalInPopover:")
    public native void setModalInPopover(boolean v);
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "contentSizeForViewInPopover")
    public native @ByVal CGSize getContentSizeForViewInPopover();
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "setContentSizeForViewInPopover:")
    public native void setContentSizeForViewInPopover(@ByVal CGSize v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "transitionCoordinator")
    public native UIViewControllerTransitionCoordinator getTransitionCoordinator();
    @Property(selector = "splitViewController")
    public native UISplitViewController getSplitViewController();
    @Property(selector = "tabBarItem")
    public native UITabBarItem getTabBarItem();
    @Property(selector = "setTabBarItem:")
    public native void setTabBarItem(UITabBarItem v);
    @Property(selector = "tabBarController")
    public native UITabBarController getTabBarController();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "traitCollection")
    public native UITraitCollection getTraitCollection();
    @Property(selector = "preferredFocusEnvironments")
    public native @org.robovm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class) List<UIFocusEnvironment> getPreferredFocusEnvironments();
    /**
     * @since Available in iOS 12.0 and later.
     */
    @Property(selector = "parentFocusEnvironment")
    public native UIFocusEnvironment getParentFocusEnvironment();
    /**
     * @since Available in iOS 12.0 and later.
     */
    @Property(selector = "focusItemContainer")
    public native UIFocusItemContainer getFocusItemContainer();
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 10.0.
     */
    @Deprecated
    @Property(selector = "preferredFocusedView")
    public native UIView getPreferredFocusedView();
    @Property(selector = "restorationParent")
    public native UIStateRestoring getRestorationParent();
    @Property(selector = "objectRestorationClass")
    public native Class<?> getObjectRestorationClass();
    /*</properties>*/
    /*<members>*//*</members>*/
    
    /* iAd extensions */
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public ADInterstitialPresentationPolicy getInterstitialPresentationPolicy() {
        return org.robovm.apple.iad.UIViewControllerExtensions.getInterstitialPresentationPolicy(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public void setInterstitialPresentationPolicy(ADInterstitialPresentationPolicy v) {
        org.robovm.apple.iad.UIViewControllerExtensions.setInterstitialPresentationPolicy(this, v);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public boolean isCanDisplayBannerAds() {
        return org.robovm.apple.iad.UIViewControllerExtensions.canDisplayBannerAds(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public void setCanDisplayBannerAds(boolean v) {
        org.robovm.apple.iad.UIViewControllerExtensions.setCanDisplayBannerAds(this, v);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public UIView getOriginalContentView() {
        return org.robovm.apple.iad.UIViewControllerExtensions.getOriginalContentView(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public boolean isPresentingFullScreenAd() {
        return org.robovm.apple.iad.UIViewControllerExtensions.isPresentingFullScreenAd(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public boolean isDisplayingBannerAd() {
        return org.robovm.apple.iad.UIViewControllerExtensions.isDisplayingBannerAd(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public boolean requestInterstitialAdPresentation() {
        return org.robovm.apple.iad.UIViewControllerExtensions.requestInterstitialAdPresentation(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public boolean shouldPresentInterstitialAd() {
        return org.robovm.apple.iad.UIViewControllerExtensions.shouldPresentInterstitialAd(this);
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public static void prepareInterstitialAds() {
        org.robovm.apple.iad.UIViewControllerExtensions.prepareInterstitialAds();
    }
    
    /* MediaPlayer extensions */
    @WeaklyLinked
    public void presentMoviePlayerViewController(MPMoviePlayerViewController moviePlayerViewController) {
        org.robovm.apple.mediaplayer.UIViewControllerExtensions.presentMoviePlayerViewController(this, moviePlayerViewController);
    }
    @WeaklyLinked
    public void dismissMoviePlayerViewController() {
        org.robovm.apple.mediaplayer.UIViewControllerExtensions.dismissMoviePlayerViewController(this);
    }
    
    /*<methods>*/
    /**
     * @since Available in iOS 8.0 and later.
     */
    @GlobalValue(symbol="UIViewControllerShowDetailTargetDidChangeNotification", optional=true)
    public static native NSString ShowDetailTargetDidChangeNotification();
    
    @Method(selector = "initWithNibName:bundle:")
    protected native @Pointer long init(String nibNameOrNil, NSBundle nibBundleOrNil);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder coder);
    @Method(selector = "loadView")
    public native void loadView();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "loadViewIfNeeded")
    public native void loadViewIfNeeded();
    @Method(selector = "viewDidLoad")
    public native void viewDidLoad();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "performSegueWithIdentifier:sender:")
    public native void performSegue(String identifier, NSObject sender);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "shouldPerformSegueWithIdentifier:sender:")
    public native boolean shouldPerformSegue(String identifier, NSObject sender);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "prepareForSegue:sender:")
    public native void prepareForSegue(UIStoryboardSegue segue, NSObject sender);
    /**
     * @since Available in iOS 13.0 and later.
     */
    @Method(selector = "canPerformUnwindSegueAction:fromViewController:sender:")
    public native boolean canPerformUnwindSegueAction(Selector action, UIViewController fromViewController, NSObject sender);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 13.0.
     */
    @Deprecated
    @Method(selector = "canPerformUnwindSegueAction:fromViewController:withSender:")
    public native boolean canPerformUnwind(Selector action, UIViewController fromViewController, NSObject sender);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "allowedChildViewControllersForUnwindingFromSource:")
    public native NSArray<UIViewController> getAllowedChildViewControllersForUnwinding(UIStoryboardUnwindSegueSource source);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "childViewControllerContainingSegueSource:")
    public native UIViewController getChildViewControllerContainingSegueSource(UIStoryboardUnwindSegueSource source);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Method(selector = "viewControllerForUnwindSegueAction:fromViewController:withSender:")
    public native UIViewController getViewControllerForUnwind(Selector action, UIViewController fromViewController, NSObject sender);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "unwindForSegue:towardsViewController:")
    public native void unwind(UIStoryboardSegue unwindSegue, UIViewController subsequentVC);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Method(selector = "segueForUnwindingToViewController:fromViewController:identifier:")
    public native UIStoryboardSegue getSegueForUnwinding(UIViewController toViewController, UIViewController fromViewController, String identifier);
    @Method(selector = "viewWillAppear:")
    public native void viewWillAppear(boolean animated);
    @Method(selector = "viewDidAppear:")
    public native void viewDidAppear(boolean animated);
    @Method(selector = "viewWillDisappear:")
    public native void viewWillDisappear(boolean animated);
    @Method(selector = "viewDidDisappear:")
    public native void viewDidDisappear(boolean animated);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "viewWillLayoutSubviews")
    public native void viewWillLayoutSubviews();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "viewDidLayoutSubviews")
    public native void viewDidLayoutSubviews();
    @Method(selector = "didReceiveMemoryWarning")
    public native void didReceiveMemoryWarning();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "presentViewController:animated:completion:")
    public native void presentViewController(UIViewController viewControllerToPresent, boolean animated, @Block Runnable completion);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "dismissViewControllerAnimated:completion:")
    public native void dismissViewController(boolean animated, @Block Runnable completion);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "setNeedsStatusBarAppearanceUpdate")
    public native void setNeedsStatusBarAppearanceUpdate();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "targetViewControllerForAction:sender:")
    public native UIViewController getTargetViewControllerForAction(Selector action, NSObject sender);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "showViewController:sender:")
    public native void showViewController(UIViewController vc, NSObject sender);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "showDetailViewController:sender:")
    public native void showDetailViewController(UIViewController vc, NSObject sender);
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 8.0. Header views are animated along with the rest of the view hierarchy
     */
    @Deprecated
    @Method(selector = "rotatingHeaderView")
    public native UIView getRotatingHeaderView();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 8.0. Footer views are animated along with the rest of the view hierarchy
     */
    @Deprecated
    @Method(selector = "rotatingFooterView")
    public native UIView getRotatingFooterView();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 8.0. Implement viewWillTransitionToSize:withTransitionCoordinator: instead
     */
    @Deprecated
    @Method(selector = "willRotateToInterfaceOrientation:duration:")
    public native void willRotate(UIInterfaceOrientation toInterfaceOrientation, double duration);
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Method(selector = "didRotateFromInterfaceOrientation:")
    public native void didRotate(UIInterfaceOrientation fromInterfaceOrientation);
    /**
     * @since Available in iOS 3.0 and later.
     * @deprecated Deprecated in iOS 8.0. Implement viewWillTransitionToSize:withTransitionCoordinator: instead
     */
    @Deprecated
    @Method(selector = "willAnimateRotationToInterfaceOrientation:duration:")
    public native void willAnimateRotation(UIInterfaceOrientation toInterfaceOrientation, double duration);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "attemptRotationToDeviceOrientation")
    public static native void attemptRotationToDeviceOrientation();
    @Method(selector = "setEditing:animated:")
    public native void setEditing(boolean editing, boolean animated);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "addChildViewController:")
    public native void addChildViewController(UIViewController childController);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "removeFromParentViewController")
    public native void removeFromParentViewController();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "transitionFromViewController:toViewController:duration:options:animations:completion:")
    public native void transition(UIViewController fromViewController, UIViewController toViewController, double duration, UIViewAnimationOptions options, @Block Runnable animations, @Block VoidBooleanBlock completion);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "beginAppearanceTransition:animated:")
    public native void beginAppearanceTransition(boolean isAppearing, boolean animated);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "endAppearanceTransition")
    public native void endAppearanceTransition();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "setOverrideTraitCollection:forChildViewController:")
    public native void setOverrideTraitCollection(UITraitCollection collection, UIViewController childViewController);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "overrideTraitCollectionForChildViewController:")
    public native UITraitCollection getOverrideTraitCollection(UIViewController childViewController);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 8.0. Manually forward viewWillTransitionToSize:withTransitionCoordinator: if necessary
     */
    @Deprecated
    @Method(selector = "shouldAutomaticallyForwardRotationMethods")
    public native boolean shouldAutomaticallyForwardRotationMethods();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "willMoveToParentViewController:")
    public native void willMoveToParentViewController(UIViewController parent);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "didMoveToParentViewController:")
    public native void didMoveToParentViewController(UIViewController parent);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "encodeRestorableStateWithCoder:")
    public native void encodeRestorableState(NSCoder coder);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "decodeRestorableStateWithCoder:")
    public native void decodeRestorableState(NSCoder coder);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "applicationFinishedRestoringState")
    public native void applicationFinishedRestoringState();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "updateViewConstraints")
    public native void updateViewConstraints();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "viewLayoutMarginsDidChange")
    public native void viewLayoutMarginsDidChange();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "viewSafeAreaInsetsDidChange")
    public native void viewSafeAreaInsetsDidChange();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "addKeyCommand:")
    public native void addKeyCommand(UIKeyCommand keyCommand);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "removeKeyCommand:")
    public native void removeKeyCommand(UIKeyCommand keyCommand);
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 13.0.
     */
    @Deprecated
    @Method(selector = "registerForPreviewingWithDelegate:sourceView:")
    public native UIViewControllerPreviewing registerForPreviewing(UIViewControllerPreviewingDelegate delegate, UIView sourceView);
    /**
     * @since Available in iOS 9.0 and later.
     * @deprecated Deprecated in iOS 13.0.
     */
    @Deprecated
    @Method(selector = "unregisterForPreviewingWithContext:")
    public native void unregisterForPreviewing(UIViewControllerPreviewing previewing);
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "setNeedsUpdateOfScreenEdgesDeferringSystemGestures")
    public native void setNeedsUpdateOfScreenEdgesDeferringSystemGestures();
    /**
     * @since Available in iOS 11.0 and later.
     */
    @Method(selector = "setNeedsUpdateOfHomeIndicatorAutoHidden")
    public native void setNeedsUpdateOfHomeIndicatorAutoHidden();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "setToolbarItems:animated:")
    public native void setToolbarItems(NSArray<UIBarButtonItem> toolbarItems, boolean animated);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "collapseSecondaryViewController:forSplitViewController:")
    public native void collapseSecondaryViewController(UIViewController secondaryViewController, UISplitViewController splitViewController);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "separateSecondaryViewControllerForSplitViewController:")
    public native UIViewController separateSecondaryViewController(UISplitViewController splitViewController);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "traitCollectionDidChange:")
    public native void traitCollectionDidChange(UITraitCollection previousTraitCollection);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "preferredContentSizeDidChangeForChildContentContainer:")
    public native void preferredContentSizeDidChangeForChildContentContainer(UIContentContainer container);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "systemLayoutFittingSizeDidChangeForChildContentContainer:")
    public native void systemLayoutFittingSizeDidChangeForChildContentContainer(UIContentContainer container);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "sizeForChildContentContainer:withParentContainerSize:")
    public native @ByVal CGSize sizeForChildContentContainer(UIContentContainer container, @ByVal CGSize parentSize);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "viewWillTransitionToSize:withTransitionCoordinator:")
    public native void viewWillTransitionToSize(@ByVal CGSize size, UIViewControllerTransitionCoordinator coordinator);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "willTransitionToTraitCollection:withTransitionCoordinator:")
    public native void willTransitionToTraitCollection(UITraitCollection newCollection, UIViewControllerTransitionCoordinator coordinator);
    @Method(selector = "setNeedsFocusUpdate")
    public native void setNeedsFocusUpdate();
    @Method(selector = "updateFocusIfNeeded")
    public native void updateFocusIfNeeded();
    @Method(selector = "shouldUpdateFocusInContext:")
    public native boolean shouldUpdateFocus(UIFocusUpdateContext context);
    @Method(selector = "didUpdateFocusInContext:withAnimationCoordinator:")
    public native void didUpdateFocus(UIFocusUpdateContext context, UIFocusAnimationCoordinator coordinator);
    @Method(selector = "beginRequestWithExtensionContext:")
    public native void beginRequest(NSExtensionContext context);
    /*</methods>*/
}
