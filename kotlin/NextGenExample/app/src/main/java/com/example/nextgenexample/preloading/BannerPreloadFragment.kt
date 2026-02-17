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
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo

/** A [AdFragment] subclass that preloads a banner ad. */
class BannerPreloadFragment : AdFragment<FragmentPreloadBinding>() {

  private var currentAd: BannerAd? = null

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPreloadBinding
    get() = FragmentPreloadBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Start preloading.
    startPreloadingWithCallback()

    // Initialize the UI.
    binding.txtTitle.text = getText(R.string.banner)
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
    binding?.placeholder?.removeAllViews()
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
          Log.i(TAG, ("Banner preload ad failed to load with error: " + adError.message))
          // [Optional] Get the error response info for additional details.
          // val responseInfo = adError.responseInfo
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "Banner preload ad is not available")
          updateUI()
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "Banner preload ad is available")
          // Log the response info to for more information.
          Log.i(TAG, "Response Info: ${responseInfo}")
          updateUI()
        }
      }
    // Get the ad size based on the screen width.
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()
    val preload = PreloadConfiguration(adRequest)
    BannerAdPreloader.start(AD_UNIT_ID, preload, preloadCallback)
  }

  private fun pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = BannerAdPreloader.pollAd(AD_UNIT_ID)

    if (ad != null) {
      // Destroy the previous banner.
      destroyCurrentAd()

      // Interact with the ad object as needed.
      Log.d(TAG, "Banner ad response info: ${ad.getResponseInfo()}")
      ad.adEventCallback =
        object : BannerAdEventCallback {
          override fun onAdImpression() {
            Log.d(TAG, "Banner ad recorded an impression.")
          }

          override fun onAdPaid(value: AdValue) {
            Log.d(TAG, "Banner ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}")
          }
        }

      // Show the ad.
      binding.placeholder.addView(ad.getView(requireActivity()))
      currentAd = ad
    }
  }

  private fun isAdAvailable(): Boolean {
    return BannerAdPreloader.isAdAvailable(AD_UNIT_ID)
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

  // Determine the screen width to use for the ad width.
  private val adWidth: Int
    get() {
      val displayMetrics = resources.displayMetrics
      val adWidthPixels = displayMetrics.widthPixels
      val density = displayMetrics.density
      return (adWidthPixels / density).toInt()
    }

  companion object {
    // Replace this test ad unit ID with your own ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
  }
}
