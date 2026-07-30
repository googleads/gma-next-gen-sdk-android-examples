/*
 * Copyright 2025 Google LLC
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

package com.example.nextgenexample.preloading;

import static com.example.nextgenexample.Constant.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.R;
import com.example.nextgenexample.databinding.FragmentPreloadBinding;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;
import androidx.annotation.NonNull;

/** A [Fragment] subclass that preloads app open ads. */
public class AppOpenPreloadFragment extends AdFragment<FragmentPreloadBinding> {

  // Replace this test ad unit ID with your own ad unit ID.
  public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";

  @Override
  protected BindingInflater<FragmentPreloadBinding> getBindingInflater() {
    return FragmentPreloadBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Start preloading.
    startPreloadingWithCallback();

    // Initialize the UI.
    binding.txtTitle.setText(getText(R.string.app_open));
    binding.btnShow.setOnClickListener(
        sender -> {
          pollAndShowAd();
          updateUI();
        });
    updateUI();
  }

  private void startPreloadingWithCallback() {
    PreloadCallback preloadCallback =
        // [Important] Do not call preload start of poll ad within the callback.
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(
              @NonNull String preloadId, @NonNull LoadAdError loadAdError) {
            Log.i(
                TAG, "App open preload ad failed to load with error: " + loadAdError.getMessage());
            // [Optional] Get the error response info for additional details.
            // ResponseInfo responseInfo =   loadAdError.getResponseInfo();
          }

          @Override
          public void onAdsExhausted(@NonNull String preloadId) {
            Log.i(TAG, "No preloaded app open ads available.");
            updateUI();
          }

          @Override
          public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
            Log.i(TAG, "App open ad was preloaded.");
            updateUI();
          }
        };
    AdRequest adRequest = new AdRequest.Builder(AD_UNIT_ID).build();
    PreloadConfiguration preload = new PreloadConfiguration(adRequest);
    AppOpenAdPreloader.start(AD_UNIT_ID, preload, preloadCallback);
  }

  private void pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    AppOpenAd ad = AppOpenAdPreloader.pollAd(AD_UNIT_ID);
    if (ad == null) {
      Log.i(TAG, "No preloaded app open ads available.");
      return;
    }

    // Interact with the ad object as needed.
    Log.d(TAG, "App open ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new AppOpenAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "App open ad recorded an impression.");
          }

          @Override
          public void onAdPaid(@androidx.annotation.NonNull AdValue value) {
            Log.d(
                TAG,
                String.format(
                    "App open ad onPaidEvent: %d %s",
                    value.getValueMicros(), value.getCurrencyCode()));
          }
        });

    ad.show(requireActivity());
    updateUI();
  }

  private boolean isAdAvailable() {
    return AppOpenAdPreloader.isAdAvailable(AD_UNIT_ID);
  }

  public synchronized void updateUI() {
    runOnUiThread(
        () -> {
          if (isAdAvailable()) {
            binding.txtStatus.setText(getString(R.string.available));
            binding.btnShow.setEnabled(true);
          } else {
            binding.txtStatus.setText(getString(R.string.exhausted));
            binding.btnShow.setEnabled(false);
          }
        });
  }
}
