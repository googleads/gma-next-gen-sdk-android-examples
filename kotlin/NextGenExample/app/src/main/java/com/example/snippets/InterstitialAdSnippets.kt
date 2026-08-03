// Copyright 2025 Google LLC
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
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader

/** Kotlin code snippets for the developer guide. */
private class InterstitialAdSnippets {

  private var interstitialAd: InterstitialAd? = null

  // [START start_preload]
  private fun startPreloading(adUnitId: String) {
    // Call start() once after SDK initialization.
    // Preload only one ad unit per format to optimize performance.
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    InterstitialAdPreloader.start(adUnitId, preloadConfig)
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
            ("Interstitial preload ad $preloadId failed to load with error: ${adError.message}"),
          )
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "Interstitial preload ad $preloadId is not available")
          // [Important] Don't call ad preloader start() or pollAd() from onAdsExhausted.
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "Interstitial preload ad $preloadId is available")
        }
      }
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback)
    // [END start_preload_with_callback]
  }

  // [START pollAndShowAd]
  private fun pollAndShowAd(activity: Activity, adUnitId: String) {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = InterstitialAdPreloader.pollAd(adUnitId)
    if (ad == null) {
      Log.e(TAG, "Interstitial ad is not available.")
      return
    }

    // Interact with the ad object as needed.
    Log.d(TAG, "Interstitial ad response info: ${ad.getResponseInfo()}")
    ad.adEventCallback =
      object : InterstitialAdEventCallback {
        override fun onAdImpression() {
          Log.d(TAG, "Interstitial ad recorded an impression.")
        }
      }
    ad.show(activity)
  }

  // [END pollAndShowAd]

  private fun peekAdResponseInfo(preloadId: String) {
    // [START peek_ad]
    val responseInfo = InterstitialAdPreloader.peekAdResponseInfo(preloadId)
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.")
      return
    }

    Log.d(TAG, "Peeked ad response ID: ${responseInfo.responseId}")
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private fun isAdAvailable(adUnitId: String): Boolean {
    return InterstitialAdPreloader.isAdAvailable(adUnitId)
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private fun stopPreloading(adUnitId: String) {
    // Stops the preloading and destroy preloaded ads.
    InterstitialAdPreloader.destroy(adUnitId)
  }

  // [END stop_preload]

  // [START set_buffer_size]
  private fun setBufferSize(adUnitId: String) {
    val adRequest = AdRequest.Builder(adUnitId).build()
    // Define a PreloadConfiguration and set the buffer size to 2 preloaded ads.
    val preloadConfig = PreloadConfiguration(adRequest, bufferSize = 2)
    InterstitialAdPreloader.start(adUnitId, preloadConfig)
  }

  // [END set_buffer_size]

  private fun loadSingleAd(activity: Activity, adUnitId: String) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    InterstitialAd.load(
      AdRequest.Builder(adUnitId).build(),
      object : AdLoadCallback<InterstitialAd> {
        override fun onAdLoaded(ad: InterstitialAd) {
          // Interstitial ad loaded.
          interstitialAd = ad
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          // Interstitial ad failed to load.
          Log.e(TAG, "Interstitial ad failed to load: ${adError.message}")
          interstitialAd = null
        }
      },
    )
    // [END single_load]
  }

  private fun listenToAdEvents() {
    // [START listen_events]
    // Listen for ad events.
    val ad = interstitialAd
    if (ad == null) {
      Log.e(TAG, "Interstitial ad is not ready yet.")
      return
    }

    ad.adEventCallback =
      object : InterstitialAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          // Interstitial ad did show.
        }

        override fun onAdDismissedFullScreenContent() {
          // Interstitial ad did dismiss.
          interstitialAd = null
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Interstitial ad failed to show.
          Log.e(TAG, "Interstitial ad failed to show: ${fullScreenContentError.message}")
        }

        override fun onAdImpression() {
          // Interstitial ad did record an impression.
        }

        override fun onAdClicked() {
          // Interstitial ad did record a click.
        }
      }
    // [END listen_events]
  }

  // [START show_ad]
  private fun showAd(interstitialAd: InterstitialAd, activity: Activity) {
    // Show the ad.
    interstitialAd.show(activity)
  }

  // [END show_ad]

  private companion object {
    const val TAG = "InterstitialAdSnippets"
  }
}
