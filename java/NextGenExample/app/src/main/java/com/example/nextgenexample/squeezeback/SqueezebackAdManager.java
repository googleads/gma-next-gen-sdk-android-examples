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

package com.example.nextgenexample.squeezeback;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.Constant;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAd;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdRequest;

/** Class that loads, manages lifecycle, and handles events for squeezeback ads. */
public final class SqueezebackAdManager {
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9983381271";

  @Nullable private static SqueezebackAd squeezebackAd;
  private static boolean isAdShowing;
  @Nullable private static OnAdShownListener onAdShownListener;
  @Nullable private static OnAdHiddenListener onAdHiddenListener;

  private SqueezebackAdManager() {}

  /** Listener invoked when a squeezeback ad is shown. */
  public interface OnAdShownListener {
    void onAdShown();
  }

  /** Listener invoked when a squeezeback ad is hidden. */
  public interface OnAdHiddenListener {
    void onAdHidden();
  }

  /** Listener invoked when a squeezeback ad loads successfully. */
  public interface OnAdLoadedListener {
    void onAdLoaded(@NonNull SqueezebackAd ad);
  }

  /** Listener invoked when a squeezeback ad fails to load. */
  public interface OnAdFailedToLoadListener {
    void onAdFailedToLoad(@NonNull LoadAdError adError);
  }

  @Nullable
  public static SqueezebackAd getSqueezebackAd() {
    return squeezebackAd;
  }

  /** Returns whether a squeezeback ad is currently showing on screen. */
  public static boolean isAdShowing() {
    return isAdShowing;
  }

  public static void setOnAdShownListener(@Nullable OnAdShownListener listener) {
    onAdShownListener = listener;
  }

  public static void setOnAdHiddenListener(@Nullable OnAdHiddenListener listener) {
    onAdHiddenListener = listener;
  }

  /**
   * Loads a squeezeback ad.
   *
   * @param onAdLoaded Optional callback invoked when the ad loads.
   * @param onAdFailedToLoad Optional callback invoked when the ad fails to load.
   */
  public static void loadAd(
      @Nullable OnAdLoadedListener onAdLoaded,
      @Nullable OnAdFailedToLoadListener onAdFailedToLoad) {
    if (isAdShowing) {
      Log.w(Constant.TAG, "Cannot load a new squeezeback ad while one is currently showing.");
      return;
    }

    SqueezebackAdRequest request = new SqueezebackAdRequest.Builder(AD_UNIT_ID).build();

    SqueezebackAd.load(
        request,
        new AdLoadCallback<SqueezebackAd>() {
          @Override
          public void onAdLoaded(@NonNull SqueezebackAd ad) {
            Log.d(Constant.TAG, "Squeezeback ad loaded.");
            destroyAd();
            squeezebackAd = ad;
            setAdEventCallback(ad);
            if (onAdLoaded != null) {
              onAdLoaded.onAdLoaded(ad);
            }
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.w(Constant.TAG, "Squeezeback ad failed to load: " + adError);
            if (onAdFailedToLoad != null) {
              onAdFailedToLoad.onAdFailedToLoad(adError);
            }
          }
        });
  }

  /** Shows the squeezeback ad. */
  public static void showAd(@NonNull Activity activity) {
    SqueezebackAd ad = squeezebackAd;
    if (ad == null) {
      Log.d(Constant.TAG, "No squeezeback ad available to show.");
      return;
    }
    ad.show(activity);
  }

  /** Hides the currently showing squeezeback ad. */
  public static void hideAd() {
    if (squeezebackAd != null) {
      squeezebackAd.hide();
    }
  }

  /** Destroys the squeezeback ad and cleans up resources. */
  public static void destroyAd() {
    isAdShowing = false;
    SqueezebackAd ad = squeezebackAd;
    if (ad == null) {
      return;
    }
    squeezebackAd = null;
    ad.setAdEventCallback(null);
    ad.hide();
    ad.destroy();
  }

  /** Checks if an ad exists and is available to show. */
  public static boolean isAdAvailable() {
    return squeezebackAd != null && !isAdShowing;
  }

  private static void setAdEventCallback(@NonNull SqueezebackAd ad) {
    ad.setAdEventCallback(
        new SqueezebackAdEventCallback() {
          @Override
          public void onAdShown() {
            Log.d(Constant.TAG, "Squeezeback ad shown.");
            isAdShowing = true;
            if (onAdShownListener != null) {
              onAdShownListener.onAdShown();
            }
          }

          @Override
          public void onAdHidden() {
            Log.d(Constant.TAG, "Squeezeback ad hidden.");
            isAdShowing = false;
            if (squeezebackAd == ad) {
              squeezebackAd = null;
            }
            ad.setAdEventCallback(null);
            ad.destroy();
            if (onAdHiddenListener != null) {
              onAdHiddenListener.onAdHidden();
            }
          }

          @Override
          public void onAdFailedToShow(@NonNull FullScreenContentError error) {
            Log.w(Constant.TAG, "Squeezeback ad failed to show: " + error);
            isAdShowing = false;
          }

          @Override
          public void onAdImpression() {
            Log.d(Constant.TAG, "Squeezeback ad recorded an impression.");
          }

          @Override
          public void onAdClicked() {
            Log.d(Constant.TAG, "Squeezeback ad recorded a click.");
          }

          @Override
          public void onAdShowedFullScreenContent() {
            Log.d(Constant.TAG, "Squeezeback ad showed full screen content.");
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            Log.d(Constant.TAG, "Squeezeback ad dismissed full screen content.");
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            Log.w(
                Constant.TAG,
                "Squeezeback ad failed to show full screen content: " + fullScreenContentError);
          }

          @Override
          public void onAdPaid(@NonNull AdValue value) {
            Log.d(
                Constant.TAG,
                "Squeezeback ad paid: " + value.getValueMicros() + " " + value.getCurrencyCode());
          }
        });
  }
}
