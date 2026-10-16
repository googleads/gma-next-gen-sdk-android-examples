/*
 * Copyright 2026 Google LLC
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

package com.example.snippets;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.VideoController;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdRequest;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialSignalRequest;

/** Code snippets for Swipeable Interstitial Ads in Java. */
@ExperimentalApi
public class SwipeableInterstitialSnippets {

  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379";
  private static final String TAG = "SwipeableInterstitialSnippets";
  private SwipeableInterstitialAd swipeableAd;

  // [START swipeable_interstitial_load]
  public void loadSwipeableInterstitialAd() {
    SwipeableInterstitialAdRequest request =
        new SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID).build();
    SwipeableInterstitialAd.load(
        request,
        new AdLoadCallback<SwipeableInterstitialAd>() {
          @Override
          public void onAdLoaded(@NonNull SwipeableInterstitialAd ad) {
            swipeableAd = ad;
            // Handle the ad load success.
            Log.d(TAG, "Ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError error) {
            // Handle the ad load failure.
            Log.d(TAG, "Ad failed to load: " + error);
          }
        });
  }

  // [END swipeable_interstitial_load]

  // [START swipeable_interstitial_load_from_ad_response]
  public void loadSwipeableInterstitialAdFromAdResponse(String adResponseString) {
    SwipeableInterstitialAd.loadFromAdResponse(
        adResponseString,
        new AdLoadCallback<SwipeableInterstitialAd>() {
          @Override
          public void onAdLoaded(@NonNull SwipeableInterstitialAd ad) {
            swipeableAd = ad;
            // Handle the ad load success.
            Log.d(TAG, "Ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError error) {
            // Handle the ad load failure.
            Log.d(TAG, "Ad failed to load: " + error);
          }
        });
  }

  // [END swipeable_interstitial_load_from_ad_response]

  // [START swipeable_interstitial_signal_request]
  public SwipeableInterstitialSignalRequest createSwipeableInterstitialSignalRequest(
      String signalType) {
    return new SwipeableInterstitialSignalRequest.Builder(signalType)
        .setAdUnitId(AD_UNIT_ID)
        .setMaxScreenHoldDurationSeconds(5)
        .enableCustomClickSwipeGesture(NativeAd.SwipeGestureDirection.RIGHT, true)
        .build();
  }

  // [END swipeable_interstitial_signal_request]

  // [START swipeable_interstitial_options_screen_hold]
  public SwipeableInterstitialAdRequest createSwipeableInterstitialRequestWithScreenHold(
      int maxScreenHoldDurationSeconds) {
    return new SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
        .setMaxScreenHoldDurationSeconds(maxScreenHoldDurationSeconds)
        .build();
  }

  // [END swipeable_interstitial_options_screen_hold]

  // [START register_ad_event_callbacks]
  public void registerAdEventCallbacks(SwipeableInterstitialAd swipeableAd) {
    swipeableAd.setAdEventCallback(
        new SwipeableInterstitialAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Called when ad full screen content has been shown.
            Log.d(TAG, "Ad showed full screen content.");
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Called when the ad full screen content is dismissed.
            Log.d(TAG, "Ad dismissed full screen content.");
          }

          @Override
          public void onAdImpression() {
            // Called when an impression is recorded for an ad.
            Log.d(TAG, "Ad recorded an impression.");
          }

          @Override
          public void onAdClicked() {
            // Called when a click is recorded for an ad.
            Log.d(TAG, "Ad recorded a click.");
          }

          @Override
          public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
            // Called when ad full screen content failed to show.
            Log.d(TAG, "Ad failed to show full screen content.");
          }
        });
  }

  // [END register_ad_event_callbacks]

  // [START register_screen_hold_callback]
  public void registerScreenHoldCallback(SwipeableInterstitialAd swipeableAd) {
    swipeableAd.setAdEventCallback(
        new SwipeableInterstitialAdEventCallback() {
          @Override
          public void onAdScreenHoldTimerStarted() {
            Log.d(TAG, "Ad started screen hold timer.");
          }
        });
  }

  // [END register_screen_hold_callback]

  // [START show_swipeable_interstitial]
  public void showSwipeableInterstitialAd(
      SwipeableInterstitialAd swipeableAd, Activity activity, FrameLayout adContainer) {
    // Add the swipeable interstitial ad view to your swipeable container.
    adContainer.addView(swipeableAd.getView(activity));
  }

  // [END show_swipeable_interstitial]

  // [START check_min_screen_hold_duration]
  public void holdScreen() {
    if (swipeableAd != null) {
      // If screen hold is not enabled, the value will be 0.
      int holdTime = swipeableAd.getScreenHoldDuration();
      if (holdTime <= 0) {
        return;
      }

      // Disable scrolling during screen hold.
      disableScrolling();

      // Post a delayed action to unlock the interface once elapsed.
      new Handler(Looper.getMainLooper()).postDelayed(() -> enableScrolling(), holdTime * 1000L);
    }
  }

  public void disableScrolling() {
    // TODO: Disable scrolling.
  }

  public void enableScrolling() {
    // TODO: Enable scrolling.
  }

  // [END check_min_screen_hold_duration]

  public void setCustomClickGesture() {
    // [START set_custom_click_gesture]
    // Optional: Custom click gestures require a separate allowlisting with your
    // account manager. This feature is intended for apps that use swipe
    // gestures to click on content.
    SwipeableInterstitialAdRequest request =
        new SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
            .enableCustomClickSwipeGesture(NativeAd.SwipeGestureDirection.RIGHT, true)
            .build();
    // [END set_custom_click_gesture]
  }

  // [START prevent_overscroll_artifacts]
  public void preventOverscrollArtifacts(ViewPager2 viewPager) {
    // Disable overscroll rubber-banding on the internal RecyclerView to prevent
    // GPU SurfaceView desynchronization.
    for (int i = 0; i < viewPager.getChildCount(); i++) {
      View child = viewPager.getChildAt(i);
      if (child instanceof RecyclerView) {
        child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        break;
      }
    }
  }

  // [END prevent_overscroll_artifacts]

  // [START swipeable_interstitial_options_ad_size]
  public SwipeableInterstitialAdRequest createSwipeableInterstitialRequest(AdSize adSize) {
    return new SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
        // Optional: Overrides the default fullscreen size with a custom size.
        // Custom ad sizes must fill at least 60% of the screen size.
        .setAdSize(adSize.getWidth(), adSize.getHeight())
        .build();
  }

  // [END swipeable_interstitial_options_ad_size]

  public void discardAd() {
    // [START discard_swipeable_interstitial]
    if (swipeableAd != null) {
      swipeableAd.destroy();
      swipeableAd = null;
    }
    // [END discard_swipeable_interstitial]
  }

  // [START video_lifecycle_callbacks]
  public void registerVideoLifecycleCallbacks(SwipeableInterstitialAd swipeableAd) {
    swipeableAd.setVideoLifecycleCallbacks(
        new VideoController.VideoLifecycleCallbacks() {
          @Override
          public void onVideoStart() {
            // Optional: Pause any app video content when the ad video starts.
            Log.d(TAG, "Video started.");
          }

          @Override
          public void onVideoPlay() {
            // Optional: Pause any app video content when the ad video plays.
            Log.d(TAG, "Video played.");
          }

          @Override
          public void onVideoPause() {
            // Optional: Resume app video content or handle ad video pause.
            Log.d(TAG, "Video paused.");
          }

          @Override
          public void onVideoEnd() {
            // Optional: Resume app video content or handle video completion.
            Log.d(TAG, "Video ended.");
          }

          @Override
          public void onVideoMute(boolean isMuted) {
            // Optional: Handle ad video mute state changes.
            Log.d(TAG, "Video mute state changed: " + isMuted);
          }
        });
  }
  // [END video_lifecycle_callbacks]
}
