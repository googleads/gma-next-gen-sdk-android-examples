// Copyright 2026 Google LLC
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
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdPreloader;

/** Java code snippets for the developer guide. */
public final class RewardedInterstitialAdSnippets {

  private static final String TAG = "RewardedInterstitialAdSnippets";
  private RewardedInterstitialAd rewardedInterstitialAd;

  private void startPreloading(String adUnitId) {
    // [START start_preload]
    // Call start() once after SDK initialization.
    // Preload only one ad unit per format to optimize performance.
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig);
    // [END start_preload]
  }

  private void startPreloadingWithCallback(String adUnitId) {
    // [START start_preload_with_callback]
    PreloadCallback preloadCallback =
        // [Important] Don't call ad preloader start() or pollAd() within the PreloadCallback.
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(@NonNull String preloadId, @NonNull LoadAdError adError) {
            Log.d(
                TAG,
                String.format(
                    "Rewarded interstitial preload ad %s failed to load with error: %s",
                    preloadId, adError.getMessage()));
            // [Optional] Get the error response info for additional details.
            // ResponseInfo responseInfo = adError.getResponseInfo();
          }

          @Override
          public void onAdsExhausted(@NonNull String preloadId) {
            Log.i(TAG, "Rewarded interstitial preload ad " + preloadId + " is not available");
            // [Important] Don't call ad preloader start() or pollAd() from onAdsExhausted.
          }

          @Override
          public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
            Log.i(TAG, "Rewarded interstitial preload ad " + preloadId + " is available");
          }
        };
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
    // [END start_preload_with_callback]
  }

  // [START pollAndShowAd]
  private void pollAndShowAd(Activity activity, String adUnitId) {
    // Polling returns the next available ad and loads another ad in the background.
    final RewardedInterstitialAd ad = RewardedInterstitialAdPreloader.pollAd(adUnitId);

    // Interact with the ad object as needed.
    if (ad == null) {
      Log.e(TAG, "Rewarded interstitial ad is not available.");
      return;
    }

    Log.d(TAG, "Rewarded interstitial ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new RewardedInterstitialAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "Rewarded interstitial ad recorded an impression.");
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
    ResponseInfo responseInfo = RewardedInterstitialAdPreloader.peekAdResponseInfo(preloadId);
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.");
      return;
    }

    Log.d(TAG, "Peeked ad response ID: " + responseInfo.getResponseId());
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private boolean isAdAvailable(String adUnitId) {
    return RewardedInterstitialAdPreloader.isAdAvailable(adUnitId);
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private void stopPreloading(String adUnitId) {
    // Stops the preloading and destroy preloaded ads.
    RewardedInterstitialAdPreloader.destroy(adUnitId);
  }

  // [END stop_preload]

  private void setBufferSize(String adUnitId) {
    // [START set_buffer_size]
    // Maintain small or default buffer size unless rapid transitions are expected.
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 4);
    RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig);
    // [END set_buffer_size]
  }

  // [START listen_events]
  private void listenToAdEvents() {
    // Listen for ad events.
    if (rewardedInterstitialAd == null) {
      Log.e(TAG, "Rewarded interstitial ad is not ready yet.");
      return;
    }

    rewardedInterstitialAd.setAdEventCallback(
        new RewardedInterstitialAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Rewarded interstitial ad did show.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Rewarded interstitial ad did dismiss.
            rewardedInterstitialAd = null;
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            // Rewarded interstitial ad failed to show.
            Log.e(
                TAG,
                "Rewarded interstitial ad failed to show: " + fullScreenContentError.getMessage());
          }

          @Override
          public void onAdImpression() {
            // Rewarded interstitial ad did record an impression.
          }

          @Override
          public void onAdClicked() {
            // Rewarded interstitial ad did record a click.
          }
        });
  }

  // [END listen_events]

  // [START show_ad]
  private void showAd(RewardedInterstitialAd rewardedInterstitialAd, Activity activity) {
    // Show the ad.
    rewardedInterstitialAd.show(
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
    RewardedInterstitialAd.load(
        new AdRequest.Builder(adUnitId).build(),
        new AdLoadCallback<RewardedInterstitialAd>() {
          @Override
          public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
            // Rewarded interstitial ad loaded.
            rewardedInterstitialAd = ad;
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            // Rewarded interstitial ad failed to load.
            Log.e(TAG, "Rewarded interstitial ad failed to load: " + adError.getMessage());
            rewardedInterstitialAd = null;
          }
        });
    // [END single_load]
  }
}
