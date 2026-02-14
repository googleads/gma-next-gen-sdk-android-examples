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

package com.example.nextgenexample.native

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentNativeBinding
import com.example.nextgenexample.databinding.NativeAdBinding
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.VideoController
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** A simple [Fragment] subclass that loads a native ad. */
class NativeFragment : AdFragment<FragmentNativeBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNativeBinding
    get() = FragmentNativeBinding::inflate

  private var lastNativeAd: NativeAd? = null
  private var customControls: CustomVideoControlsView? = null
  private var isUIEnabled = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.refreshAdButton.setOnClickListener { loadAd() }
    binding.checkRequestVideo.setOnClickListener { updateUI() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // Always call destroy() on ads on removal.
    destroyNativeAd()
  }

  private fun destroyNativeAd() {
    lastNativeAd?.destroy()
    lastNativeAd = null
    customControls = null
  }

  private fun loadAd() {
    setUIEnabled(false)

    // Build an ad request with native ad options to customize the user experience.
    val adUnitID = if (binding.checkRequestVideo.isChecked) VIDEO_AD_UNIT_ID else IMAGE_AD_UNIT_ID
    val videoOptions: VideoOptions =
      VideoOptions.Builder().setStartMuted(binding.checkStartMuted.isChecked).build()
    val adRequest =
      NativeAdRequest.Builder(adUnitID, listOf(NativeAd.NativeAdType.NATIVE))
        .setVideoOptions(videoOptions)
        .build()

    // Define the callback to handle successful ad loading or failed ad loading.
    val adCallback =
      object : NativeAdLoaderCallback {
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
          Log.d(Constant.TAG, "Native ad loaded.")
          CoroutineScope(Dispatchers.Main).launch {
            // Remove all old ad views when loading a new native ad.
            binding.nativeViewContainer.removeAllViews()
            // Always call destroy() on ads on removal.
            destroyNativeAd()
            setEventCallback(nativeAd)
            displayNativeAd(nativeAd)
            lastNativeAd = nativeAd
            setUIEnabled(true)
          }
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.e(Constant.TAG, "Native ad failed to load: $adError")
          showToast("Ad failed to load.")
          setUIEnabled(true)
        }
      }
    // Load the native ad with our request and callback.
    NativeAdLoader.load(adRequest, adCallback)
  }

  private fun setEventCallback(nativeAd: NativeAd) {
    nativeAd.adEventCallback =
      object : NativeAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "Native ad showed full screen content.")
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "Native ad dismissed full screen content.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          Log.d(
            Constant.TAG,
            "Native ad failed to show full screen content with error: $fullScreenContentError",
          )
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Native ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Native ad recorded a click.")
        }
      }
  }

  private fun displayNativeAd(nativeAd: NativeAd) {
    // Inflate the native ad view and add it to the view hierarchy.
    val nativeAdBinding = NativeAdBinding.inflate(layoutInflater)
    binding.nativeViewContainer.addView(nativeAdBinding.root)

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

    val videoController = nativeAd.mediaContent.videoController
    if (videoController != null && nativeAd.mediaContent.hasVideoContent) {
      // If the main asset is a video, set the videoLifecycleCallbacks.
      binding.textVideoStatus.text = getString(R.string.nativead_video_play)
      videoController.videoLifecycleCallbacks = getVideoCallbacks()
    } else {
      // If the main asset is a image, enable the UI.
      binding.textVideoStatus.text = getString(R.string.nativead_video_none)
    }
  }

  private fun getVideoCallbacks(): VideoController.VideoLifecycleCallbacks {
    return object : VideoController.VideoLifecycleCallbacks {
      override fun onVideoStart() {
        binding.textVideoStatus.text = getString(R.string.nativead_video_started)
        customControls?.onVideoStart()
        super.onVideoStart()
      }

      override fun onVideoEnd() {
        // Publishers should allow native ads to complete video playback before
        // refreshing or replacing them with another ad in the same UI location.
        binding.textVideoStatus.text = getString(R.string.nativead_video_ended)
        customControls?.onVideoEnd()
        super.onVideoEnd()
      }

      override fun onVideoPause() {
        binding.textVideoStatus.text = getString(R.string.nativead_video_pause)
        customControls?.onVideoPause()
        super.onVideoPause()
      }

      override fun onVideoPlay() {
        binding.textVideoStatus.text = getString(R.string.nativead_video_play)
        customControls?.onVideoPlay()
        super.onVideoPlay()
      }

      override fun onVideoMute(muted: Boolean) {
        customControls?.onVideoMute(muted)
        super.onVideoPlay()
      }
    }
  }

  private fun setUIEnabled(enableUI: Boolean) {
    isUIEnabled = enableUI
    updateUI()
  }

  private fun updateUI() {
    activity?.runOnUiThread {
      binding.refreshAdButton.isEnabled = isUIEnabled
      binding.checkRequestVideo.isEnabled = isUIEnabled
      binding.checkStartMuted.isEnabled = isUIEnabled && binding.checkRequestVideo.isChecked
    }
  }

  private companion object {
    // Sample native image ad unit ID.
    const val IMAGE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    // Sample native video ad unit ID.
    const val VIDEO_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"
  }
}
