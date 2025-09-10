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

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerSignalRequest;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeSignalRequest;
import com.google.android.libraries.ads.mobile.sdk.signal.Signal;
import com.google.android.libraries.ads.mobile.sdk.signal.SignalError;
import com.google.android.libraries.ads.mobile.sdk.signal.SignalGenerationCallback;

/** Java code snippets for the developer guide. */
public class AdMobSCARSnippets {

  public void loadNative(String adUnitId) {
    // [START signal_request_native]
    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    NativeSignalRequest signalRequest =
        new NativeSignalRequest.Builder("SIGNAL_TYPE")
            .setRequestAgent("REQUEST_AGENT")
            .setadUnitId(adUnitId)
            .setNativeAdTypes(Arrays.asList(NativeAdType.NATIVE))
            .build();

    // Generate and send the signal request.
    MobileAds.generateSignal(
        signalRequest,
        new SignalGenerationCallback() {
          @Override
          public void onSuccess(@NonNull Signal signal) {
            Log.d(TAG, "QueryInfo string: " + signal.getSignalString());
            // TODO: Fetch the ad response using your generated query info.
          }

          @Override
          public void onFailure(@NonNull SignalError error) {
            System.out.println("Error generating signal: " + error.getMessage());
          }
        });
    // [END signal_request_native]
  }

  public void loadBanner(Context context, String adUnitId) {
    // [START signal_request_banner]
    // Get the adaptive banner size.
    // Refer to the AdSize class for available ad sizes.
    AdSize size = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, 320);

    // Create a signal request for an ad.
    // Contact your account manager for your assigned signal type.
    BannerSignalRequest signalRequest =
        new BannerSignalRequest.Builder("SIGNAL_TYPE")
            .setRequestAgent("REQUEST_AGENT")
            .setadUnitId(adUnitId)
            .setNativeAdTypes(Arrays.asList(NativeAdType.BANNER))
            .setAdSize(size)
            .build();

    // Generate and send the signal request.
    MobileAds.generateSignal(
        signalRequest,
        new SignalGenerationCallback() {
          @Override
          public void onSuccess(@NonNull Signal signal) {
            Log.d(TAG, "QueryInfo string: " + signal.getSignalString());
            // TODO: Fetch the ad response using your generated query info.
          }

          @Override
          public void onFailure(@NonNull SignalError error) {
            System.out.println("Error generating signal: " + error.getMessage());
          }
        });
    // [END signal_request_banner]
  }

  private static final String TAG = "AdmobSCARSnippets";
}
