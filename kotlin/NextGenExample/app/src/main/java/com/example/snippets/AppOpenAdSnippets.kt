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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration

private class AppOpenSnippets {

  // [START load_ad]

  val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

  fun startPreloading() {
    // Preload the app open ad with a single ad request.
    val adRequest = AdRequest.Builder(AD_UNIT_ID).build()
    val preloadConfig = PreloadConfiguration(adRequest, 1)
    AppOpenAdPreloader.start(AD_UNIT_ID, preloadConfig)
  }

  // [END load_ad]

  fun setAdEventCallbacks(appOpenAd: AppOpenAd) {

    // [START ad_events]
    appOpenAd.adEventCallback =
      object : AppOpenAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          // Called when the app open ad showed.
        }

        override fun onAdDismissedFullScreenContent() {
          // Called when the app open ad dismissed.
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Called when the app open ad failed to show.
        }

        override fun onAdImpression() {
          // Called when the app open ad recorded an impression.
        }

        override fun onAdClicked() {
          // Called when the app open ad recorded a click.
        }
      }
  }

  // [END ad_events]

  // [START show_ad]
  fun showAdIfAvailable(activity: Activity) {
    // Do not show app open ad if another ad is showing.
    if (
      activity.javaClass.name ==
        "com.google.android.libraries.ads.mobile.sdk.internal.common.AdActivity"
    ) {
      return
    }
    // Poll for the app open ad.
    val appOpenAd = AppOpenAdPreloader.pollAd(AD_UNIT_ID)
    appOpenAd.show(activity)
  }

  // [END show_ad]

  // [START lifecycle_events]
  class MyApplication :
    Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    // Cache the current activity.
    private var currentActivity: Activity? = null

    override fun onCreate() {
      super<Application>.onCreate()
      // Register the application lifecycle callbacks.
      registerActivityLifecycleCallbacks(this)
      // Register the application lifecycle observer.
      ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
      currentActivity?.let { activity ->
        // Show an app open ad on a cold start.
        showAdIfAvailable(AD_UNIT_ID, activity)
      }
    }

    override fun onActivityStarted(activity: Activity) {
      // Cache the current activity.
      currentActivity = activity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
    // [END lifecycle_events]
  }
}
