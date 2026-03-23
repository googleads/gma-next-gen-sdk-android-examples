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
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerSignalRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeSignalRequest
import com.google.android.libraries.ads.mobile.sdk.signal.Signal
import com.google.android.libraries.ads.mobile.sdk.signal.SignalError
import com.google.android.libraries.ads.mobile.sdk.signal.SignalGenerationCallback

/** Kotlin code snippets for the developer guide. */
class AdManagerSCARSnippets {

  fun loadNative(activity: Activity, adUnitID: String) {
    // [START signal_request_native]
    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    val signalRequest: NativeSignalRequest =
      NativeSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setNativeAdTypes(listOf(NativeAd.NativeAdType.NATIVE))
        .setAdUnitId(adUnitID)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "Signal string: " + signal.signalString)
          // Fetch the ad response using your generated signal.
          val adResponseString = fetchAdResponseString(signal)
          renderNative(activity, adResponseString)
        }

        override fun onFailure(error: SignalError) {
          Log.e(TAG, "Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_native]
  }

  fun loadBanner(activity: Activity, adUnitID: String) {
    // [START signal_request_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    val size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, 320)

    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    val signalRequest: BannerSignalRequest =
      BannerSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setAdUnitId(adUnitID)
        .setAdSize(size)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "Signal string: " + signal.signalString)
          // Fetch the ad response using your generated signal.
          val adResponseString = fetchAdResponseString(signal)
          renderBanner(activity, adResponseString)
        }

        override fun onFailure(error: SignalError) {
          Log.e(TAG, "Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_banner]
  }

  fun loadNativePlusBanner(activity: Activity, adUnitID: String) {
    // [START signal_request_native_plus_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    val size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, 320)

    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    val signalRequest: NativeSignalRequest =
      NativeSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setNativeAdTypes(listOf(NativeAd.NativeAdType.NATIVE, NativeAd.NativeAdType.BANNER))
        .setAdUnitId(adUnitID)
        .setAdSize(size)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "Signal string: " + signal.signalString)
          // Fetch the ad response using your generated signal.
          val adResponseString = fetchAdResponseString(signal)
          renderNativePlusBanner(activity, adResponseString)
        }

        override fun onFailure(error: SignalError) {
          Log.e(TAG, "Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_native_plus_banner]
  }

  fun loadNativeWithOptions(activity: Activity, adUnitID: String) {
    // [START native_ad_options]
    val videoOptions = VideoOptions.Builder().setStartMuted(true).build()

    val signalRequest: NativeSignalRequest =
      NativeSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setNativeAdTypes(listOf(NativeAd.NativeAdType.NATIVE))
        .setAdUnitId(adUnitID)
        .setVideoOptions(videoOptions)
        .build()

    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "Signal string: " + signal.signalString)
          // Fetch the ad response using your generated signal.
          val adResponseString = fetchAdResponseString(signal)
          renderNative(activity, adResponseString)
        }

        override fun onFailure(error: SignalError) {
          Log.e(TAG, "Error generating signal: ${error.message}")
        }
      },
    )
    // [END native_ad_options]
  }

  // [START fetch_response]
  // Emulates a request to your ad server.
  private fun fetchAdResponseString(signal: Signal): String {
    // This value is generated dynamically at runtime from your ad server.
    return "Ad response"
  }

  // [END fetch_response]

  fun renderNative(activity: Activity, adResponseString: String) {
    // [START render_native]
    val nativeAdView = NativeAdView(context = activity)
    NativeAdLoader.loadFromAdResponse(
      adResponseString,
      object : NativeAdLoaderCallback {
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
          Log.d(TAG, "Native ad rendered.")
          // TODO : Set native ad assets.
          nativeAdView.registerNativeAd(nativeAd, mediaView = null)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.e(TAG, "Error rendering native ad: ${adError.message}")
        }
      },
    )
    // [END render_native]
  }

  fun renderBanner(activity: Activity, adResponseString: String) {
    // [START render_banner]
    val adView = AdView(activity)
    adView.loadFromAdResponse(
      adResponseString,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          Log.d(TAG, "Banner ad rendered.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.e(TAG, "Error rendering banner ad: ${adError.message}")
        }
      },
    )
    // [END render_banner]
  }

  fun renderNativePlusBanner(activity: Activity, adResponseString: String) {
    // [START render_native_plus_banner]
    NativeAdLoader.loadFromAdResponse(
      adResponseString,
      object : NativeAdLoaderCallback {
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
          Log.d(TAG, "Native ad rendered.")
          val nativeAdView = NativeAdView(context = activity)
          // TODO : Set native ad assets.
          nativeAdView.registerNativeAd(nativeAd, mediaView = null)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.e(TAG, "Error rendering native ad: ${adError.message}")
        }

        override fun onBannerAdLoaded(bannerAd: BannerAd) {
          Log.d(TAG, "Banner ad rendered.")
          val adView = AdView(activity)
          adView.registerBannerAd(bannerAd, activity)
        }
      },
    )
    // [END render_native_plus_banner]
  }

  companion object {
    private const val TAG = "AdManagerSCARSnippets"
  }
}
