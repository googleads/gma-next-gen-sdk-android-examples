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

package com.example.nextgenexample.icon

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentIconAdBinding
import com.example.nextgenexample.databinding.IconAdBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAd
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdPlacement
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdRequest

class IconAdFragment : AdFragment<FragmentIconAdBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentIconAdBinding
    get() = FragmentIconAdBinding::inflate

  private var iconAd: IconAd? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    loadIconAd()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // Always call destroy() on ads on removal.
    iconAd?.destroy()
    iconAd = null
  }

  private fun loadIconAd() {
    val request =
      IconAdRequest.Builder(AD_UNIT_ID)
        // The "AdChoices" badge is rendered at the top right corner of the icon ad
        // if left unspecified.
        .setAdChoicesPlacement(AdChoicesPlacement.BOTTOM_RIGHT)
        // Indicate the icon ad placement is in a browser
        .setIconAdPlacement(IconAdPlacement.BROWSER)
        .build()

    IconAd.load(
      request,
      object : IconAdLoadCallback {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.w(Constant.TAG, "Icon ad failed to load: $adError")
          showToast("Icon ad failed to load.")
        }

        override fun onAdLoaded(ad: IconAd) {
          Log.d(Constant.TAG, "Icon ad loaded")
          // Always call destroy() on ads on removal.
          iconAd?.destroy()
          iconAd = ad
          setAdEventCallback(ad)
          displayIconAd(ad)
        }
      },
    )
  }

  private fun setAdEventCallback(iconAd: IconAd) {
    iconAd.adEventCallback =
      object : IconAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          // Icon ad showed full screen content.
        }

        override fun onAdDismissedFullScreenContent() {
          // Icon ad dismissed full screen content.
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Icon ad failed to show full screen content.
        }

        override fun onAdImpression() {
          // Icon ad recorded an impression.
        }

        override fun onAdClicked() {
          // Icon ad recorded a click.
        }

        override fun onAdPaid(value: AdValue) {
          // Icon ad estimated to have earned money.
        }
      }
  }

  private fun displayIconAd(iconAd: IconAd) {
    activity?.runOnUiThread {
      val iconAdViewBinding = IconAdBinding.inflate(layoutInflater)
      // Add the ad view to the active view hierarchy.
      binding.iconAdContainer.addView(iconAdViewBinding.root)
      val iconAdView = iconAdViewBinding.root

      // Populate the view elements with their respective icon ad asset.
      iconAdView.callToActionView = iconAdViewBinding.adCallToAction
      iconAdView.headlineView = iconAdViewBinding.adHeadline
      iconAdView.iconView = iconAdViewBinding.adIcon
      iconAdView.starRatingView = iconAdViewBinding.adStars

      // Map each asset view property to the corresponding view in your view hierarchy.
      iconAdViewBinding.adCallToAction.text = iconAd.callToAction
      iconAdViewBinding.adHeadline.text = iconAd.headline
      iconAdViewBinding.adIcon.setImageDrawable(iconAd.icon.drawable)
      iconAd.starRating?.toFloat().also { value ->
        if (value != null) {
          iconAdViewBinding.adStars.rating = value
        }
      }

      // Register the icon ad with the view presenting it.
      iconAdView.registerIconAd(iconAd)
    }
  }

  private companion object {
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1476272466"
  }
}
