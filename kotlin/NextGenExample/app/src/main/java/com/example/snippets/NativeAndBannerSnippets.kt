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

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.NativeAdType
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

/** Kotlin code snippets for the developer guide. */
private class NativeAndBannerSnippets {

  private fun loadNativeAndBannerAd() {
    // [START load_native_and_banner]
    val adRequest =
      NativeAdRequest.Builder(AD_UNIT_ID, listOf(NativeAdType.NATIVE, NativeAdType.BANNER))
        // Use setAdSize() or setAdSizes() depending on if you want multiple ad sizes or not.
        .setAdSizes(listOf(AdSize.BANNER, AdSize.LARGE_BANNER))
        .build()

    // Load the native and banner ad with the ad request and callback.
    NativeAdLoader.load(adRequest, getNativeAdLoaderCallback())
    // [END load_native_and_banner]
  }

  // [START native_and_banner_callback]
  private fun getNativeAdLoaderCallback(): NativeAdLoaderCallback {
    return object : NativeAdLoaderCallback {
      override fun onNativeAdLoaded(nativeAd: NativeAd) {
        // Called when a native ad has loaded.
      }

      override fun onBannerAdLoaded(bannerAd: BannerAd) {
        // Called when a banner ad has loaded.
      }
    }
  }

  // [END native_and_banner_callback]

  private fun registerNativeAdWithoutMediaView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
    // [START register_native_ad_null]
    nativeAdView.registerNativeAd(nativeAd, null)
    // [END register_native_ad_null]
  }

  private companion object {
    const val AD_UNIT_ID = "/21775744923/example/native-and-banner"
  }
}
