// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.snippets

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdPreloader

/** Kotlin code snippets for the developer guide. */
private class RewardedInterstitialAdSnippets {

  private var rewardedInterstitialAd: RewardedInterstitialAd? = null

  // [START start_preload]
  private fun startPreloading(adUnitId: String) {
    // Call start() once after SDK initialization.
    // Preload only one ad unit per format to optimize performance.
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig)
  }

  // [END start_preload]

  private fun startPreloadingWithCallback(adUnitId: String) {
    // [START start_preload_with_callback]
    val preloadCallback =
      // [Important] Don't call ad preloader start() or pollAd() within the PreloadCallback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.d(
            TAG,
            "Rewarded interstitial preload ad $preloadId failed to load with error: ${adError.message}",
          )
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "Rewarded interstitial preload ad $preloadId is not available")
          // [Important] Don't call ad preloader start() or pollAd() from onAdsExhausted.
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "Rewarded interstitial preload ad $preloadId is available")
        }
      }
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback)
    // [END start_preload_with_callback]
  }

  // [START pollAndShowAd]
  private fun pollAndShowAd(activity: Activity, adUnitId: String) {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = RewardedInterstitialAdPreloader.pollAd(adUnitId)
    if (ad == null) {
      Log.e(TAG, "Rewarded interstitial ad is not available.")
      return
    }

    // Interact with the ad object as needed.
    Log.d(TAG, "Rewarded interstitial ad response info: ${ad.getResponseInfo()}")
    ad.adEventCallback =
      object : RewardedInterstitialAdEventCallback {
        override fun onAdImpression() {
          Log.d(TAG, "Rewarded interstitial ad recorded an impression.")
        }
      }
    ad.show(activity) { rewardItem -> Log.d(TAG, "User earned reward: ${rewardItem.amount}") }
  }

  // [END pollAndShowAd]

  private fun peekAdResponseInfo(preloadId: String) {
    // [START peek_ad]
    val responseInfo = RewardedInterstitialAdPreloader.peekAdResponseInfo(preloadId)
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.")
      return
    }

    Log.d(TAG, "Peeked ad response ID: ${responseInfo.responseId}")
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private fun isAdAvailable(adUnitId: String): Boolean {
    return RewardedInterstitialAdPreloader.isAdAvailable(adUnitId)
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private fun stopPreloading(adUnitId: String) {
    // Stops the preloading and destroy preloaded ads.
    RewardedInterstitialAdPreloader.destroy(adUnitId)
  }

  // [END stop_preload]

  // [START set_buffer_size]
  private fun setBufferSize(adUnitId: String) {
    val adRequest = AdRequest.Builder(adUnitId).build()
    // Define a PreloadConfiguration and set the buffer size to 2 preloaded ads.
    val preloadConfig = PreloadConfiguration(adRequest, bufferSize = 2)
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig)
  }

  // [END set_buffer_size]

  // [START listen_events]
  private fun listenToAdEvents() {
    // Listen for ad events.
    val ad = rewardedInterstitialAd
    if (ad == null) {
      Log.e(TAG, "Rewarded interstitial ad is not ready yet.")
      return
    }

    ad.adEventCallback =
      object : RewardedInterstitialAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          // Rewarded interstitial ad did show.
        }

        override fun onAdDismissedFullScreenContent() {
          // Rewarded interstitial ad did dismiss.
          rewardedInterstitialAd = null
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Rewarded interstitial ad failed to show.
          Log.e(TAG, "Rewarded interstitial ad failed to show: ${fullScreenContentError.message}")
        }

        override fun onAdImpression() {
          // Rewarded interstitial ad did record an impression.
        }

        override fun onAdClicked() {
          // Rewarded interstitial ad did record a click.
        }
      }
  }

  // [END listen_events]

  // [START show_ad]
  private fun showAd(rewardedInterstitialAd: RewardedInterstitialAd, activity: Activity) {
    // Show the ad.
    rewardedInterstitialAd.show(
      activity,
      object : OnUserEarnedRewardListener {
        override fun onUserEarnedReward(rewardItem: RewardItem) {
          // User earned the reward.
          val rewardAmount = rewardItem.amount
          val rewardType = rewardItem.type
        }
      },
    )
  }

  // [END show_ad]

  private fun loadSingleAd(activity: Activity, adUnitId: String) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    RewardedInterstitialAd.load(
      AdRequest.Builder(adUnitId).build(),
      object : AdLoadCallback<RewardedInterstitialAd> {
        override fun onAdLoaded(ad: RewardedInterstitialAd) {
          // Rewarded interstitial ad loaded.
          rewardedInterstitialAd = ad
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          // Rewarded interstitial ad failed to load.
          Log.e(TAG, "Rewarded interstitial ad failed to load: ${adError.message}")
          rewardedInterstitialAd = null
        }
      },
    )
    // [END single_load]
  }

  private companion object {
    const val TAG = "RewardedInterstitialAdSnippets"
  }
}
