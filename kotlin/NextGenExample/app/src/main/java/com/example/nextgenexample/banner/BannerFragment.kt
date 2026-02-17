/*
 * Copyright 2024 Google LLC
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

package com.example.nextgenexample.banner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentBannerBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** A simple [Fragment] subclass that loads a banner ad. */
class BannerFragment : AdFragment<FragmentBannerBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBannerBinding
    get() = FragmentBannerBinding::inflate

  private lateinit var adView: AdView

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adView = binding.adView

    // Get the ad size based on the screen width.
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)

    // Load an ad.
    loadAd(adSize)
  }

  private fun loadAd(adSize: AdSize) {
    adView.loadAd(
      BannerAdRequest.Builder(AD_UNIT_ID, adSize).build(),
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                Log.d(Constant.TAG, "Banner ad recorded an impression.")
              }

              override fun onAdClicked() {
                Log.d(Constant.TAG, "Banner ad recorded a click.")
              }
            }
          ad.bannerAdRefreshCallback =
            object : BannerAdRefreshCallback {
              override fun onAdRefreshed() {
                showToast("Banner ad refreshed.")
                Log.d(Constant.TAG, "Banner ad refreshed.")
              }

              override fun onAdFailedToRefresh(adError: LoadAdError) {
                showToast("Banner ad failed to refresh.")
                Log.w(Constant.TAG, "Banner ad failed to refresh: $adError")
              }
            }

          showToast("Banner ad loaded.")
          Log.d(Constant.TAG, "Banner ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          showToast("Banner failed to load.")
          Log.w(Constant.TAG, "Banner ad failed to load: $adError")
        }
      },
    )
  }

  // Determine the screen width to use for the ad width.
  private val adWidth: Int
    get() {
      val displayMetrics = resources.displayMetrics
      val adWidthPixels = displayMetrics.widthPixels
      val density = displayMetrics.density
      return (adWidthPixels / density).toInt()
    }

  override fun onDestroyView() {
    if (this::adView.isInitialized) {
      adView.destroy()
    }
    super.onDestroyView()
  }

  companion object {
    // Sample anchored adaptive banner ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
  }
}
