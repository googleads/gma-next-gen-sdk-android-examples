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
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdPreloader;

/** Java code snippets for the developer guide. */
public final class RewardedAdSnippets {

  private static final String TAG = "RewardedAdSnippets";
  private RewardedAd rewardedAd;

  // [START start_preload]
  private void startPreloading(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    RewardedAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END start_preload]

  // [START set_buffer_size]
  private void setBufferSize(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    // Maintain small or default buffer size unless rapid transitions are expected.
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 4);
    RewardedAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END set_buffer_size]

  // [START start_preload_with_callback]
  private void startPreloadingWithCallback(String adUnitId) {
    PreloadCallback preloadCallback =
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(String preloadId, LoadAdError adError) {
            Log.d(
                TAG,
                String.format(
                    "Rewarded preload ad %s failed to load with error: %s",
                    preloadId, adError.getMessage()));
          }

          @Override
          public void onAdsExhausted(String preloadId) {
            Log.i(TAG, "Rewarded preload ad " + preloadId + " is not available");
          }

          @Override
          public void onAdPreloaded(String preloadId, ResponseInfo responseInfo) {
            Log.i(TAG, "Rewarded preload ad " + preloadId + " is available");
          }
        };

    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    RewardedAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
  }

  // [END start_preload_with_callback]

  // [START pollAndShowAd]
  private void pollAndShowAd(Activity activity, String adUnitId) {
    // Polling returns the next available ad and loads another ad in the background.
    final RewardedAd ad = RewardedAdPreloader.pollAd(adUnitId);

    // Interact with the ad object as needed.
    if (ad == null) {
      Log.e(TAG, "Rewarded ad is not available.");
      return;
    }

    Log.d(TAG, "Rewarded ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new RewardedAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "Rewarded ad recorded an impression.");
          }
        });

    // Show the ad.
    ad.show(
        activity,
        rewardItem -> {
          Log.d(TAG, "User earned reward: " + rewardItem.getAmount());
        });
  }

  // [END pollAndShowAd]

  private void peekAdResponseInfo(String preloadId) {
    // [START peek_ad]
    ResponseInfo responseInfo = RewardedAdPreloader.peekAdResponseInfo(preloadId);
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.");
      return;
    }

    Log.d(TAG, "Peeked ad response ID: " + responseInfo.getResponseId());
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private boolean isAdAvailable(String adUnitId) {
    return RewardedAdPreloader.isAdAvailable(adUnitId);
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private void stopPreloading(String adUnitId) {
    // Stops the preloading and destroy preloaded ads.
    RewardedAdPreloader.destroy(adUnitId);
  }

  // [END stop_preload]

  // [START listen_events]
  private void listenToAdEvents() {
    // Listen for ad events.
    if (rewardedAd == null) {
      Log.e(TAG, "Rewarded ad is not ready yet.");
      return;
    }

    rewardedAd.setAdEventCallback(
        new RewardedAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Rewarded ad did show.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Rewarded ad did dismiss.
            rewardedAd = null;
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              FullScreenContentError fullScreenContentError) {
            // Rewarded ad failed to show.
            Log.e(TAG, "Rewarded ad failed to show: " + fullScreenContentError.getMessage());
          }

          @Override
          public void onAdImpression() {
            // Rewarded ad did record an impression.
          }

          @Override
          public void onAdClicked() {
            // Rewarded ad did record a click.
          }
        });
  }

  // [END listen_events]

  // [START show_ad]
  private void showAd(RewardedAd rewardedAd, Activity activity) {
    // Show the ad.
    rewardedAd.show(
        activity,
        new OnUserEarnedRewardListener() {
          @Override
          public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
            // User earned the reward.
            int rewardAmount = rewardItem.getAmount();
            String rewardType = rewardItem.getType();
          }
        });
  }

  // [END show_ad]

  private void loadSingleAd(Activity activity, String adUnitId) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    RewardedAd.load(
        new AdRequest.Builder(adUnitId).build(),
        new AdLoadCallback<RewardedAd>() {
          @Override
          public void onAdLoaded(@NonNull RewardedAd ad) {
            // Rewarded ad loaded.
            rewardedAd = ad;
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            // Rewarded ad failed to load.
            Log.e(TAG, "Rewarded ad failed to load: " + adError.getMessage());
            rewardedAd = null;
          }
        });
    // [END single_load]
  }
}
