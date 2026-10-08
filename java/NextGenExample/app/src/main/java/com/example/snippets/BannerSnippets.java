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
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import java.util.Arrays;
import java.util.List;

/** Java code snippets for the developer guide. */
final class BannerSnippets {

  private static final String AD_UNIT_ID = "/21775744923/example/api-demo/ad-sizes";
  private static final String TAG = "BannerSnippets";
  @Nullable private AdView adView;

  private void createCustomAdSize() {
    // [START create_custom_ad_size]
    AdSize customAdSize = new AdSize(250, 250);
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, customAdSize).build();
    // [END create_custom_ad_size]
  }

  private void createMultipleAdSizes() {
    // [START create_multiple_ad_sizes]
    List<AdSize> adSizes =
        Arrays.asList(new AdSize(120, 20), AdSize.BANNER, AdSize.MEDIUM_RECTANGLE);
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, adSizes).build();
    // [END create_multiple_ad_sizes]
  }

  // [START create_ad_view]
  private void createAdView(FrameLayout adViewContainer, Activity activity) {
    AdView adView = new AdView(activity);
    adViewContainer.addView(adView);
  }

  // [END create_ad_view]

  // [START load_ad]
  private void loadBannerAd(AdView adView, Activity activity) {
    // Get a BannerAdRequest for a 360 wide large anchored adaptive banner ad.
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, 360);
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, adSize).build();

    adView.loadAd(
        adRequest,
        new AdLoadCallback<BannerAd>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd bannerAd) {
            Log.d(TAG, "Banner ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.d(TAG, "Banner ad failed to load: " + adError);
          }
        });
  }

  // [END load_ad]

  private void loadBannerAdWithAdEvents(AdView adView, Activity activity) {
    // Get a BannerAdRequest for a 360 wide large anchored adaptive banner ad.
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, 360);
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, adSize).build();

    adView.loadAd(
        adRequest,
        new AdLoadCallback<BannerAd>() {
          // [START ad_events]
          @Override
          public void onAdLoaded(@NonNull BannerAd bannerAd) {
            bannerAd.setAdEventCallback(
                new BannerAdEventCallback() {
                  @Override
                  public void onAdImpression() {
                    // Banner ad recorded an impression.
                    Log.d(TAG, "Banner ad recorded an impression.");
                  }

                  @Override
                  public void onAdClicked() {
                    // Banner ad recorded a click.
                    Log.d(TAG, "Banner ad clicked.");
                  }

                  @Override
                  public void onAdShowedFullScreenContent() {
                    // Banner ad showed.
                    Log.d(TAG, "Banner ad showed full screen content.");
                  }

                  @Override
                  public void onAdDismissedFullScreenContent() {
                    // Banner ad dismissed.
                    Log.d(TAG, "Banner ad dismissed full screen content.");
                  }

                  @Override
                  public void onAdFailedToShowFullScreenContent(
                      @NonNull FullScreenContentError fullScreenContentError) {
                    // Banner ad failed to show.
                    Log.w(
                        TAG,
                        "Banner ad failed to show full screen content: " + fullScreenContentError);
                  }
                });
          }

          // [END ad_events]

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            // Banner ad failed to load.
            Log.d(TAG, "Banner ad failed to load: " + adError);
          }
        });
  }

  private void loadBannerAdWithRefreshCallback(@NonNull AdView adView, @NonNull Activity activity) {
    // Get a BannerAdRequest for a 360 wide large anchored adaptive banner ad.
    AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, 360);
    BannerAdRequest adRequest = new BannerAdRequest.Builder(AD_UNIT_ID, adSize).build();

    // [START ad_refresh]
    adView.loadAd(
        adRequest,
        new AdLoadCallback<BannerAd>() {
          @Override
          public void onAdLoaded(@NonNull BannerAd bannerAd) {
            bannerAd.setBannerAdRefreshCallback(
                new BannerAdRefreshCallback() {
                  @Override
                  public void onAdRefreshed() {
                    // Banner ad refreshed.
                    Log.d(TAG, "Banner ad refreshed.");
                  }

                  @Override
                  public void onAdFailedToRefresh(@NonNull LoadAdError adError) {
                    // Banner ad failed to refresh.
                    Log.w(TAG, "Banner ad failed to refresh: " + adError);
                  }
                });
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            // Banner ad failed to load.
            Log.w(TAG, "Banner ad failed to load: " + adError);
          }
        });
    // [END ad_refresh]
  }

  // [START destroy]
  private void destroyBanner() {
    // Remove banner from view hierarchy.
    if (adView != null) {
      if (adView.getParent() instanceof ViewGroup) {
        ((ViewGroup) adView.getParent()).removeView(adView);
      }

      // Destroy the banner ad resources.
      adView.destroy();
    }

    // Drop reference to the banner ad.
    adView = null;
  }
  // [END destroy]
}
