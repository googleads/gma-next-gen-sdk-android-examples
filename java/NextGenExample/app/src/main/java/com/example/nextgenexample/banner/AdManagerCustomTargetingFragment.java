/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nextgenexample.banner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.Constant;
import com.example.nextgenexample.databinding.FragmentCustomTargetingBinding;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import java.util.ArrayList;
import java.util.Arrays;

/** A [Fragment] subclass that loads an ad configured to have custom targeting. */
public class AdManagerCustomTargetingFragment extends AdFragment<FragmentCustomTargetingBinding> {
  // Default constructor required for fragment instantiation.
  public AdManagerCustomTargetingFragment() {}

  // Sample ad unit ID.
  private static final String AD_UNIT_ID = "/21775744923/example/api-demo/custom-targeting";
  private static final String CUSTOM_TARGETING_KEY = "sportpref";
  private AdView adView;
  private Spinner sportPicker;
  private ArrayList<String> sports =
      new ArrayList<>(
          Arrays.asList(
              "Baseball",
              "Basketball",
              "Bobsled",
              "Football",
              "Ice Hockey",
              "Running",
              "Skiing",
              "Snowboarding",
              "Softball"));

  @Override
  protected BindingInflater<FragmentCustomTargetingBinding> getBindingInflater() {
    return FragmentCustomTargetingBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    adView = binding.adView;
    sportPicker = binding.sportPicker;

    sportPicker.setAdapter(
        new ArrayAdapter<>(
            requireView().getContext(), android.R.layout.simple_spinner_item, sports));

    binding.loadAdButton.setOnClickListener(
        v -> loadAd(AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360)));
  }

  private void loadAd(AdSize adSize) {
    String customTargetingValue = (String) sportPicker.getSelectedItem();
    // [START create_ad_request]
    // Create an ad request with selected custom targeting string.
    BannerAdRequest adRequest =
        new BannerAdRequest.Builder(AD_UNIT_ID, adSize)
            // Put the custom key-value pairs set the in Ad Manager UI to target specific
            // campaigns (line items).
            .putCustomTargeting(CUSTOM_TARGETING_KEY, customTargetingValue)
            .build();
    // [END create_ad_request]

    adView.loadAd(
        adRequest,
        new AdLoadCallback<>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd ad) {
            ad.setAdEventCallback(
                new BannerAdEventCallback() {
                  @Override
                  public void onAdImpression() {
                    Log.d(Constant.TAG, "Banner ad recorded an impression.");
                  }

                  @Override
                  public void onAdClicked() {
                    Log.d(Constant.TAG, "Banner ad recorded a click.");
                  }
                });
            ad.setBannerAdRefreshCallback(
                new BannerAdRefreshCallback() {
                  @Override
                  public void onAdRefreshed() {
                    showToast("Banner ad refreshed.");
                    Log.d(Constant.TAG, "Banner ad refreshed.");
                  }

                  @Override
                  public void onAdFailedToRefresh(@NonNull LoadAdError adError) {
                    showToast("Banner ad failed to refresh.");
                    Log.w(Constant.TAG, "Banner ad failed to refresh: " + adError);
                  }
                });
            showToast("Banner ad loaded.");
            Log.d(Constant.TAG, "Banner ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            showToast("Banner ad failed to load.");
            Log.w(Constant.TAG, "Banner ad failed to load: " + adError);
          }
        });
  }

  @Override
  public void onDestroyView() {
    if (adView != null) {
      adView.destroy();
    }
    super.onDestroyView();
  }
}
