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
import android.util.Log;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
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

  // [START start_preload]
  private void startPreloading(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    InterstitialAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END start_preload]

  // [START set_buffer_size]
  private void setBufferSize(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 3);
    InterstitialAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END set_buffer_size]

  // [START set_callback]
  private void startPreloadingWithCallback(String adUnitId) {
    PreloadCallback preloadCallback =
        // [Important] Don't call ad preloader start() or pollAd() within the PreloadCallback.
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(String preloadId, LoadAdError adError) {
            Log.e(
                TAG,
                String.format(
                    "Interstitial preload ad %s failed to load with error: %s",
                    preloadId, adError.getMessage()));
            // [Optional] Get the error response info for additional details.
            // ResponseInfo responseInfo = adError.getResponseInfo();
          }

          @Override
          public void onAdsExhausted(String preloadId) {
            Log.i(TAG, "Interstitial preload ad " + preloadId + " is not available");
            // [Important] Don't call ad preloader start() or pollAd() from onAdsExhausted.
          }

          @Override
          public void onAdPreloaded(String preloadId, ResponseInfo responseInfo) {
            Log.i(TAG, "Interstitial preload ad " + preloadId + " is available");
          }
        };

    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
  }

  // [END set_callback]

  private void pollAd(String adUnitId) {
    // [START poll_ad]
    // Polling returns the next available ad and loads another ad in the background.
    final InterstitialAd ad = InterstitialAdPreloader.pollAd(adUnitId);
    // [END poll_ad]
  }

  // [START pollAndShowAd]
  private void pollAndShowAd(Activity activity, String adUnitId) {
    // Polling returns the next available ad and loads another ad in the background.
    final InterstitialAd ad = InterstitialAdPreloader.pollAd(adUnitId);

    // Interact with the ad object as needed.
    if (ad != null) {
      Log.d(TAG, "Interstitial ad response info: " + ad.getResponseInfo());
      ad.setAdEventCallback(
          new InterstitialAdEventCallback() {
            @Override
            public void onAdImpression() {
              Log.d(TAG, "Interstitial ad recorded an impression.");
            }

            @Override
            public void onAdPaid(AdValue value) {
              Log.d(
                  TAG,
                  "Interstitial ad onPaidEvent: "
                      + value.getValueMicros()
                      + " "
                      + value.getCurrencyCode());
            }
          });

      // Show the ad.
      ad.show(activity);
    }
  }

  // [END pollAndShowAd]

  // [START isAdAvailable]
  private boolean isAdAvailable(String adUnitId) {
    return InterstitialAdPreloader.isAdAvailable(adUnitId);
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private void stopPreloading(String adUnitId) {
    // Stops the preloading and destroys preloaded ads.
    InterstitialAdPreloader.destroy(adUnitId);
    // Stops the preloading and destroys all ads.
    InterstitialAdPreloader.destroyAll();
  }
  // [END stop_preload]
}
