package com.example.snippets;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;

/** Java code snippets for the developer guide. */
public final class AppOpenAdSnippets {

  private static final String TAG = "AppOpenAdSnippets";
  private AppOpenAd appOpenAd;

  // [START start_preload]
  private void startPreloading(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    AppOpenAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END start_preload]

  // [START set_buffer_size]
  private void setBufferSize(String adUnitId) {
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    // Four is the recommended maximum buffer size.
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, 4);
    AppOpenAdPreloader.start(adUnitId, preloadConfig);
  }

  // [END set_buffer_size]

  // [START start_preload_with_callback]
  private void startPreloadingWithCallback(String adUnitId) {
    PreloadCallback preloadCallback =
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(@NonNull String preloadId, @NonNull LoadAdError adError) {
            Log.d(
                TAG,
                String.format(
                    "App open preload ad %s failed to load with error: %s",
                    preloadId, adError.getMessage()));
          }

          @Override
          public void onAdsExhausted(@NonNull String preloadId) {
            Log.i(TAG, String.format("App open preload ad %s is not available", preloadId));
          }

          @Override
          public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
            Log.i(TAG, String.format("App open preload ad %s is available", preloadId));
          }
        };
    AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
    PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);
    AppOpenAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
  }

  // [END start_preload_with_callback]

  // [START pollAndShowAd]
  private void pollAndShowAd(Activity activity, String adUnitId) {
    // Polling returns the next available ad and loads another ad in the background.
    AppOpenAd ad = AppOpenAdPreloader.pollAd(adUnitId);

    // Interact with the ad object as needed.
    if (ad == null) {
      Log.e(TAG, "App open ad is not available.");
      return;
    }

    Log.d(TAG, "App open ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "App open ad recorded an impression.");
          }
        });

    // Show the ad.
    ad.show(activity);
  }

  // [END pollAndShowAd]

  private void peekAdResponseInfo(String preloadId) {
    // [START peek_ad]
    ResponseInfo responseInfo = AppOpenAdPreloader.peekAdResponseInfo(preloadId);
    if (responseInfo == null) {
      Log.e(TAG, "Failed to peek ad response info.");
      return;
    }

    Log.d(TAG, "Peeked ad response ID: " + responseInfo.getResponseId());
    // [END peek_ad]
  }

  // [START isAdAvailable]
  private boolean isAdAvailable(String adUnitId) {
    return AppOpenAdPreloader.isAdAvailable(adUnitId);
  }

  // [END isAdAvailable]

  // [START stop_preload]
  private void stopPreloading(String adUnitId) {
    // Stops the preloading and destroy preloaded ads.
    AppOpenAdPreloader.destroy(adUnitId);
  }

  // [END stop_preload]

  // [START listen_events]
  private void listenToAdEvents() {
    // Listen for ad events.
    if (appOpenAd == null) {
      Log.e(TAG, "App open ad is not ready yet.");
      return;
    }

    appOpenAd.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // App open ad did show.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // App open ad did dismiss.
            appOpenAd = null;
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            // App open ad failed to show.
            Log.e(TAG, "App open ad failed to show: " + fullScreenContentError.getMessage());
          }

          @Override
          public void onAdImpression() {
            // App open ad did record an impression.
          }

          @Override
          public void onAdClicked() {
            // App open ad did record a click.
          }
        });
  }

  // [END listen_events]

  // [START show_ad]
  private void showAd(AppOpenAd appOpenAd, Activity activity) {
    // Show the ad.
    appOpenAd.show(activity);
  }

  // [END show_ad]

  private void loadSingleAd(Activity activity, String adUnitId) {
    // [START single_load]

    // Load ads after you initialize MobileAds.
    AppOpenAd.load(
        new AdRequest.Builder(adUnitId).build(),
        new AdLoadCallback<AppOpenAd>() {
          @Override
          public void onAdLoaded(AppOpenAd ad) {
            // App open ad loaded.
            appOpenAd = ad;
          }

          @Override
          public void onAdFailedToLoad(LoadAdError adError) {
            // App open ad failed to load.
            Log.e(TAG, "App open ad failed to load: " + adError.getMessage());
            appOpenAd = null;
          }
        });
    // [END single_load]
  }
}
