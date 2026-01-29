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
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader

/** Kotlin code snippets for the developer guide. */
private class InterstitialAdSnippets {

  // [START start_preload]
  private fun startPreloading(adUnitId: String) {
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    InterstitialAdPreloader.start(adUnitId, preloadConfig)
  }

  // [END start_preload]

  // [START set_buffer_size]
  private fun setBufferSize(adUnitId: String) {
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest, bufferSize = 3)
    InterstitialAdPreloader.start(adUnitId, preloadConfig)
  }

  // [END set_buffer_size]

  // [START set_callback]
  private fun startPreloadingWithCallback(adUnitId: String) {
    val preloadCallback =
      // [Important] Don't call ad preloader start() or pollAd() within the PreloadCallback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.i(
            TAG,
            ("Interstitial preload ad $preloadId failed to load with error: ${adError.message}"),
          )
          // [Optional] Get the error response info for additional details.
          // val responseInfo = adError.responseInfo
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
  }

  // [END set_callback]

  private fun pollAd(adUnitId: String) {
    // [START poll_ad]
    // Polling returns the next available ad and loads another ad in the background.
    val ad = InterstitialAdPreloader.pollAd(adUnitId)
    // [END poll_ad]
  }

  // [START pollAndShowAd]
  private fun pollAndShowAd(activity: Activity, adUnitId: String) {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = InterstitialAdPreloader.pollAd(adUnitId)

    // Interact with the ad object as needed.
    ad?.apply {
      Log.d(TAG, "Interstitial ad response info: ${this.getResponseInfo()}")
      this.adEventCallback =
        object : InterstitialAdEventCallback {
          override fun onAdImpression() {
            Log.d(TAG, "Interstitial ad recorded an impression.")
          }

          override fun onAdPaid(value: AdValue) {
            Log.d(TAG, "Interstitial ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}")
          }
        }

      // Show the ad.
      ad.show(activity)
    }
  }

  // [END pollAndShowAd]

  // [START isAdAvailable]
  private fun isAdAvailable(adUnitId: String): Boolean {
    return InterstitialAdPreloader.isAdAvailable(adUnitId)
  }

  // [END isAdAvailable]

  private companion object {
    const val TAG = "InterstitialAdSnippets"
  }
}
