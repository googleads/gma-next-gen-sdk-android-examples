package com.example.snippets

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo

/** Kotlin code snippets for the developer guide. */
private class AppOpenAdSnippets {

  private var appOpenAd: AppOpenAd? = null

  private fun startPreloading(adUnitId: String) {
    // [START start_preload]
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    AppOpenAdPreloader.start(adUnitId, preloadConfig)
    // [END start_preload]
  }

  private fun setBufferSize(adUnitId: String) {
    // [START set_buffer_size]
    val adRequest = AdRequest.Builder(adUnitId).build()
    // Define a PreloadConfiguration and set the buffer size to 2 preloaded ads.
    val preloadConfig = PreloadConfiguration(adRequest, bufferSize = 2)
    AppOpenAdPreloader.start(adUnitId, preloadConfig)
    // [END set_buffer_size]
  }

  private fun startPreloadingWithCallback(adUnitId: String) {
    // [START start_preload_with_callback]
    val preloadCallback =
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.d(TAG, "App open preload ad $preloadId failed to load with error: ${adError.message}")
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(TAG, "App open preload ad $preloadId is not available")
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(TAG, "App open preload ad $preloadId is available")
        }
      }
    val adRequest = AdRequest.Builder(adUnitId).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    AppOpenAdPreloader.start(adUnitId, preloadConfig, preloadCallback)
    // [END start_preload_with_callback]
  }

  // [START pollAndShowAd]
  private fun pollAndShowAd(activity: Activity, adUnitId: String) {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = AppOpenAdPreloader.pollAd(adUnitId)
    if (ad == null) {
      Log.e(TAG, "App open ad is not available.")
      return
    }

    // Interact with the ad object as needed.
    Log.d(TAG, "App open ad response info: ${ad.getResponseInfo()}")
    ad.adEventCallback =
      object : AppOpenAdEventCallback {
        override fun onAdImpression() {
          Log.d(TAG, "App open ad recorded an impression.")
        }
      }
    ad.show(activity)
  }

  // [END pollAndShowAd]

  private fun peekAdResponseInfo(preloadId: String) {
    // [START peek_ad]
    val responseInfo = AppOpenAdPreloader.peekAdResponseInfo(preloadId)
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.")
      return
    }

    Log.d(TAG, "Peeked ad response ID: ${responseInfo.responseId}")
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private fun isAdAvailable(adUnitId: String): Boolean {
    return AppOpenAdPreloader.isAdAvailable(adUnitId)
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private fun stopPreloading(adUnitId: String) {
    // Stops the preloading and destroy preloaded ads.
    AppOpenAdPreloader.destroy(adUnitId)
  }

  // [END stop_preload]

  // [START listen_events]
  private fun listenToAdEvents() {
    // Listen for ad events.
    val ad = appOpenAd
    if (ad == null) {
      Log.e(TAG, "App open ad is not ready yet.")
      return
    }

    ad.adEventCallback =
      object : AppOpenAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          // App open ad did show.
        }

        override fun onAdDismissedFullScreenContent() {
          // App open ad did dismiss.
          appOpenAd = null
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // App open ad failed to show.
          Log.e(TAG, "App open ad failed to show: ${fullScreenContentError.message}")
        }

        override fun onAdImpression() {
          // App open ad did record an impression.
        }

        override fun onAdClicked() {
          // App open ad did record a click.
        }
      }
  }

  // [END listen_events]

  // [START show_ad]
  private fun showAd(appOpenAd: AppOpenAd, activity: Activity) {
    // Show the ad.
    appOpenAd.show(activity)
  }

  // [END show_ad]

  private fun loadSingleAd(activity: Activity, adUnitId: String) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    AppOpenAd.load(
      AdRequest.Builder(adUnitId).build(),
      object : AdLoadCallback<AppOpenAd> {
        override fun onAdLoaded(ad: AppOpenAd) {
          // App open ad loaded.
          appOpenAd = ad
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          // App open ad failed to load.
          Log.e(TAG, "App open ad failed to load: ${adError.message}")
          appOpenAd = null
        }
      },
    )
    // [END single_load]
  }

  private companion object {
    const val TAG = "AppOpenAdSnippets"
  }
}
