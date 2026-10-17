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
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAd;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdOptions;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezeBackAdRequest;

/** Class that loads, manages lifecycle, and handles events for squeezeback ads. */
public final class SqueezeBackAdManager {
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";

  private static SqueezeBackAd squeezebackAd;
  private static OnAdShownListener onAdShownListener;
  private static OnAdHiddenListener onAdHiddenListener;

  private SqueezeBackAdManager() {}

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
    void onAdLoaded(@NonNull SqueezeBackAd ad);
  }

  /** Listener invoked when a squeezeback ad fails to load. */
  public interface OnAdFailedToLoadListener {
    void onAdFailedToLoad(@NonNull LoadAdError adError);
  }

  @Nullable
  public static SqueezeBackAd getSqueezeBackAd() {
    return squeezebackAd;
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
    SqueezeBackAdRequest request = new SqueezeBackAdRequest.Builder(AD_UNIT_ID).build();

    SqueezeBackAd.load(
        request,
        new AdLoadCallback<SqueezeBackAd>() {
          @Override
          public void onAdLoaded(@NonNull SqueezeBackAd ad) {
            Log.d(Constant.TAG, "Squeezeback ad loaded.");
            if (squeezebackAd != null) {
              squeezebackAd.destroy();
            }
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
    SqueezeBackAd ad = squeezebackAd;
    if (ad == null) {
      Log.d(Constant.TAG, "No squeezeback ad available to show.");
      return;
    }
    SqueezeBackAdOptions options = new SqueezeBackAdOptions.Builder().build();
    ad.show(activity, options);
  }

  /** Hides the currently showing squeezeback ad. */
  public static void hideAd() {
    if (squeezebackAd != null) {
      squeezebackAd.hide();
    }
  }

  /** Destroys the squeezeback ad and cleans up resources. */
  public static void destroyAd() {
    if (squeezebackAd != null) {
      squeezebackAd.destroy();
      squeezebackAd = null;
    }
  }

  /** Checks if an ad exists and is available to show. */
  public static boolean isAdAvailable() {
    return squeezebackAd != null;
  }

  private static void setAdEventCallback(@NonNull SqueezeBackAd ad) {
    ad.setAdEventCallback(
        new SqueezeBackAdEventCallback() {
          @Override
          public void onAdShown() {
            Log.d(Constant.TAG, "Squeezeback ad shown.");
            if (onAdShownListener != null) {
              onAdShownListener.onAdShown();
            }
          }

          @Override
          public void onAdHidden() {
            Log.d(Constant.TAG, "Squeezeback ad hidden.");
            if (onAdHiddenListener != null) {
              onAdHiddenListener.onAdHidden();
            }
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
