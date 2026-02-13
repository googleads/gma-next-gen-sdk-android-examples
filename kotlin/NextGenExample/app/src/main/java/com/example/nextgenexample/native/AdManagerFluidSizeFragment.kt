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

package com.example.nextgenexample.native

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentFluidSizeBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.util.LinkedList
import java.util.Queue

/** A [Fragment] subclass that loads a FLUID size ad. */
class AdManagerFluidSizeFragment : AdFragment<FragmentFluidSizeBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFluidSizeBinding
    get() = FragmentFluidSizeBinding::inflate

  private lateinit var fluidAdContainer: FrameLayout

  private val adViewWidths: Queue<Int> = LinkedList(listOf(200, 250, 320, 360))

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    fluidAdContainer = binding.adViewContainer

    binding.fluidWidthChangeBtn.setOnClickListener { _ ->
      // Cycle to the next width in the queue.
      val newWidth: Int? = adViewWidths.poll()
      adViewWidths.add(newWidth)

      // Change the ad view container's width.
      val layoutParams = binding.adViewContainer.layoutParams
      val scale = resources.displayMetrics.density
      if (newWidth != null) {
        layoutParams.width = (newWidth.times(scale) + 0.5f).toInt()
      }
      binding.adViewContainer.setLayoutParams(layoutParams)

      // Update the TextView with the new width.
      binding.fluidCurrentWidthText.text = buildString {
        append(newWidth)
        append(" dp")
      }
    }

    // [START fluid_size_ad_request]
    // Be sure to specify Fluid as the ad size in the Ad Manager UI and create
    // an ad request with FLUID size.
    val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, AdSize.FLUID).build()
    // [END fluid_size_ad_request]

    loadAd(adRequest)
  }

  private fun loadAd(adRequest: BannerAdRequest) {
    BannerAd.load(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                Log.d(Constant.TAG, "Fluid size ad recorded an impression.")
              }

              override fun onAdClicked() {
                Log.d(Constant.TAG, "Fluid size ad recorded a click.")
              }
            }
          ad.bannerAdRefreshCallback =
            object : BannerAdRefreshCallback {
              override fun onAdRefreshed() {
                showToast("Fluid size ad refreshed.")
                Log.d(Constant.TAG, "Fluid size ad refreshed.")
              }

              override fun onAdFailedToRefresh(adError: LoadAdError) {
                showToast("Fluid size ad failed to refresh.")
                Log.w(Constant.TAG, "Fluid size ad failed to refresh: $adError")
              }
            }

          activity?.runOnUiThread {
            fluidAdContainer.removeAllViews()
            fluidAdContainer.addView(ad.getView(requireActivity()))
          }

          showToast("Fluid size ad loaded.")
          Log.d(Constant.TAG, "Fluid size ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          showToast("Fluid size ad failed to load.")
          Log.w(Constant.TAG, "Fluid size ad failed to load: $adError")
        }
      },
    )
  }

  private companion object {
    // Sample ad unit ID.
    const val AD_UNIT_ID = "/21775744923/example/api-demo/fluid"
  }
}
