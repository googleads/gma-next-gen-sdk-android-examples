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

package com.example.nextgenexample.banner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentMultipleAdSizesBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** A [Fragment] subclass that loads an ad configured to have multiple ad sizes. */
class AdManagerMultipleAdSizesFragment : AdFragment<FragmentMultipleAdSizesBinding>() {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentMultipleAdSizesBinding
    get() = FragmentMultipleAdSizesBinding::inflate

  private lateinit var adView: AdView

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adView = binding.adView

    binding.loadAdButton.setOnClickListener {
      if (
        !binding.adsizesCb120x20.isChecked &&
          !binding.adsizesCb320x50.isChecked &&
          !binding.adsizesCb300x250.isChecked
      ) {
        Toast.makeText(this.activity, "At least one size is required.", Toast.LENGTH_SHORT).show()
      } else {
        val sizeList = buildList {
          if (binding.adsizesCb120x20.isChecked) {
            add(AdSize(120, 20))
          }
          if (binding.adsizesCb320x50.isChecked) {
            add(AdSize.BANNER)
          }
          if (binding.adsizesCb300x250.isChecked) {
            add(AdSize.MEDIUM_RECTANGLE)
          }
        }
        val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, sizeList).build()

        loadAd(adRequest)
      }
    }
  }

  private fun loadAd(adRequest: BannerAdRequest) {
    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {

          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                Log.d(Constant.Companion.TAG, "Banner ad recorded an impression.")
              }

              override fun onAdClicked() {
                Log.d(Constant.Companion.TAG, "Banner ad recorded a click.")
              }
            }
          ad.bannerAdRefreshCallback =
            object : BannerAdRefreshCallback {
              override fun onAdRefreshed() {
                showToast("Banner ad refreshed.")
                Log.d(Constant.Companion.TAG, "Banner ad refreshed.")
              }

              override fun onAdFailedToRefresh(adError: LoadAdError) {
                showToast("Banner ad failed to refresh.")
                Log.w(Constant.Companion.TAG, "Banner ad failed to refresh: $adError")
              }
            }

          showToast("Banner ad loaded.")
          Log.d(Constant.Companion.TAG, "Banner ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {

          showToast("Banner failed to load.")
          Log.w(Constant.Companion.TAG, "Banner ad failed to load: $adError")
        }
      },
    )
  }

  override fun onDestroyView() {
    if (this::adView.isInitialized) {
      adView.destroy()
    }
    super.onDestroyView()
  }

  private companion object {
    // Sample ad unit ID.
    const val AD_UNIT_ID = "/21775744923/example/api-demo/ad-sizes"
  }
}
