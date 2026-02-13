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
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo

/** A [AdFragment] subclass that preloads an app open ad. */
class AppOpenPreloadFragment : AdFragment<FragmentPreloadBinding>() {

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPreloadBinding
    get() = FragmentPreloadBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Start preloading.
    startPreloadingWithCallback()

    // Initialize the UI.
    binding.txtTitle.text = getText(R.string.app_open)
    binding.btnShow.setOnClickListener { sender ->
      pollAndShowAd()
      updateUI()
    }
    updateUI()
  }

  private fun startPreloadingWithCallback() {
    val preloadCallback =
      // [Important] Do not call preload start of poll ad within the callback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.i(TAG, ("App Open preload ad failed to load with error: " + adError.message))
          // [Optional] Get the error response info for additional details.
          // val responseInfo = adError.responseInfo
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "App open preload ad is not available")
          updateUI()
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "App open preload ad is available")
          updateUI()
        }
      }
    val adRequest: AdRequest = AdRequest.Builder(AD_UNIT_ID).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    AppOpenAdPreloader.start(AD_UNIT_ID, preloadConfig, preloadCallback)
  }

  private fun pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = AppOpenAdPreloader.pollAd(AD_UNIT_ID)

    // Interact with the ad object as needed.
    ad?.apply {
      Log.d(TAG, "App Open ad response info: ${this.getResponseInfo()}")
      this.adEventCallback =
        object : AppOpenAdEventCallback {
          override fun onAdImpression() {
            Log.d(TAG, "App Open ad recorded an impression.")
          }

          override fun onAdPaid(value: AdValue) {
            Log.d(TAG, "App Open ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}")
          }
        }

      // Show the ad.
      ad.show(requireActivity())
    }
  }

  private fun isAdAvailable(): Boolean {
    return AppOpenAdPreloader.isAdAvailable(AD_UNIT_ID)
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

  companion object {
    // Replace this test ad unit ID with your own ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
  }
}
