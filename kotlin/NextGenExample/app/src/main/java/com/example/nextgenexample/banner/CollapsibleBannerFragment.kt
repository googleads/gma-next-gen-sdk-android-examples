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
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentBannerBinding
import com.google.android.gms.ads.mediation.admob.AdMobAdapter
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** A [Fragment] subclass that loads a collapsible banner ad. */
class CollapsibleBannerFragment : AdFragment<FragmentBannerBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBannerBinding
    get() = FragmentBannerBinding::inflate

  private lateinit var adView: AdView

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adView = binding.adView

    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360)

    loadCollapsibleBanner(adSize)
  }

  private fun loadCollapsibleBanner(adSize: AdSize) {
    // [START build_collapsible_banner_ad_request]
    // Create an extra parameter that aligns the bottom of the expanded ad to
    // the bottom of the bannerView.
    val extras = Bundle()
    extras.putString("collapsible", "bottom")

    // Create an ad request.
    val adRequest =
      BannerAdRequest.Builder(AD_UNIT_ID, adSize)
        .putAdSourceExtrasBundle(AdMobAdapter::class.java, extras)
        .build()
    // [END build_collapsible_banner_ad_request]

    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          ad.adEventCallback =
            object : BannerAdEventCallback {
              override fun onAdImpression() {
                Log.d(Constant.TAG, "Collapsible banner ad recorded an impression.")
              }

              override fun onAdClicked() {
                Log.d(Constant.TAG, "Collapsible banner ad recorded a click.")
              }
            }
          ad.bannerAdRefreshCallback =
            object : BannerAdRefreshCallback {
              override fun onAdRefreshed() {
                showToast("Collapsible banner ad refreshed.")
                Log.d(Constant.TAG, "Collapsible banner ad refreshed.")
              }

              override fun onAdFailedToRefresh(adError: LoadAdError) {
                showToast("Collapsible banner ad failed to refresh.")
                Log.w(Constant.TAG, "Collapsible banner ad failed to refresh: $adError")
              }
            }

          showToast("Collapsible banner ad loaded.")
          Log.d(Constant.TAG, "Collapsible banner ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          showToast("Banner failed to load.")
          Log.w(Constant.TAG, "Collapsible banner ad failed to load: $adError")
        }
      },
    )
  }

  override fun onDestroyView() {
    // Destroy the ad view when no longer needed.
    if (this::adView.isInitialized) {
      adView.destroy()
    }
    super.onDestroyView()
  }

  private companion object {
    // Sample collapsible banner ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
  }
}
