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

import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerSignalRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeSignalRequest
import com.google.android.libraries.ads.mobile.sdk.signal.Signal
import com.google.android.libraries.ads.mobile.sdk.signal.SignalError
import com.google.android.libraries.ads.mobile.sdk.signal.SignalGenerationCallback

/** Kotlin code snippets for the developer guide. */
class AdManagerSCARSnippets {

  fun loadNative(adUnitID: String) {
    // [START signal_request_native]
    // Create a signal request for an ad.
    // Specify the signal type as "signal_type_ad_manager_s2s" to denote that
    // the request is for Ad Manager S2S.
    val signalRequest: NativeSignalRequest =
      NativeSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setRequestAgent("REQUEST_AGENT")
        .setNativeAdTypes(listOf(NativeAd.NativeAdType.NATIVE))
        .setAdUnitId(adUnitID)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "QueryInfo string: " + signal.signalString)
          // TODO: Fetch the ad response using your generated query info.
        }

        override fun onFailure(error: SignalError) {
          print("Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_native]
  }

  fun loadBanner(applicationContext: Context, adUnitID: String) {
    // [START signal_request_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    val size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(applicationContext, 320)

    // Create a signal request for an ad.
    // Specify the signal type as "signal_type_ad_manager_s2s" to denote that
    // the request is for Ad Manager S2S.
    val signalRequest: BannerSignalRequest =
      BannerSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setRequestAgent("REQUEST_AGENT")
        .setAdUnitId(adUnitID)
        .setAdSize(size)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "QueryInfo string: " + signal.signalString)
          // TODO: Fetch the ad response using your generated query info.
        }

        override fun onFailure(error: SignalError) {
          print("Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_banner]
  }

  fun loadNativePlusBanner(applicationContext: Context, adUnitID: String) {
    // [START signal_request_native_plus_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    val size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(applicationContext, 320)

    // Create a signal request for an ad.
    // Specify the signal type as "signal_type_ad_manager_s2s" to denote that
    // the request is for Ad Manager S2S.
    val signalRequest: NativeSignalRequest =
      NativeSignalRequest.Builder(signalType = "signal_type_ad_manager_s2s")
        .setRequestAgent("REQUEST_AGENT")
        .setNativeAdTypes(listOf(NativeAd.NativeAdType.NATIVE, NativeAd.NativeAdType.BANNER))
        .setAdUnitId(adUnitID)
        .setAdSize(size)
        .build()

    // Generate and send the signal request.
    MobileAds.generateSignal(
      signalRequest,
      object : SignalGenerationCallback {
        override fun onSuccess(signal: Signal) {
          Log.d(TAG, "QueryInfo string: " + signal.signalString)
          // TODO: Fetch the ad response using your generated query info.
        }

        override fun onFailure(error: SignalError) {
          print("Error generating signal: ${error.message}")
        }
      },
    )
    // [END signal_request_native_plus_banner]
  }

  companion object {
    private const val TAG = "AdManagerSCARSnippets"
  }
}
