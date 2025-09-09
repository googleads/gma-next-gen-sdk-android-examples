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
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerSignalRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeSignalRequest;
import com.google.android.libraries.ads.mobile.sdk.signal.Signal;
import com.google.android.libraries.ads.mobile.sdk.signal.SignalError;
import com.google.android.libraries.ads.mobile.sdk.signal.SignalGenerationCallback;
import java.util.List;

/** Java code snippets for the developer guide. */
public class AdMobSCARSnippets {

  public void loadNative(String adUnitId) {
    // [START signal_request_native]
    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    NativeSignalRequest signalRequest =
        new NativeSignalRequest.Builder("SIGNAL_TYPE")
            .setNativeAdTypes(List.of(NativeAd.NativeAdType.NATIVE))
            .setAdUnitId(adUnitId)
            .build();

    // Generate and send the signal request.
    MobileAds.generateSignal(
        signalRequest,
        new SignalGenerationCallback() {
          @Override
          public void onSuccess(@NonNull Signal signal) {
            Log.d(TAG, "Signal string: " + signal.getSignalString());
            // Fetch the ad response using your generated signal.
            String adResponseString = fetchSignalResponse(signal);
          }

          @Override
          public void onFailure(@NonNull SignalError error) {
            Log.d(TAG, "Error generating signal: " + error.getMessage());
          }
        });
    // [END signal_request_native]
  }

  public void loadBanner(Context applicationContext, String adUnitId) {
    // [START signal_request_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    AdSize size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(applicationContext, 320);

    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    BannerSignalRequest signalRequest =
        new BannerSignalRequest.Builder("SIGNAL_TYPE")
            .setAdUnitId(adUnitId)
            .setAdSize(size)
            .build();

    // Generate and send the signal request.
    MobileAds.generateSignal(
        signalRequest,
        new SignalGenerationCallback() {
          @Override
          public void onSuccess(@NonNull Signal signal) {
            Log.d(TAG, "Signal string: " + signal.getSignalString());
            // Fetch the ad response using your generated signal.
            String adResponseString = fetchSignalResponse(signal);
          }

          @Override
          public void onFailure(@NonNull SignalError error) {
            Log.d(TAG, "Error generating signal: " + error.getMessage());
          }
        });
    // [END signal_request_banner]
  }

  public void loadNativeWithOptions(String adUnitId) {
    // [START native_ad_options]
    VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();

    NativeSignalRequest signalRequest =
        new NativeSignalRequest.Builder("SIGNAL_TYPE")
            .setNativeAdTypes(List.of(NativeAd.NativeAdType.NATIVE))
            .setAdUnitId(adUnitId)
            .setVideoOptions(videoOptions)
            .build();
    MobileAds.generateSignal(
        signalRequest,
        new SignalGenerationCallback() {
          @Override
          public void onSuccess(@NonNull Signal signal) {
            Log.d(TAG, "Signal string: " + signal.getSignalString());
            // Fetch the ad response using your generated signal.
            String adResponseString = fetchSignalResponse(signal);
          }

          @Override
          public void onFailure(@NonNull SignalError error) {
            Log.d(TAG, "Error generating signal: " + error.getMessage());
          }
        });
    // [END native_ad_options]
  }

  // [START fetch_response]
  // This function emulates a request to your ad server.
  private String fetchSignalResponse(Signal signal) {
    return "adResponseString";
  }

  // [END fetch_response]

  public void renderNative(Activity activity, String adResponseString) {
    // [START render_native]
    NativeAdView nativeAdView = new NativeAdView(activity);
    NativeAdLoader.loadFromAdResponse(
        adResponseString,
        new NativeAdLoaderCallback() {
          @Override
          public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native ad rendered.");
            // TODO : Set native ad assets.
            nativeAdView.registerNativeAd(nativeAd, /* mediaView= */ null);
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.d(TAG, "Error rendering native ad: " + adError.getMessage());
          }
        });
    // [END render_native]
  }

  public void renderBanner(Activity activity, String adResponseString) {
    // [START render_banner]
    AdView adView = new AdView(activity);
    adView.loadFromAdResponse(
        adResponseString,
        new AdLoadCallback<BannerAd>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd ad) {
            Log.d(TAG, "Banner ad rendered.");
            adView.registerBannerAd(ad, activity);
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.d(TAG, "Error rendering banner ad: " + adError.getMessage());
          }
        });
    // [END render_banner]
  }

  private static final String TAG = "AdMobSCARSnippets";
}
