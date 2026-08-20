/*
 * Copyright 2026 Google LLC
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

package com.example.nextgenexample.pictureinpicture

import android.app.Activity
import android.util.Log
import com.example.nextgenexample.Constant
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAd
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdOptions
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdRequest

/**
 * Singleton object that loads, manages lifecycle, and handles events for Picture-in-Picture (PiP)
 * ads.
 */
@OptIn(ExperimentalApi::class)
object PictureInPictureAdManager {
  private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

  var pipAd: PictureInPictureAd? = null
    private set

  var onAdShownListener: (() -> Unit)? = null
  var onAdHiddenListener: (() -> Unit)? = null

  /**
   * Loads a Picture-in-Picture ad.
   *
   * @param onAdLoaded Optional callback invoked when the ad loads.
   * @param onAdFailedToLoad Optional callback invoked when the ad fails to load.
   */
  fun loadAd(
    onAdLoaded: ((PictureInPictureAd) -> Unit)? = null,
    onAdFailedToLoad: ((LoadAdError) -> Unit)? = null,
  ) {
    val request = PictureInPictureAdRequest.Builder(AD_UNIT_ID).build()

    PictureInPictureAd.load(
      request,
      object : AdLoadCallback<PictureInPictureAd> {
        override fun onAdLoaded(ad: PictureInPictureAd) {
          Log.d(Constant.TAG, "Picture-in-Picture ad loaded.")
          pipAd?.destroy()
          pipAd = ad
          setAdEventCallback(ad)
          onAdLoaded?.invoke(ad)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.w(Constant.TAG, "Picture-in-Picture ad failed to load: $adError")
          onAdFailedToLoad?.invoke(adError)
        }
      },
    )
  }

  /** Shows the Picture-in-Picture ad with the provided options. */
  fun showAd(activity: Activity, options: PictureInPictureAdOptions) {
    val ad = pipAd
    if (ad == null) {
      Log.d(Constant.TAG, "No Picture-in-Picture ad available to show.")
      return
    }
    ad.show(activity, options)
  }

  /** Hides the currently showing Picture-in-Picture ad. */
  fun hideAd() {
    pipAd?.hide()
  }

  /** Destroys the Picture-in-Picture ad and cleans up resources. */
  fun destroyAd() {
    pipAd?.destroy()
    pipAd = null
  }

  /** Checks if an ad exists and is available to show. */
  fun isAdAvailable(): Boolean = pipAd != null

  private fun setAdEventCallback(ad: PictureInPictureAd) {
    ad.adEventCallback =
      object : PictureInPictureAdEventCallback {
        override fun onAdShown() {
          Log.d(Constant.TAG, "Picture-in-Picture ad shown.")
          onAdShownListener?.invoke()
        }

        override fun onAdHidden() {
          Log.d(Constant.TAG, "Picture-in-Picture ad hidden.")
          onAdHiddenListener?.invoke()
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Picture-in-Picture ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Picture-in-Picture ad recorded a click.")
        }

        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "Picture-in-Picture ad showed full screen content.")
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "Picture-in-Picture ad dismissed full screen content.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          Log.w(
            Constant.TAG,
            "Picture-in-Picture ad failed to show full screen content: $fullScreenContentError",
          )
        }

        override fun onAdPaid(value: AdValue) {
          Log.d(
            Constant.TAG,
            "Picture-in-Picture ad paid: ${value.valueMicros} ${value.currencyCode}",
          )
        }
      }
  }
}
