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
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.example.nextgenexample.Constant
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdActivity
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import java.lang.ref.WeakReference

/** Singleton object that loads and shows app open ads. */
object AppOpenAdManager : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

  // Replace this test ad unit ID with your own ad unit ID.
  private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
  private const val KEY_ENABLE_APP_OPEN = "enable_app_open_ads"
  private var isShowingAd = false
  private var currentActivity: WeakReference<Activity>? = null

  /**
   * Initializes AppOpenAdManager to observe app foregrounding events (via ProcessLifecycleOwner)
   * and activity events (via ActivityLifecycleCallbacks). This allows it to know when to show an
   * ad, and which activity to use as context.
   */
  fun initialize(application: Application) {
    application.registerActivityLifecycleCallbacks(this)
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)

    val activity =
      currentActivity?.get()
        ?: run {
          Log.d(Constant.TAG, "App is in the foreground, but currentActivity is null.")
          return
        }

    if (isAppOpenAdEnabled(activity)) {
      Log.d(Constant.TAG, "App is in the foreground, showing app open ad.")
      showAdIfAvailable(activity)
    } else {
      Log.d(Constant.TAG, "App is in the foreground, but app open ads are disabled.")
    }
  }

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {
    currentActivity = WeakReference(activity)
  }

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {
    // For cold starts, it is expected that currentActivity will still be null.
    if (currentActivity?.get() == activity) {
      currentActivity?.clear()
    }
  }

  /**
   * Checks if App Open ads are configured to be shown when the application comes to the foreground.
   * This setting is retrieved from SharedPreferences.
   *
   * @param context The Context used to access SharedPreferences.
   * @return `true` if app open ads are enabled, `false` otherwise. Defaults to `false` if no value
   *   is found in SharedPreferences.
   */
  fun isAppOpenAdEnabled(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    return prefs.getBoolean(KEY_ENABLE_APP_OPEN, false)
  }

  /**
   * Sets whether App Open ads should be shown when the application comes to the foreground. This
   * setting is persisted in SharedPreferences.
   *
   * @param context The Context used to access SharedPreferences.
   * @param enabled `true` to enable app open ads, `false` to disable them.
   */
  fun setAppOpenAdEnabled(context: Context, enabled: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    prefs.edit { putBoolean(KEY_ENABLE_APP_OPEN, enabled) }
  }

  /** Starts the preloading process for an App Open Ad. */
  fun startPreloading() {
    val adRequest: AdRequest = AdRequest.Builder(AD_UNIT_ID).build()
    // Preload the app open ad with a single ad request.
    val preloadConfig = PreloadConfiguration(adRequest, 1)
    AppOpenAdPreloader.start(AD_UNIT_ID, preloadConfig)
  }

  /** Stops the preloading process for an App Open Ad. */
  fun stopPreloading() {
    AppOpenAdPreloader.destroy(AD_UNIT_ID)
  }

  /**
   * Show the ad if one isn't already showing.
   *
   * @param activity the activity that shows the app open ad.
   * @param onShowAdComplete An optional callback that is invoked when the ad show lifecycle is
   *   complete.
   */
  fun showAdIfAvailable(activity: Activity, onShowAdComplete: (() -> Unit)? = null) {
    // If the app open ad is already showing, do not show the ad again.
    if (isShowingAd) {
      Log.d(Constant.TAG, "App open ad is already showing.")
      onShowAdComplete?.invoke()
      return
    }

    // Do not show app open ad if other ad is showing.
    if (activity is AdActivity) {
      Log.d(Constant.TAG, "Ad is already showing.")
      onShowAdComplete?.invoke()
      return
    }

    // Poll for the app open ad.
    val appOpenAd = AppOpenAdPreloader.pollAd(AD_UNIT_ID)

    // If the app open ad is not available yet, invoke the callback.
    if (appOpenAd == null) {
      Log.d(Constant.TAG, "App open ad is not ready yet.")
      onShowAdComplete?.invoke()
      return
    }

    appOpenAd.adEventCallback =
      object : AppOpenAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "App open ad showed.")
          activity.runOnUiThread {
            Toast.makeText(activity, "App open ad shown.", Toast.LENGTH_SHORT).show()
          }
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "App open ad dismissed.")
          isShowingAd = false
          activity.runOnUiThread {
            Toast.makeText(activity, "App open ad dismissed.", Toast.LENGTH_SHORT).show()
          }
          onShowAdComplete?.invoke()
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          isShowingAd = false
          activity.runOnUiThread {
            Toast.makeText(activity, "App open ad failed to show.", Toast.LENGTH_SHORT).show()
          }
          Log.e(Constant.TAG, "App open ad failed to show: $fullScreenContentError")
          onShowAdComplete?.invoke()
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "App open ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "App open ad recorded a click.")
        }
      }

    isShowingAd = true
    appOpenAd.show(activity)
  }
}
