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

package com.example.nextgenexample.banner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.Constant;
import com.example.nextgenexample.databinding.FragmentCategoryExclusionBinding;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

/** An [AdFragment] subclass that loads ads configured to use category exclusions. */
public class AdManagerCategoryExclusionFragment
    extends AdFragment<FragmentCategoryExclusionBinding> {
  // Default constructor required for fragment instantiation.
  public AdManagerCategoryExclusionFragment() {}

  // Sample ad unit ID.
  private static final String AD_UNIT_ID = "/21775744923/example/api-demo/category-exclusion";

  private static final String DOGS_EXCLUSION_KEY = "apidemo_exclude_dogs";
  private static final String CATS_EXCLUSION_KEY = "apidemo_exclude_cats";

  @Override
  protected BindingInflater<FragmentCategoryExclusionBinding> getBindingInflater() {
    return FragmentCategoryExclusionBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loadAd(null, binding.noneExcludedBanner);
    loadAd(DOGS_EXCLUSION_KEY, binding.dogsExcludedBanner);
    loadAd(CATS_EXCLUSION_KEY, binding.catsExcludedBanner);
  }

  @Override
  public void onDestroyView() {
    binding.noneExcludedBanner.destroy();
    binding.dogsExcludedBanner.destroy();
    binding.catsExcludedBanner.destroy();
    super.onDestroyView();
  }

  private void loadAd(@Nullable String exclusionKey, AdView adView) {
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360);

    BannerAdRequest.Builder requestBuilder = new BannerAdRequest.Builder(AD_UNIT_ID, adSize);

    if (exclusionKey != null) {
      requestBuilder.addCategoryExclusion(exclusionKey);
    }

    final String logPrefix;
    if (exclusionKey != null) {
      logPrefix = String.format("Banner ad with category exclusion %s", exclusionKey);
    } else {
      logPrefix = "Banner ad";
    }

    adView.loadAd(
        requestBuilder.build(),
        new AdLoadCallback<>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd ad) {

            // Interact with the loaded ad object as needed.
            ad.setAdEventCallback(
                new BannerAdEventCallback() {
                  @Override
                  public void onAdImpression() {
                    Log.d(Constant.TAG, logPrefix + " recorded an impression.");
                  }

                  @Override
                  public void onAdClicked() {
                    Log.d(Constant.TAG, logPrefix + " clicked.");
                  }
                });

            ad.setBannerAdRefreshCallback(
                new BannerAdRefreshCallback() {
                  @Override
                  public void onAdRefreshed() {
                    showToast(logPrefix + " refreshed.");
                    Log.d(Constant.TAG, logPrefix + " refreshed.");
                  }

                  @Override
                  public void onAdFailedToRefresh(@NonNull LoadAdError adError) {
                    showToast(
                        logPrefix + " failed to refresh with error code: " + adError.getCode());
                    Log.w(Constant.TAG, logPrefix + " failed to refresh: " + adError);
                  }
                });
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            showToast(logPrefix + " failed to load with error code: " + adError.getCode());
            Log.w(Constant.TAG, logPrefix + " failed to load: " + adError);
          }
        });
  }
}
