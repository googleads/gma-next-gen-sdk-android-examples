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

package com.example.nextgenexample.appopen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.example.nextgenexample.Constant;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;

/** Singleton object that loads and shows app open ads. */
public class AppOpenAdManager {

  private static AppOpenAdManager instance;

  // Replace this test ad unit ID with your own ad unit ID.
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";
  private static final String KEY_ENABLE_APP_OPEN_AD_ON_COLD_START =
      "enable_app_open_ad_on_cold_start";
  private boolean isShowingAd = false;

  public static synchronized AppOpenAdManager getInstance() {
    if (instance == null) {
      instance = new AppOpenAdManager();
    }
    return instance;
  }

  /**
   * Checks if App Open ads are configured to be shown on a cold start of the application. This
   * setting is retrieved from SharedPreferences.
   *
   * @param context The Context used to access SharedPreferences.
   * @return {@code true} if app open ads on cold start are enabled, {@code false} otherwise.
   *     Defaults to {@code false} if no value is found in SharedPreferences.
   */
  public boolean isAppOpenAdOnColdStartEnabled(Context context) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    return prefs.getBoolean(KEY_ENABLE_APP_OPEN_AD_ON_COLD_START, false);
  }

  /**
   * Sets whether App Open ads should be shown on a cold start of the application. This setting is
   * persisted in SharedPreferences.
   *
   * @param context The Context used to access SharedPreferences.
   * @param enabled {@code true} to enable app open ads on cold start, {@code false} to disable
   *     them.
   */
  public void setAppOpenAdOnColdStartEnabled(Context context, boolean enabled) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    prefs.edit().putBoolean(KEY_ENABLE_APP_OPEN_AD_ON_COLD_START, enabled).apply();
    }

  /** Starts the preloading process for an App Open Ad. */
  public void startPreloading() {
    AdRequest adRequest = new AdRequest.Builder(AD_UNIT_ID).build();
    // Preload the app open ad with a single ad request.
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 1);
    AppOpenAdPreloader.start(AD_UNIT_ID, preloadConfig);
  }

  /** Stops the preloading process for an App Open Ad. */
  public void stopPreloading() {
    AppOpenAdPreloader.destroy(AD_UNIT_ID);
  }

  /**
   * Show the ad if one isn't already showing.
   *
   * @param activity the activity that shows the app open ad.
   * @param onShowAdComplete An optional Runnable that is run when the ad show lifecycle is
   *     complete.
   */
  public void showAdIfAvailable(@NonNull Activity activity, @Nullable Runnable onShowAdComplete) {
    // If the app open ad is already showing, do not show the ad again.
    if (isShowingAd) {
      Log.d(Constant.TAG, "App open ad is already showing.");
      if (onShowAdComplete != null) {
        onShowAdComplete.run();
      }
      return;
    }

    // Poll for the app open ad.
    AppOpenAd appOpenAd = AppOpenAdPreloader.pollAd(AD_UNIT_ID);

    // If the app open ad is not available yet, invoke the callback.
    if (appOpenAd == null) {
      Log.d(Constant.TAG, "App open ad is not ready yet.");
      if (onShowAdComplete != null) {
        onShowAdComplete.run();
      }
      return;
    }

    appOpenAd.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            Log.d(Constant.TAG, "App open ad shown.");
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(activity, "App open ad shown.", Toast.LENGTH_SHORT).show());
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            Log.d(Constant.TAG, "App open ad dismissed.");
            isShowingAd = false;
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(activity, "App open ad dismissed.", Toast.LENGTH_SHORT)
                            .show());
            if (onShowAdComplete != null) {
              onShowAdComplete.run();
            }
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            isShowingAd = false;
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(activity, "App open ad failed to show.", Toast.LENGTH_SHORT)
                            .show());
            Log.e(Constant.TAG, "App open ad failed to show: " + fullScreenContentError);
            if (onShowAdComplete != null) {
              onShowAdComplete.run();
            }
          }

          @Override
          public void onAdImpression() {
            Log.d(Constant.TAG, "App open ad recorded an impression.");
          }

          @Override
          public void onAdClicked() {
            Log.d(Constant.TAG, "App open ad recorded a click.");
          }
        });

    isShowingAd = true;
    appOpenAd.show(activity);
  }
}
