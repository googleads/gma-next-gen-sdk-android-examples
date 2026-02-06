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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.Constant;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import java.util.Date;

/** Singleton object that loads and shows app open ads. */
public class AppOpenAdManager {

  /**
   * Interface definition for a callback to be invoked when an app open ad is complete (i.e.
   * dismissed or fails to show).
   */
  public interface OnShowAdCompleteListener {
    void onShowAdComplete();
  }

  private static AppOpenAdManager instance;
  private AppOpenAd appOpenAd;
  private boolean isLoadingAd = false;
  private boolean isShowingAd = false;

  /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
  private long loadTime = 0;

  public static synchronized AppOpenAdManager getInstance() {
    if (instance == null) {
      instance = new AppOpenAdManager();
    }
    return instance;
  }

  // [START load_ad]

  /**
   * Load an ad.
   *
   * @param context a context used to perform UI-related operations (e.g. display Toast messages).
   *     Loading the app open ad itself does not require a context.
   */
  public void loadAd(@NonNull Context context) {
    // Do not load ad if there is an unused ad or one is already loading.
    if (isLoadingAd || isAdAvailable()) {
      Log.d(Constant.TAG, "App open ad is either loading or has already loaded.");
      return;
    }

    isLoadingAd = true;
    AppOpenAd.load(
        new AdRequest.Builder(AppOpenFragment.AD_UNIT_ID).build(),
        new AdLoadCallback<AppOpenAd>() {
          @Override
          public void onAdLoaded(@NonNull AppOpenAd ad) {
            appOpenAd = ad;
            isLoadingAd = false;
            loadTime = new Date().getTime();
            Log.d(Constant.TAG, "App open ad loaded.");
            // [START_EXCLUDE silent]
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(context, "App open ad loaded.", Toast.LENGTH_SHORT).show());
            // [END_EXCLUDE]
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            isLoadingAd = false;
            Log.w(Constant.TAG, "App open ad failed to load: " + loadAdError);
            // [START_EXCLUDE silent]
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(
                                context,
                                "App open ad failed to load: " + loadAdError.getCode(),
                                Toast.LENGTH_SHORT)
                            .show());
            // [END_EXCLUDE]
          }
        });
  }

  // [END load_ad]

  // [START show_ad]
  /**
   * Show the ad if one isn't already showing.
   *
   * @param activity the activity that shows the app open ad.
   * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete.
   */
  public void showAdIfAvailable(
      @NonNull Activity activity, @Nullable OnShowAdCompleteListener onShowAdCompleteListener) {
    // If the app open ad is already showing, do not show the ad again.
    if (isShowingAd) {
      Log.d(Constant.TAG, "App open ad is already showing.");
      if (onShowAdCompleteListener != null) {
        onShowAdCompleteListener.onShowAdComplete();
      }
      return;
    }

    // If the app open ad is not available yet, invoke the callback.
    if (!isAdAvailable()) {
      Log.d(Constant.TAG, "App open ad is not ready yet.");
      if (onShowAdCompleteListener != null) {
        onShowAdCompleteListener.onShowAdComplete();
      }
      return;
    }

    appOpenAd.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            Log.d(Constant.TAG, "App open ad shown.");
            // [START_EXCLUDE silent]
            activity.runOnUiThread(
                () -> Toast.makeText(activity, "App open ad shown.", Toast.LENGTH_SHORT).show());
            // [END_EXCLUDE]
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            Log.d(Constant.TAG, "App open ad dismissed.");
            appOpenAd = null;
            isShowingAd = false;
            // [START_EXCLUDE silent]
            activity.runOnUiThread(
                () ->
                    Toast.makeText(activity, "App open ad dismissed.", Toast.LENGTH_SHORT).show());
            // [END_EXCLUDE]
            if (onShowAdCompleteListener != null) {
              onShowAdCompleteListener.onShowAdComplete();
            }
            loadAd(activity);
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            appOpenAd = null;
            isShowingAd = false;
            Log.w(Constant.TAG, "App open ad failed to show: " + fullScreenContentError);
            // [START_EXCLUDE silent]
            new Handler(Looper.getMainLooper())
                .post(
                    () ->
                        Toast.makeText(activity, "App open ad failed to show.", Toast.LENGTH_SHORT)
                            .show());
            // [END_EXCLUDE]
            if (onShowAdCompleteListener != null) {
              onShowAdCompleteListener.onShowAdComplete();
            }
            loadAd(activity);
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

  // [END show_ad]

  /** Check if ad was loaded more than n hours ago. */
  private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
    long dateDifference = new Date().getTime() - loadTime;
    long numMilliSecondsPerHour = 3600000L;
    return dateDifference < numMilliSecondsPerHour * numHours;
  }

  /** Check if ad exists and can be shown. */
  private boolean isAdAvailable() {
    // App open ads expire after four hours. Ads rendered more than four hours after request time
    // are no longer valid and may not earn revenue.
    return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
  }
}
