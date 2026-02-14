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

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.example.nextgenexample.databinding.CustomNativeAdBinding
import com.google.android.gms.ads.nativead.NativeAdAssetNames
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.NativeAdType
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.OnCustomClickListener

/** Kotlin code snippets for the developer guide. */
class CustomNativeSnippets {

  private lateinit var customNativeAdBinding: CustomNativeAdBinding

  private fun loadCustomNativeAd() {
    // [START load_ad]
    val adRequest =
      NativeAdRequest.Builder(AD_UNIT_ID, listOf(NativeAdType.CUSTOM_NATIVE))
        .setCustomFormatIds(listOf(CUSTOM_NATIVE_FORMAT_ID))
        .build()

    // Load the native ad with the ad request and callback.
    NativeAdLoader.load(
      adRequest,
      object : NativeAdLoaderCallback {
        override fun onCustomNativeAdLoaded(customNativeAd: CustomNativeAd) {
          // TODO: Store the custom native ad.
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {}
      },
    )
    // [END load_ad]
  }

  // [START display_ad]
  private fun displayCustomNativeAd(customNativeAd: CustomNativeAd, context: Context) {
    // Render the text elements.

    // The `customNativeAdBinding` is the layout binding for the ad container that
    // contains all `CustomNativeAd` assets.
    customNativeAdBinding.headline.text = customNativeAd.getText("Headline")
    customNativeAdBinding.caption.text = customNativeAd.getText("Caption")

    // If the main asset is an image, render it with an ImageView.
    val imageView = ImageView(context)
    imageView.adjustViewBounds = true
    imageView.setImageDrawable(customNativeAd.getImage("MainImage")?.drawable)
    // [START perform_click]
    imageView.setOnClickListener { customNativeAd.performClick("MainImage") }
    // [END perform_click]
    customNativeAdBinding.mediaPlaceholder.addView(imageView)

    // Render the ad choices icon.
    renderAdChoices(customNativeAd)

    // [START record_impression]
    // Record an impression.
    customNativeAd.recordImpression()
    // [END record_impression]
  }

  // [END display_ad]

  // [START display_video_ad]
  private fun displayVideoCustomNativeAd(customNativeAd: CustomNativeAd, context: Context) {
    // Check whether the custom native ad has video content.
    val mediaContent = customNativeAd.mediaContent
    if (mediaContent != null && mediaContent.hasVideoContent) {
      // Render the media content in a MediaView.
      val mediaView = MediaView(context)
      mediaView.mediaContent = mediaContent
      customNativeAdBinding.mediaPlaceholder.addView(mediaView)
    } else {
      // Fall back to other assets defined on your custom native ad.
      val imageView = ImageView(context)
      imageView.adjustViewBounds = true
      imageView.setImageDrawable(customNativeAd.getImage("MainImage")?.drawable)
      customNativeAdBinding.mediaPlaceholder.addView(imageView)
    }

    // Record an impression.
    customNativeAd.recordImpression()
  }

  // [END display_video_ad]

  // [START render_ad_choices]
  private fun renderAdChoices(customNativeAd: CustomNativeAd) {
    // Render the AdChoices image.
    val adChoiceAsset = customNativeAd.getImage(NativeAdAssetNames.ASSET_ADCHOICES_CONTAINER_VIEW)
    if (adChoiceAsset != null) {
      customNativeAdBinding.adchoices.setImageDrawable(adChoiceAsset.drawable)
      customNativeAdBinding.adchoices.visibility = View.VISIBLE
      customNativeAdBinding.adchoices.setOnClickListener {
        // Handle click. See the next section for more details.
        customNativeAd.performClick(NativeAdAssetNames.ASSET_ADCHOICES_CONTAINER_VIEW)
      }
    } else {
      customNativeAdBinding.adchoices.visibility = View.GONE
    }
  }

  // [END render_ad_choices]

  private fun setOnCustomClickListener(customNativeAd: CustomNativeAd) {
    // [START set_custom_click_listener]
    customNativeAd.onCustomClickListener =
      object : OnCustomClickListener {
        override fun onCustomClick(assetName: String) {
          // Perform your custom action.
        }
      }
    // [END set_custom_click_listener]
  }

  private companion object {
    // Sample custom native ad unit ID for video ads.
    const val AD_UNIT_ID = "/21775744923/example/native"
    // Sample custom native format IDs.
    const val CUSTOM_NATIVE_FORMAT_ID = "12387226"
  }
}
