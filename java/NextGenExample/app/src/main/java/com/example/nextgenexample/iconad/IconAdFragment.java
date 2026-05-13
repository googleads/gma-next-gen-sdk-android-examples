/*
 * Copyright 2026 Google LLC
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

package com.example.nextgenexample.iconad;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.Constant;
import com.example.nextgenexample.databinding.FragmentIconBinding;
import com.example.nextgenexample.databinding.IconAdBinding;
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAd;
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdPlacement;
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdRequest;
import com.google.android.libraries.ads.mobile.sdk.iconad.IconAdView;

/** An [AdFragment] subclass that loads an icon ad. */
public class IconAdFragment extends AdFragment<FragmentIconBinding> {
  // Default constructor required for fragment instantiation.
  public IconAdFragment() {}

  // Sample icon ad unit ID.
  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1476272466";

  private IconAd iconAd;

  @Override
  protected BindingInflater<FragmentIconBinding> getBindingInflater() {
    return com.example.nextgenexample.databinding.FragmentIconBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadIconAd();
  }

  @Override
  public void onDestroyView() {
      super.onDestroyView();

      // Always call destroy() on ads on removal.
      if (iconAd != null) {
          iconAd.destroy();
          iconAd = null;
      }
  }

  private void loadIconAd() {
    IconAdRequest request =
        new IconAdRequest.Builder(AD_UNIT_ID)
            // The "AdChoices" badge is rendered at the top right corner of the icon ad
            // if left unspecified.
            .setAdChoicesPlacement(AdChoicesPlacement.BOTTOM_RIGHT)
            // Indicate the icon ad placement is in a browser
            .setIconAdPlacement(IconAdPlacement.BROWSER)
            .build();

    IconAd.load(
        request,
        new AdLoadCallback<IconAd>() {
          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.w(Constant.TAG, "Icon ad failed to load :" + adError);
            showToast("Icon ad failed to load with error code: " + adError.getCode());
          }

          @Override
          public void onAdLoaded(@NonNull IconAd ad) {
            Log.d(Constant.TAG, "Icon ad loaded.");

            // Always call destroy() on ads on removal.
            if (iconAd != null) {
                iconAd.destroy();
            }
            iconAd = ad;
            setAdEventCallback(ad);
            displayIconAd(ad);
          }
        });
  }

  private void setAdEventCallback(IconAd iconAd) {
    iconAd.setAdEventCallback(
        new IconAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Icon ad showed full screen content.
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Icon ad dismissed full screen content.
          }

          @Override
          public void onAdFailedToShowFullScreenContent(
              @NonNull FullScreenContentError fullScreenContentError) {
            // Icon ad failed to show full screen content.
          }

          @Override
          public void onAdImpression() {
            // Icon ad recorded an impression.
          }

          @Override
          public void onAdClicked() {
            // Icon ad recorded a click.
          }

          @Override
          public void onAdPaid(@NonNull AdValue value) {
            // Icon ad estimated to have earned money.
          }
        });
  }

  private void displayIconAd(IconAd iconAd) {
    if (getActivity() == null) {
      return;
    }

    getActivity()
        .runOnUiThread(
            () -> {
              IconAdBinding iconAdViewBinding = IconAdBinding.inflate(getLayoutInflater());
              // Add the ad view to the active view hierarchy.
              binding.iconAdContainer.addView(iconAdViewBinding.getRoot());
              IconAdView iconAdView = iconAdViewBinding.getRoot();

              // Populate the view elements with their respective icon ad asset.
              iconAdView.setCallToActionView(iconAdViewBinding.adCallToAction);
              iconAdView.setHeadlineView(iconAdViewBinding.adHeadline);
              iconAdView.setIconView(iconAdViewBinding.adIcon);
              iconAdView.setStarRatingView(iconAdViewBinding.adStars);

              // Map each asset view property to the corresponding view in your view hierarchy.
              iconAdViewBinding.adCallToAction.setText(iconAd.getCallToAction());
              iconAdViewBinding.adHeadline.setText(iconAd.getHeadline());
              iconAdViewBinding.adIcon.setImageDrawable(iconAd.getIcon().getDrawable());

              if (iconAd.getStarRating() != null) {
                iconAdViewBinding.adStars.setRating(iconAd.getStarRating().floatValue());
              }

              // Register the icon ad with the view presenting it.
              iconAdView.registerIconAd(iconAd);
            });
  }

  private void createRequestWithCorrelator() {
    IconAdRequest request = new IconAdRequest.Builder(AD_UNIT_ID).setCorrelator("12345").build();
  }

  @OptIn(markerClass = ExperimentalApi.class)
  private void createRequestWithPersistence() {
    IconAdRequest request = new IconAdRequest.Builder(AD_UNIT_ID).setAdPersistenceEnabled().build();
  }

  private void loadMultipleIconAds() {
    IconAdRequest request = new IconAdRequest.Builder(AD_UNIT_ID).build();

    IconAd.load(
        request,
        /* maxNumberOfAds= */ 3,
        new AdLoadCallback<IconAd>() {
          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            Log.w(Constant.TAG, "Icon ad failed to load: " + adError);
          }

          @Override
          public void onAdLoaded(@NonNull IconAd ad) {
            Log.d(Constant.TAG, "Icon ad loaded");
          }
        });
  }
}
