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

package com.example.nextgenexample.pictureinpicture;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.Constant;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAd;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdOptions;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdRequest;

/** Class that loads, manages lifecycle, and handles events for Picture-in-Picture (PiP) ads. */
public final class PictureInPictureAdManager {
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";

  private static PictureInPictureAd pipAd;
  private static OnAdShownListener onAdShownListener;
  private static OnAdHiddenListener onAdHiddenListener;

  private PictureInPictureAdManager() {}

  /** Listener invoked when a Picture-in-Picture ad is shown. */
  public interface OnAdShownListener {
    void onAdShown();
  }

  /** Listener invoked when a Picture-in-Picture ad is hidden. */
  public interface OnAdHiddenListener {
    void onAdHidden();
  }

  /** Listener invoked when a Picture-in-Picture ad loads successfully. */
  public interface OnAdLoadedListener {
    void onAdLoaded(@NonNull PictureInPictureAd ad);
  }

  /** Listener invoked when a Picture-in-Picture ad fails to load. */
  public interface OnAdFailedToLoadListener {
    void onAdFailedToLoad(@NonNull LoadAdError adError);
  }

  @Nullable
  public static PictureInPictureAd getPipAd() {
    return pipAd;
  }

  public static void setOnAdShownListener(@Nullable OnAdShownListener listener) {
    onAdShownListener = listener;
  }

  public static void setOnAdHiddenListener(@Nullable OnAdHiddenListener listener) {
    onAdHiddenListener = listener;
  }

  /**
   * Loads a Picture-in-Picture ad.
   *
   * @param onAdLoaded Optional callback invoked when the ad loads.
   * @param onAdFailedToLoad Optional callback invoked when the ad fails to load.
   */
  public static void loadAd(
      @Nullable OnAdLoadedListener onAdLoaded,
      @Nullable OnAdFailedToLoadListener onAdFailedToLoad) {
    PictureInPictureAdRequest request = new PictureInPictureAdRequest.Builder(AD_UNIT_ID).build();

    PictureInPictureAd.load(
        request,
        new AdLoadCallback<PictureInPictureAd>() {
          @Override
          public void onAdLoaded(@NonNull PictureInPictureAd ad) {
            Log.d(Constant.TAG, "Picture-in-Picture ad loaded.");
            if (pipAd != null) {
              pipAd.destroy();
            }
            pipAd = ad;
            setAdEventCallback(ad);
            if (onAdLoaded != null) {
              onAdLoaded.onAdLoaded(ad);
            }
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.w(Constant.TAG, "Picture-in-Picture ad failed to load: " + adError);
            if (onAdFailedToLoad != null) {
              onAdFailedToLoad.onAdFailedToLoad(adError);
            }
          }
        });
  }

  /** Shows the Picture-in-Picture ad with the provided options. */
  public static void showAd(
      @NonNull Activity activity, @NonNull PictureInPictureAdOptions options) {
    PictureInPictureAd ad = pipAd;
    if (ad == null) {
      Log.d(Constant.TAG, "No Picture-in-Picture ad available to show.");
      return;
    }
    ad.show(activity, options);
  }

  /** Hides the currently showing Picture-in-Picture ad. */
  public static void hideAd() {
    if (pipAd != null) {
      pipAd.hide();
    }
  }

  /** Destroys the Picture-in-Picture ad and cleans up resources. */
  public static void destroyAd() {
    if (pipAd != null) {
      pipAd.destroy();
      pipAd = null;
    }
  }

  /** Checks if an ad exists and is available to show. */
  public static boolean isAdAvailable() {
    return pipAd != null;
  }

  private static void setAdEventCallback(@NonNull PictureInPictureAd ad) {
    ad.setAdEventCallback(
        new PictureInPictureAdEventCallback() {
          @Override
          public void onAdShown() {
            Log.d(Constant.TAG, "Picture-in-Picture ad shown.");
            if (onAdShownListener != null) {
              onAdShownListener.onAdShown();
            }
          }

          @Override
          public void onAdHidden() {
            Log.d(Constant.TAG, "Picture-in-Picture ad hidden.");
            if (onAdHiddenListener != null) {
              onAdHiddenListener.onAdHidden();
            }
          }

          @Override
          public void onAdImpression() {
            Log.d(Constant.TAG, "Picture-in-Picture ad recorded an impression.");
          }

          @Override
          public void onAdClicked() {
            Log.d(Constant.TAG, "Picture-in-Picture ad recorded a click.");
          }

          @Override
          public void onAdShowedFullScreenContent() {
            Log.d(Constant.TAG, "Picture-in-Picture ad showed full screen content.");
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            Log.d(Constant.TAG, "Picture-in-Picture ad dismissed full screen content.");
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            Log.w(
                Constant.TAG,
                "Picture-in-Picture ad failed to show full screen content: "
                    + fullScreenContentError);
          }

          @Override
          public void onAdPaid(@NonNull AdValue value) {
            Log.d(
                Constant.TAG,
                "Picture-in-Picture ad paid: "
                    + value.getValueMicros()
                    + " "
                    + value.getCurrencyCode());
          }
        });
  }
}
