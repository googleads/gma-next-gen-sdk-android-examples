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

package com.example.snippets

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.VideoController
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdRequest

/** Code snippets for swipeable interstitial Ads in Kotlin. */
@OptIn(ExperimentalApi::class)
class SwipeableInterstitialSnippets(
  private val activity: Activity,
  private val adContainer: FrameLayout,
) {

  private var swipeableAd: SwipeableInterstitialAd? = null

  // [START swipeable_interstitial_load]
  fun loadSwipeableInterstitialAd() {
    val request = SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID).build()
    SwipeableInterstitialAd.load(
      request,
      object : AdLoadCallback<SwipeableInterstitialAd> {
        override fun onAdLoaded(ad: SwipeableInterstitialAd) {
          swipeableAd = ad
          // Handle the ad load success.
          Log.d(TAG, "Ad loaded.")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          // Handle the ad load failure.
          Log.d(TAG, "Ad failed to load: $error")
        }
      },
    )
  }

  // [END swipeable_interstitial_load]

  // [START swipeable_interstitial_options_screen_hold]
  fun createSwipeableInterstitialRequestWithScreenHold(
    maxScreenHoldDurationSeconds: Int
  ): SwipeableInterstitialAdRequest {
    return SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
      .setMaxScreenHoldDurationSeconds(maxScreenHoldDurationSeconds)
      .build()
  }

  // [END swipeable_interstitial_options_screen_hold]

  // [START register_ad_event_callbacks]
  fun registerAdEventCallbacks(swipeableAd: SwipeableInterstitialAd) {
    swipeableAd.adEventCallback =
      object : SwipeableInterstitialAdEventCallback {
        override fun onAdClicked() {
          // Called when a click is recorded for an ad.
          Log.d(TAG, "Ad recorded a click.")
        }

        override fun onAdDismissedFullScreenContent() {
          // Called when the ad full screen content is dismissed.
          Log.d(TAG, "Ad dismissed full screen content.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Called when ad full screen content failed to show.
          Log.d(TAG, "Ad failed to show full screen content.")
        }

        override fun onAdImpression() {
          // Called when an impression is recorded for an ad.
          Log.d(TAG, "Ad recorded an impression.")
        }

        override fun onAdShowedFullScreenContent() {
          // Called when ad full screen content has been shown.
          Log.d(TAG, "Ad showed full screen content.")
        }
      }
  }

  // [END register_ad_event_callbacks]

  // [START register_screen_hold_callback]
  fun registerScreenHoldCallback(swipeableAd: SwipeableInterstitialAd) {
    swipeableAd.adEventCallback =
      object : SwipeableInterstitialAdEventCallback {
        override fun onAdScreenHoldTimerStarted() {
          Log.d(TAG, "Ad started screen hold timer.")
        }
      }
  }

  // [END register_screen_hold_callback]

  // [START show_swipeable_interstitial]
  fun showSwipeableInterstitialAd(swipeableAd: SwipeableInterstitialAd) {
    // Add the swipeable interstitial ad view to your swipeable container.
    adContainer.addView(swipeableAd.getView(activity))
  }

  // [END show_swipeable_interstitial]

  // [START check_min_screen_hold_duration]
  fun holdScreen() {
    val ad = swipeableAd ?: return
    // If screen hold is not enabled, the value will be 0.
    val holdTime = ad.getScreenHoldDuration()
    if (holdTime <= 0) {
      return
    }

    // Disable scrolling during screen hold.
    disableScrolling()

    // Post a delayed action to unlock the interface once elapsed.
    Handler(Looper.getMainLooper()).postDelayed({ enableScrolling() }, holdTime * 1000L)
  }

  fun disableScrolling() {
    // TODO: Disable scrolling.
  }

  fun enableScrolling() {
    // TODO: Enable scrolling.
  }

  // [END check_min_screen_hold_duration]

  fun setCustomClickGesture() {
    // [START set_custom_click_gesture]
    // Optional: Custom click gestures require a separate allowlisting with your
    // account manager. This feature is intended for apps that use swipe
    // gestures to click on content.
    val request =
      SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
        .enableCustomClickSwipeGesture(NativeAd.SwipeGestureDirection.RIGHT, true)
        .build()
    // [END set_custom_click_gesture]
  }

  // [START prevent_overscroll_artifacts]
  fun preventOverscrollArtifacts(viewPager: ViewPager2) {
    // Disable overscroll rubber-banding on the internal RecyclerView to prevent
    // GPU SurfaceView desynchronization.
    for (i in 0 until viewPager.childCount) {
      val child = viewPager.getChildAt(i)
      if (child is RecyclerView) {
        child.overScrollMode = View.OVER_SCROLL_NEVER
        break
      }
    }
  }

  // [END prevent_overscroll_artifacts]

  // [START swipeable_interstitial_options_ad_size]
  fun createSwipeableInterstitialRequest(adSize: AdSize): SwipeableInterstitialAdRequest {
    return SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
      // Optional: Overrides the full screen ad size with a custom size.
      .setAdSize(adSize.width, adSize.height)
      .build()
  }

  // [END swipeable_interstitial_options_ad_size]

  fun discardAd() {
    // [START discard_swipeable_interstitial]
    swipeableAd?.destroy()
    swipeableAd = null
    // [END discard_swipeable_interstitial]
  }

  // [START video_lifecycle_callbacks]
  fun registerVideoLifecycleCallbacks(swipeableAd: SwipeableInterstitialAd) {
    swipeableAd.videoLifecycleCallbacks =
      object : VideoController.VideoLifecycleCallbacks {
        override fun onVideoStart() {
          Log.d(TAG, "Video started.")
        }

        override fun onVideoPlay() {
          Log.d(TAG, "Video played.")
        }

        override fun onVideoPause() {
          Log.d(TAG, "Video paused.")
        }

        override fun onVideoEnd() {
          Log.d(TAG, "Video ended.")
        }

        override fun onVideoMute(isMuted: Boolean) {
          Log.d(TAG, "Video mute: $isMuted")
        }
      }
  }

  // [END video_lifecycle_callbacks]

  private companion object {
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TAG = "SwipeableInterstitialSnippets"
  }
}
