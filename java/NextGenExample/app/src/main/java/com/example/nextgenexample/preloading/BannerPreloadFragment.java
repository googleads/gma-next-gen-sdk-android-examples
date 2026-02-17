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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.R;
import com.example.nextgenexample.databinding.FragmentPreloadBinding;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;

/** A [Fragment] subclass that preloads banner ads. */
public class BannerPreloadFragment extends AdFragment<FragmentPreloadBinding> {

  // Replace this test ad unit ID with your own ad unit ID.
  public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";

  BannerAd currentAd;

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
    binding.txtTitle.setText(getText(R.string.banner));
    binding.btnShow.setOnClickListener(
        sender -> {
          pollAndShowAd();
          updateUI();
        });
    updateUI();
  }

  @Override
  public void onDestroyView() {
    // Always call destroy() on ads on removal.
    destroyCurrentAd();
    super.onDestroyView();
  }

  private void destroyCurrentAd() {
    if (binding != null && binding.placeholder != null) {
      binding.placeholder.removeAllViews();
    }
    if (currentAd != null) {
      currentAd.destroy();
      currentAd = null;
    }
  }

  private void startPreloadingWithCallback() {
    PreloadCallback preloadCallback =
        // [Important] Do not call preload start of poll ad within the callback.
        new PreloadCallback() {
          @Override
          public void onAdFailedToPreload(
              @NonNull String preloadId, @NonNull LoadAdError loadAdError) {
            Log.i(TAG, "Banner preload ad failed to load with error: " + loadAdError.getMessage());
            // [Optional] Get the error response info for additional details.
            // ResponseInfo responseInfo =   loadAdError.getResponseInfo();
          }

          @Override
          public void onAdsExhausted(@NonNull String preloadId) {
            Log.i(TAG, "No preloaded banner ads available.");
            updateUI();
          }

          @Override
          public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
            Log.i(TAG, "Banner ad was preloaded.");
            updateUI();
          }
        };
    // Get the ad size based on the screen width.
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), getAdWidth());
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, adSize).build();
    PreloadConfiguration preload = new PreloadConfiguration(adRequest);
    BannerAdPreloader.start(AD_UNIT_ID, preload, preloadCallback);
  }

  private void pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    BannerAd ad = BannerAdPreloader.pollAd(AD_UNIT_ID);
    if (ad == null) {
      Log.i(TAG, "No preloaded banner ads available.");
      return;
    }

    // Interact with the ad object as needed.
    Log.d(TAG, "Banner ad response info: " + ad.getResponseInfo());
    ad.setAdEventCallback(
        new BannerAdEventCallback() {
          @Override
          public void onAdImpression() {
            Log.d(TAG, "Banner ad recorded an impression.");
          }

          @Override
          public void onAdPaid(@NonNull AdValue value) {
            Log.d(
                TAG,
                String.format(
                    "Banner ad onPaidEvent: %d %s",
                    value.getValueMicros(), value.getCurrencyCode()));
          }
        });

    // Destroy the previous banner.
    destroyCurrentAd();
    // Show the new banner.
    binding.placeholder.addView(ad.getView(requireActivity()));
    currentAd = ad;
    updateUI();
  }

  private boolean isAdAvailable() {
    return BannerAdPreloader.isAdAvailable(AD_UNIT_ID);
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

  // Determine the screen width to use for the ad width.
  private int getAdWidth() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int adWidthPixels = displayMetrics.widthPixels;

    // The Next Gen SDK uses DisplayMetrics to check screen width. Avoid using window manager bounds
    // on Android R+.
    // if (VERSION.SDK_INT >= VERSION_CODES.R && getActivity() != null) {
    //  WindowMetrics windowMetrics = getActivity().getWindowManager().getCurrentWindowMetrics();
    //  adWidthPixels = windowMetrics.getBounds().width();
    // }

    float density = displayMetrics.density;
    return (int) (adWidthPixels / density);
  }
}
