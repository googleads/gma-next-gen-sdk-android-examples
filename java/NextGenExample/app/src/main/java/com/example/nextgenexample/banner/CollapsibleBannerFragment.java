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
import com.example.nextgenexample.databinding.FragmentBannerBinding;
import com.google.android.gms.ads.mediation.admob.AdMobAdapter;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

/** An [AdFragment] subclass that loads a collapsible banner ad. */
public class CollapsibleBannerFragment extends AdFragment<FragmentBannerBinding> {
  // Default constructor required for fragment instantiation.
  public CollapsibleBannerFragment() {}

  // Sample collapsible banner ad unit ID.
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
  private AdView adView;

  @Override
  protected BindingInflater<FragmentBannerBinding> getBindingInflater() {
    return FragmentBannerBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    adView = binding.adView;

    // 360 is a placeholder value. Replace 360 with your banner container width.
    AdSize adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), 360);

    // Load an ad.
    loadCollapsibleBanner(adSize);
  }

  private void loadCollapsibleBanner(AdSize adSize) {
    // [START build_collapsible_banner_ad_request]
    // Create an extra parameter that aligns the bottom of the expanded ad to
    // the bottom of the bannerView.
    Bundle extras = new Bundle();
    extras.putString("collapsible", "bottom");

    // Create an ad request.
    BannerAdRequest adRequest =
        new BannerAdRequest.Builder(AD_UNIT_ID, adSize)
            .putAdSourceExtrasBundle(AdMobAdapter.class, extras)
            .build();
    // [END build_collapsible_banner_ad_request]

    adView.loadAd(
        adRequest,
        new AdLoadCallback<>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd ad) {
            ad.setAdEventCallback(
                new BannerAdEventCallback() {
                  @Override
                  public void onAdImpression() {
                    Log.d(Constant.TAG, "Collapsible banner ad recorded an impression.");
                  }

                  @Override
                  public void onAdClicked() {
                    Log.d(Constant.TAG, "Collapsible banner ad clicked.");
                  }
                });

            ad.setBannerAdRefreshCallback(
                new BannerAdRefreshCallback() {
                  @Override
                  public void onAdRefreshed() {
                    showToast("Collapsible banner ad refreshed.");
                    Log.d(Constant.TAG, "Collapsible banner ad refreshed.");
                  }

                  @Override
                  public void onAdFailedToRefresh(@NonNull LoadAdError adError) {
                    showToast(
                        "Collapsible banner ad failed to refresh with error code: "
                            + adError.getCode());
                    Log.w(Constant.TAG, "Collapsible banner ad failed to refresh: " + adError);
                  }
                });
            showToast("Collapsible banner ad loaded.");
            Log.d(Constant.TAG, "Collapsible banner ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            showToast("Collapsible banner ad failed to load with error code: " + adError.getCode());
            Log.w(Constant.TAG, "Collapsible banner ad failed to load: " + adError);
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
