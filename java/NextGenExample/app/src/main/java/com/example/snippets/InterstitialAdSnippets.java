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
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader;

/** Java code snippets for the developer guide. */
public final class InterstitialAdSnippets {

  private static final String TAG = "InterstitialAdSnippets";
  private InterstitialAd interstitialAd;

  // [START start_preload]
  private void startPreloading(String adUnitId) {
    // Call start() once after SDK initialization.
    // Preload only one ad unit per format to optimize performance.
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    InterstitialAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END start_preload]

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
                    "Interstitial preload ad %s failed to load with error: %s",
                    preloadId, adError.getMessage()));
            // [Optional] Get the error response info for additional details.
            // ResponseInfo responseInfo = adError.getResponseInfo();
          }

          @Override
          public void onAdsExhausted(@NonNull String preloadId) {
            Log.i(TAG, "Interstitial preload ad " + preloadId + " is not available");
            // [Important] Don't call ad preloader start() or pollAd() from onAdsExhausted.
          }

          @Override
          public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
            Log.i(TAG, "Interstitial preload ad " + preloadId + " is available");
          }
        };

    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
    // [END start_preload_with_callback]
  }

  // [START pollAndShowAd]
  private void pollAndShowAd(Activity activity, String adUnitId) {
    // Polling returns the next available ad and loads another ad in the background.
    final InterstitialAd ad = InterstitialAdPreloader.pollAd(adUnitId);

    // Interact with the ad object as needed.
    if (ad == null) {
      Log.e(TAG, "Interstitial ad is not available.");
      return;
    }

    Log.d(TAG, "Interstitial ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new InterstitialAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "Interstitial ad recorded an impression.");
          }
        });

    // Show the ad.
    ad.show(activity);
  }

  // [END pollAndShowAd]

  private void peekAdResponseInfo(String preloadId) {
    // [START peek_ad]
    ResponseInfo responseInfo = InterstitialAdPreloader.peekAdResponseInfo(preloadId);
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.");
      return;
    }

    Log.d(TAG, "Peeked ad response ID: " + responseInfo.getResponseId());
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private boolean isAdAvailable(String adUnitId) {
    return InterstitialAdPreloader.isAdAvailable(adUnitId);
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private void stopPreloading(String adUnitId) {
    // Stops the preloading and destroy preloaded ads.
    InterstitialAdPreloader.destroy(adUnitId);
  }

  // [END stop_preload]

  // [START set_buffer_size]
  private void setBufferSize(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    // Define a PreloadConfiguration and set the buffer size to 2 preloaded ads.
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 2);
    InterstitialAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END set_buffer_size]

  private void loadSingleAd(Activity activity, String adUnitId) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    InterstitialAd.load(
        new AdRequest.Builder(adUnitId).build(),
        new AdLoadCallback<InterstitialAd>() {
          @Override
          public void onAdLoaded(@NonNull InterstitialAd ad) {
            // Interstitial ad loaded.
            interstitialAd = ad;
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            // Interstitial ad failed to load.
            Log.e(TAG, "Interstitial ad failed to load: " + adError.getMessage());
            interstitialAd = null;
          }
        });
    // [END single_load]
  }

  private void listenToAdEvents() {
    // [START listen_events]
    // Listen for ad events.
    if (interstitialAd == null) {
      Log.e(TAG, "Interstitial ad is not ready yet.");
      return;
    }

    interstitialAd.setAdEventCallback(
        new InterstitialAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Interstitial ad did show.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Interstitial ad did dismiss.
            interstitialAd = null;
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            // Interstitial ad failed to show.
            Log.e(TAG, "Interstitial ad failed to show: " + fullScreenContentError.getMessage());
          }

          @Override
          public void onAdImpression() {
            // Interstitial ad did record an impression.
          }

          @Override
          public void onAdClicked() {
            // Interstitial ad did record a click.
          }
        });
    // [END listen_events]
  }

  // [START show_ad]
  private void showAd(InterstitialAd interstitialAd, Activity activity) {
    // Show the ad.
    interstitialAd.show(activity);
  }

  // [END show_ad]
}
