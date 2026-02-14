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

package com.example.nextgenexample.appopen

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.nextgenexample.Constant
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interface definition for a callback to be invoked when an app open ad is complete (i.e. dismissed
 * or fails to show).
 */
fun interface OnShowAdCompleteListener {
  fun onShowAdComplete()
}

/** Singleton object that loads and shows app open ads. */
object AppOpenAdManager {
  private var appOpenAd: AppOpenAd? = null
  private var isLoadingAd = false
  var isShowingAd = false

  /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
  private var loadTime: Long = 0

  // [START load_ad]

  /**
   * Load an ad.
   *
   * @param context a context used to perform UI-related operations (e.g. display Toast messages).
   *   Loading the app open ad itself does not require a context.
   */
  fun loadAd(context: Context) {
    // Do not load ad if there is an unused ad or one is already loading.
    if (isLoadingAd || isAdAvailable()) {
      Log.d(Constant.TAG, "App open ad is either loading or has already loaded.")
      return
    }

    isLoadingAd = true
    AppOpenAd.load(
      AdRequest.Builder(AppOpenFragment.AD_UNIT_ID).build(),
      object : AdLoadCallback<AppOpenAd> {
        /**
         * Called when an app open ad has loaded.
         *
         * @param ad the loaded app open ad.
         */
        override fun onAdLoaded(ad: AppOpenAd) {
          // Called when an ad has loaded.
          appOpenAd = ad
          isLoadingAd = false
          loadTime = Date().time
          Log.d(Constant.TAG, "App open ad loaded.")
          // [START_EXCLUDE silent]
          CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "App open ad loaded.", Toast.LENGTH_SHORT).show()
          }
          // [END_EXCLUDE]
        }

        /**
         * Called when an app open ad has failed to load.
         *
         * @param loadAdError the error.
         */
        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          isLoadingAd = false
          Log.w(Constant.TAG, "App open ad failed to load: $loadAdError")
          // [START_EXCLUDE silent]
          CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "App open ad failed to load.", Toast.LENGTH_SHORT).show()
          }
          // [END_EXCLUDE]
        }
      },
    )
  }

  // [END load_ad]

  /** Check if ad was loaded more than n hours ago. */
  private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
    val dateDifference: Long = Date().time - loadTime
    val numMilliSecondsPerHour: Long = 3600000
    return dateDifference < numMilliSecondsPerHour * numHours
  }

  /** Check if ad exists and can be shown. */
  private fun isAdAvailable(): Boolean {
    // App open ads expire after four hours. Ads rendered more than four hours after request time
    // are no longer valid and may not earn revenue.
    return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
  }

  // [START show_ad]
  /**
   * Show the ad if one isn't already showing.
   *
   * @param activity the activity that shows the app open ad.
   * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete.
   */
  fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener?) {
    // If the app open ad is already showing, do not show the ad again.
    if (isShowingAd) {
      Log.d(Constant.TAG, "App open ad is already showing.")
      onShowAdCompleteListener?.onShowAdComplete()
      return
    }

    // If the app open ad is not available yet, invoke the callback.
    if (!isAdAvailable()) {
      Log.d(Constant.TAG, "App open ad is not ready yet.")
      onShowAdCompleteListener?.onShowAdComplete()
      return
    }

    appOpenAd?.adEventCallback =
      object : AppOpenAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "App open ad showed.")
          // [START_EXCLUDE silent]
          CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(activity, "App open ad shown.", Toast.LENGTH_SHORT).show()
          }
          // [END_EXCLUDE]
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "App open ad dismissed.")
          appOpenAd = null
          isShowingAd = false
          // [START_EXCLUDE silent]
          CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(activity, "App open ad dismissed.", Toast.LENGTH_SHORT).show()
          }
          // [END_EXCLUDE]
          onShowAdCompleteListener?.onShowAdComplete()
          loadAd(activity)
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          appOpenAd = null
          isShowingAd = false
          // [START_EXCLUDE silent]
          CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(activity, "App open ad failed to show.", Toast.LENGTH_SHORT).show()
          }
          // [END_EXCLUDE]
          Log.w(Constant.TAG, "App open ad failed to show: $fullScreenContentError")
          onShowAdCompleteListener?.onShowAdComplete()
          loadAd(activity)
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "App open ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "App open ad recorded a click.")
        }
      }

    isShowingAd = true
    appOpenAd?.show(activity)
  }
  // [END show_ad]
}
