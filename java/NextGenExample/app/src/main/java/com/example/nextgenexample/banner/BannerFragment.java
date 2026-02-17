/*
 * Copyright 2024 Google LLC
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

package com.example.nextgenexample.banner;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.Constant;
import com.example.nextgenexample.databinding.FragmentBannerBinding;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

/** An [AdFragment] subclass that loads a banner ad. */
public class BannerFragment extends AdFragment<FragmentBannerBinding> {
  // Default constructor required for fragment instantiation.
  public BannerFragment() {}

  // Sample banner ad unit ID.
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
  private AdView adView;

  @Override
  protected BindingInflater<FragmentBannerBinding> getBindingInflater() {
    return FragmentBannerBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Get the ad size based on the screen width.
    adView = binding.adView;

    // Get the ad size based on the screen width.
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), getAdWidth());

    // Load an ad.
    loadAd(adSize);
  }

  // Determine the screen width to use for the ad width.
  private int getAdWidth() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    return (int) (displayMetrics.widthPixels / displayMetrics.density);
  }

  private void loadAd(AdSize adSize) {
    adView.loadAd(
        new BannerAdRequest.Builder(AD_UNIT_ID, adSize).build(),
        new AdLoadCallback<BannerAd>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd bannerAd) {
            bannerAd.setAdEventCallback(
                new BannerAdEventCallback() {
                  @Override
                  public void onAdImpression() {
                    Log.d(Constant.TAG, "Banner ad recorded an impression.");
                  }

                  @Override
                  public void onAdClicked() {
                    Log.d(Constant.TAG, "Banner ad clicked.");
                  }
                });

            bannerAd.setBannerAdRefreshCallback(
                new BannerAdRefreshCallback() {
                  @Override
                  public void onAdRefreshed() {
                    showToast("Banner ad refreshed.");
                    Log.d(Constant.TAG, "Banner ad refreshed.");
                  }

                  @Override
                  public void onAdFailedToRefresh(@NonNull LoadAdError adError) {
                    showToast("Banner ad failed to refresh with error code: " + adError.getCode());
                    Log.w(Constant.TAG, "Banner ad failed to refresh: " + adError);
                  }
                });
            showToast("Banner ad loaded.");
            Log.d(Constant.TAG, "Banner ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            showToast("Banner ad failed to load with error code: " + adError.getCode());
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
