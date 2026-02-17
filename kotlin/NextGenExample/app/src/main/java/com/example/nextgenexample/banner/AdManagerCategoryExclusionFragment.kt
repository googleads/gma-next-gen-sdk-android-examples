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
import androidx.fragment.app.Fragment
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant.Companion.TAG
import com.example.nextgenexample.databinding.FragmentCategoryExclusionBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** A [Fragment] subclass that loads ads configured to use category exclusion. */
class AdManagerCategoryExclusionFragment : AdFragment<FragmentCategoryExclusionBinding>() {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentCategoryExclusionBinding
    get() = FragmentCategoryExclusionBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loadAd(null, binding.noneExcludedBanner)
    loadAd(DOGS_EXCLUSION_KEY, binding.dogsExcludedBanner)
    loadAd(CATS_EXCLUSION_KEY, binding.catsExcludedBanner)
  }

  private fun loadAd(exclusionKey: String?, adView: AdView) {
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360)

    val adRequest =
      if (exclusionKey != null) {
        BannerAdRequest.Builder(AD_UNIT_ID, adSize).addCategoryExclusion(exclusionKey).build()
      } else {
        BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()
      }

    val logPrefix = buildString {
      append("Banner ad ")
      if (exclusionKey != null) {
        append("with category exclusion $exclusionKey ")
      }
    }

    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {

          // Interact with the loaded ad object as needed.
          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                Log.d(TAG, logPrefix + "recorded an impression.")
              }

              override fun onAdClicked() {
                Log.d(TAG, logPrefix + "recorded a click.")
              }
            }
          ad.bannerAdRefreshCallback =
            object : BannerAdRefreshCallback {
              override fun onAdRefreshed() {
                showToast(logPrefix + "refreshed.")
                Log.d(TAG, logPrefix + "refreshed.")
              }

              override fun onAdFailedToRefresh(adError: LoadAdError) {
                showToast(logPrefix + "failed to refresh.")
                Log.w(TAG, logPrefix + "failed to refresh: $adError")
              }
            }
          showToast(logPrefix + "loaded.")
          Log.d(TAG, logPrefix + "loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          showToast(logPrefix + "failed to load.")
          Log.w(TAG, logPrefix + "failed to load: $adError")
        }
      },
    )
  }

  override fun onDestroyView() {
    binding.noneExcludedBanner.destroy()
    binding.dogsExcludedBanner.destroy()
    binding.catsExcludedBanner.destroy()
    super.onDestroyView()
  }

  private companion object {
    // Sample ad unit ID.
    const val AD_UNIT_ID = "/21775744923/example/api-demo/category-exclusion"
    const val DOGS_EXCLUSION_KEY = "apidemo_exclude_dogs"
    const val CATS_EXCLUSION_KEY = "apidemo_exclude_cats"
  }
}
