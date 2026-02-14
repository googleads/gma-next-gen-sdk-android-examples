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
import android.widget.FrameLayout
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** Kotlin code snippets for the developer guide. */
private class BannerSnippets {

  private fun createCustomAdSize() {
    // [START create_custom_ad_size]
    val customAdSize = AdSize(250, 250)
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, customAdSize).build()
    // [END create_custom_ad_size]
  }

  private fun createMultipleAdSizes() {
    // [START create_multiple_ad_sizes]
    val adSizes = listOf(AdSize(120, 20), AdSize.BANNER, AdSize.MEDIUM_RECTANGLE)
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, adSizes).build()
    // [END create_multiple_ad_sizes]
  }

  // [START create_ad_view]
  private fun createAdView(adViewContainer: FrameLayout, activity: Activity) {
    val adView = AdView(activity)
    adViewContainer.addView(adView)
  }

  // [END create_ad_view]

  // [START load_ad]
  private fun loadBannerAd(adView: AdView, activity: Activity) {
    // Get a BannerAdRequest for a 360 wide large anchored adaptive banner ad.
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, 360)
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()

    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          Log.d(TAG, "Banner ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.d(TAG, "Banner ad failed to load: $adError")
        }
      },
    )
  }

  // [END load_ad]

  private fun loadBannerAdWithAdEvents(adView: AdView, activity: Activity) {
    // Get a BannerAdRequest for a 360 wide large anchored adaptive banner ad.
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, 360)
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()

    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        // [START ad_events]
        override fun onAdLoaded(ad: BannerAd) {
          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                // Banner ad recorded an impression.
                Log.d(TAG, "Banner ad recorded an impression.")
              }

              override fun onAdClicked() {
                // Banner ad recorded a click.
                Log.d(TAG, "Banner ad clicked.")
              }

              override fun onAdShowedFullScreenContent() {
                // Banner ad showed.
                Log.d(TAG, "Banner ad showed full screen content.")
              }

              override fun onAdDismissedFullScreenContent() {
                // Banner ad dismissed.
                Log.d(TAG, "Banner ad dismissed full screen content.")
              }

              override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError
              ) {
                // Banner ad failed to show.
                Log.w(TAG, "Banner ad failed to show full screen content: $fullScreenContentError")
              }
            }
        }

        // [END ad_events]

        override fun onAdFailedToLoad(adError: LoadAdError) {
          // Banner ad failed to load.
          Log.d(TAG, "Banner ad failed to load: $adError")
        }
      },
    )
  }

  private companion object {
    const val AD_UNIT_ID = "/21775744923/example/api-demo/ad-sizes"
    const val TAG = "BannerSnippets"
  }
}
