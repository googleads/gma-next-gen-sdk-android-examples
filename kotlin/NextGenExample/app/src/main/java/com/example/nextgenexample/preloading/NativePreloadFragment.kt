/*
 * Copyright 2025 Google LLC
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

package com.example.nextgenexample.preloading

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant.Companion.TAG
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentPreloadBinding
import com.example.nextgenexample.databinding.NativeAdBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult.*
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdPreloader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

/** A [AdFragment] subclass that preloads a native ad. */
class NativePreloadFragment : AdFragment<FragmentPreloadBinding>() {

  private var currentAd: NativeAd? = null

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPreloadBinding
    get() = FragmentPreloadBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Start preloading.
    startPreloadingWithCallback()

    // Initialize the UI.
    binding.txtTitle.text = getText(R.string.native_ad)
    binding.btnShow.setOnClickListener { sender ->
      pollAndShowAd()
      updateUI()
    }
    updateUI()
  }

  override fun onDestroyView() {
    // Always call destroy() on ads on removal.
    destroyCurrentAd()
    super.onDestroyView()
  }

  private fun destroyCurrentAd() {
    binding.placeholder.removeAllViews()
    if (currentAd != null) {
      currentAd?.destroy()
      currentAd = null
    }
  }

  private fun startPreloadingWithCallback() {
    val preloadCallback =
      // [Important] Do not call preload start of poll ad within the callback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.i(TAG, ("Native preload ad failed to load with error: " + adError.message))
          // [Optional] Get the error response info for additional details.
          // val responseInfo = adError.responseInfo
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "Native preload ad is not available")
          updateUI()
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "Native preload ad is available")
          updateUI()
        }
      }
    val videoOptions: VideoOptions = VideoOptions.Builder().setStartMuted(true).build()
    val adRequest =
      NativeAdRequest.Builder(AD_UNIT_ID, listOf(NativeAd.NativeAdType.NATIVE))
        .setVideoOptions(videoOptions)
        .build()
    val preload = PreloadConfiguration(adRequest)
    NativeAdPreloader.start(AD_UNIT_ID, preload, preloadCallback)
  }

  private fun pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    val result = NativeAdPreloader.pollAd(AD_UNIT_ID)

    if (result is NativeAdSuccess) {
      // Destroy the previous banner.
      destroyCurrentAd()
      val nativeAd = result.ad

      // Interact with the ad object as needed.
      nativeAd.apply {
        Log.d(TAG, "Banner ad response info: ${nativeAd.getResponseInfo()}")
        this.adEventCallback =
          object : NativeAdEventCallback {
            override fun onAdImpression() {
              Log.d(TAG, "App Open ad recorded an impression.")
            }

            override fun onAdPaid(value: AdValue) {
              Log.d(TAG, "Native ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}")
            }
          }

        // Show the ad.
        displayNativeAd(nativeAd)
        currentAd = nativeAd
      }
    }
  }

  private fun isAdAvailable(): Boolean {
    return NativeAdPreloader.isAdAvailable(AD_UNIT_ID)
  }

  @Synchronized
  fun updateUI() {
    runOnUiThread {
      if (isAdAvailable()) {
        binding.txtStatus.text = getString(R.string.available)
        binding.btnShow.isEnabled = true
      } else {
        binding.txtStatus.text = getString(R.string.exhausted)
        binding.btnShow.isEnabled = false
      }
    }
  }

  private fun displayNativeAd(nativeAd: NativeAd) {
    // Inflate the native ad view and add it to the view hierarchy.
    val nativeAdBinding = NativeAdBinding.inflate(layoutInflater)
    binding.placeholder.addView(nativeAdBinding.root)

    // Set the native ad view elements.
    val nativeAdView = nativeAdBinding.root
    nativeAdView.advertiserView = nativeAdBinding.adAdvertiser
    nativeAdView.bodyView = nativeAdBinding.adBody
    nativeAdView.callToActionView = nativeAdBinding.adCallToAction
    nativeAdView.headlineView = nativeAdBinding.adHeadline
    nativeAdView.iconView = nativeAdBinding.adIcon
    nativeAdView.priceView = nativeAdBinding.adPrice
    nativeAdView.starRatingView = nativeAdBinding.adStars
    nativeAdView.storeView = nativeAdBinding.adStore

    // Set the view element with the native ad assets.
    nativeAdBinding.adAdvertiser.text = nativeAd.advertiser
    nativeAdBinding.adBody.text = nativeAd.body
    nativeAdBinding.adCallToAction.text = nativeAd.callToAction
    nativeAdBinding.adHeadline.text = nativeAd.headline
    nativeAdBinding.adIcon.setImageDrawable(nativeAd.icon?.drawable)
    nativeAdBinding.adPrice.text = nativeAd.price
    nativeAd.starRating?.toFloat().also { value ->
      if (value != null) {
        nativeAdBinding.adStars.rating = value
      }
    }
    nativeAdBinding.adStore.text = nativeAd.store

    // Hide views for assets that don't have data.
    nativeAdBinding.adAdvertiser.visibility =
      if (nativeAd.advertiser == null) View.INVISIBLE else View.VISIBLE
    nativeAdBinding.adIcon.visibility = if (nativeAd.icon == null) View.INVISIBLE else View.VISIBLE
    nativeAdBinding.adPrice.visibility =
      if (nativeAd.price == null) View.INVISIBLE else View.VISIBLE
    nativeAdBinding.adStars.visibility =
      if (nativeAd.starRating == null) View.INVISIBLE else View.VISIBLE
    nativeAdBinding.adStore.visibility =
      if (nativeAd.store == null) View.INVISIBLE else View.VISIBLE

    // Inform the Google Mobile Ads SDK that you have finished populating the native ad views
    // with this native ad.
    nativeAdView.registerNativeAd(nativeAd, nativeAdBinding.adMedia)
  }

  companion object {
    // Replace this test ad unit ID with your own ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
  }
}
