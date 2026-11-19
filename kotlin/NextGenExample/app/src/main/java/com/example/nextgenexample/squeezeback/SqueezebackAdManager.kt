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

package com.example.nextgenexample.squeezeback

import android.app.Activity
import android.util.Log
import com.example.nextgenexample.Constant
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAd
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdRequest

/** Singleton object that loads, manages lifecycle, and handles events for squeezeback ads. */
@OptIn(ExperimentalApi::class)
object SqueezebackAdManager {
  private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9983381271"

  var squeezebackAd: SqueezebackAd? = null
    private set

  var isAdShowing: Boolean = false
    private set

  var onAdShownListener: (() -> Unit)? = null
  var onAdHiddenListener: (() -> Unit)? = null

  /**
   * Loads a squeezeback ad.
   *
   * @param onAdLoaded Optional callback invoked when the ad loads.
   * @param onAdFailedToLoad Optional callback invoked when the ad fails to load.
   */
  fun loadAd(
    onAdLoaded: ((SqueezebackAd) -> Unit)? = null,
    onAdFailedToLoad: ((LoadAdError) -> Unit)? = null,
  ) {
    if (isAdShowing) {
      Log.w(Constant.TAG, "Cannot load a new squeezeback ad while one is currently showing.")
      return
    }

    val request = SqueezebackAdRequest.Builder(AD_UNIT_ID).build()

    SqueezebackAd.load(
      request,
      object : AdLoadCallback<SqueezebackAd> {
        override fun onAdLoaded(ad: SqueezebackAd) {
          Log.d(Constant.TAG, "Squeezeback ad loaded.")
          destroyAd()
          squeezebackAd = ad
          setAdEventCallback(ad)
          onAdLoaded?.invoke(ad)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.w(Constant.TAG, "Squeezeback ad failed to load: $adError")
          onAdFailedToLoad?.invoke(adError)
        }
      },
    )
  }

  /** Shows the squeezeback ad. */
  fun showAd(activity: Activity) {
    val ad = squeezebackAd
    if (ad == null) {
      Log.d(Constant.TAG, "No squeezeback ad available to show.")
      return
    }
    ad.show(activity)
  }

  /** Hides the currently showing squeezeback ad. */
  fun hideAd() {
    squeezebackAd?.hide()
  }

  /** Destroys the squeezeback ad and cleans up resources. */
  fun destroyAd() {
    isAdShowing = false
    val ad = squeezebackAd ?: return
    squeezebackAd = null
    ad.adEventCallback = null
    ad.hide()
    ad.destroy()
  }

  /** Checks if an ad exists and is available to show. */
  fun isAdAvailable(): Boolean = squeezebackAd != null && !isAdShowing

  private fun setAdEventCallback(ad: SqueezebackAd) {
    ad.adEventCallback =
      object : SqueezebackAdEventCallback {
        override fun onAdShown() {
          Log.d(Constant.TAG, "Squeezeback ad shown.")
          isAdShowing = true
          onAdShownListener?.invoke()
        }

        override fun onAdHidden() {
          Log.d(Constant.TAG, "Squeezeback ad hidden.")
          isAdShowing = false
          if (squeezebackAd === ad) {
            squeezebackAd = null
          }
          ad.adEventCallback = null
          ad.destroy()
          onAdHiddenListener?.invoke()
        }

        override fun onAdFailedToShow(error: FullScreenContentError) {
          Log.w(Constant.TAG, "Squeezeback ad failed to show: $error")
          isAdShowing = false
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Squeezeback ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Squeezeback ad recorded a click.")
        }

        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "Squeezeback ad showed full screen content.")
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "Squeezeback ad dismissed full screen content.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          Log.w(
            Constant.TAG,
            "Squeezeback ad failed to show full screen content: $fullScreenContentError",
          )
        }

        override fun onAdPaid(value: AdValue) {
          Log.d(Constant.TAG, "Squeezeback ad paid: ${value.valueMicros} ${value.currencyCode}")
        }
      }
  }
}
