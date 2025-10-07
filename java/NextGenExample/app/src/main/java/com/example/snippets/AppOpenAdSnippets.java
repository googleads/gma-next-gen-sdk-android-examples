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

package com.example.snippets;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;

/** AdMob App Open Ad snippets. */
public class AppOpenSnippets {

  // [START load_ad]
  public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";

  public void startPreloading() {
    // Preload the app open ad with a single ad request.
    AdRequest adRequest = new AdRequest.Builder(AD_UNIT_ID).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 1);
    AppOpenAdPreloader.start(AD_UNIT_ID, preloadConfig);
  }

  // [END load_ad]

  public void setAdEventCallbacks(AppOpenAd appOpenAd) {
    // [START ad_events]
    appOpenAd.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Called when the app open ad showed.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Called when the app open ad is dismissed.
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            // Called when the app open ad failed to show.
          }

          @Override
          public void onAdImpression() {
            // Called when the app open ad recorded an impression.
          }

          @Override
          public void onAdClicked() {
            // Called when the app open ad recorded a click.
          }
        });
    // [END ad_events]
  }

  // [START show_ad]
  public void showAdIfAvailable(@NonNull Activity activity) {
    // Do not show app open ad if other ad is showing.
    if (activity
        .getClass()
        .getName()
        .equals("com.google.android.libraries.ads.mobile.sdk.internal.common.AdActivity")) {
      return;
    }
    // Poll for the app open ad.
    AppOpenAd appOpenAd = AppOpenAdPreloader.pollAd(AD_UNIT_ID);
    appOpenAd.show(activity);
  }

  // [END show_ad]

  // [START lifecycle_events]
  /** Application class that shows app open ads on app foregrounding. */
  public abstract class MyApplication extends Application
      implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    // Cache the current activity.
    private Activity currentActivity;

    @Override
    public void onCreate() {
      super.onCreate();
      // Register the application lifecycle callbacks.
      registerActivityLifecycleCallbacks(this);
      // Register the application lifecycle observer.
      ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
      if (currentActivity != null) {
        // Show an app open ad on a cold start.
        showAdIfAvailable(AD_UNIT_ID, currentActivity);
      }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
      // Cache the current activity.
      currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityCreated(
        @NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
  }
  // [END lifecycle_events]
}
