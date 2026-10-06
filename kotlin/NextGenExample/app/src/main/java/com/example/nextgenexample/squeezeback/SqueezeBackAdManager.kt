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
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAd
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdOptions
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdRequest

/** Singleton object that loads, manages lifecycle, and handles events for squeezeback ads. */
@OptIn(ExperimentalApi::class)
object SqueezeBackAdManager {
  private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

  var squeezebackAd: SqueezeBackAd? = null
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
    onAdLoaded: ((SqueezeBackAd) -> Unit)? = null,
    onAdFailedToLoad: ((LoadAdError) -> Unit)? = null,
  ) {
    val request = SqueezeBackAdRequest.Builder(AD_UNIT_ID).build()

    SqueezeBackAd.load(
      request,
      object : AdLoadCallback<SqueezeBackAd> {
        override fun onAdLoaded(ad: SqueezeBackAd) {
          Log.d(Constant.TAG, "Squeezeback ad loaded.")
          squeezebackAd?.destroy()
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
    val options = SqueezeBackAdOptions.Builder().build()
    ad.show(activity, options)
  }

  /** Hides the currently showing squeezeback ad. */
  fun hideAd() {
    squeezebackAd?.hide()
  }

  /** Destroys the squeezeback ad and cleans up resources. */
  fun destroyAd() {
    squeezebackAd?.destroy()
    squeezebackAd = null
  }

  /** Checks if an ad exists and is available to show. */
  fun isAdAvailable(): Boolean = squeezebackAd != null

  private fun setAdEventCallback(ad: SqueezeBackAd) {
    ad.adEventCallback =
      object : SqueezeBackAdEventCallback {
        override fun onAdShown() {
          Log.d(Constant.TAG, "Squeezeback ad shown.")
          onAdShownListener?.invoke()
        }

        override fun onAdHidden() {
          Log.d(Constant.TAG, "Squeezeback ad hidden.")
          onAdHiddenListener?.invoke()
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
