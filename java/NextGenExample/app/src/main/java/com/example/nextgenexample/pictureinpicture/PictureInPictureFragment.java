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

package com.example.nextgenexample.pictureinpicture;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.databinding.FragmentPictureInPictureBinding;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdOptions;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPosition;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPresentationScope;
import java.util.Arrays;
import java.util.List;

/** A Fragment subclass that demonstrates Picture-in-Picture (PiP) ads. */
public class PictureInPictureFragment extends AdFragment<FragmentPictureInPictureBinding> {

  public PictureInPictureFragment() {}

  private record ScopeItem(String label, PictureInPictureAdPresentationScope scope) {
    @NonNull
    @Override
    public String toString() {
      return label;
    }
  }

  private record PositionItem(String label, PictureInPictureAdPosition position) {
    @NonNull
    @Override
    public String toString() {
      return label;
    }
  }

  private final List<ScopeItem> presentationScopes =
      Arrays.asList(
          new ScopeItem("Screen", PictureInPictureAdPresentationScope.SCREEN),
          new ScopeItem("Application", PictureInPictureAdPresentationScope.APPLICATION));

  private final List<PositionItem> positions =
      Arrays.asList(
          new PositionItem("Default", PictureInPictureAdPosition.DEFAULT),
          new PositionItem("Top Left", PictureInPictureAdPosition.TOP_LEFT),
          new PositionItem("Top Right", PictureInPictureAdPosition.TOP_RIGHT),
          new PositionItem("Bottom Left", PictureInPictureAdPosition.BOTTOM_LEFT),
          new PositionItem("Bottom Right", PictureInPictureAdPosition.BOTTOM_RIGHT));

  @Override
  protected BindingInflater<FragmentPictureInPictureBinding> getBindingInflater() {
    return FragmentPictureInPictureBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Setup presentation scope spinner.
    ArrayAdapter<ScopeItem> scopeAdapter =
        new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, presentationScopes);
    scopeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.scopeSpinner.setAdapter(scopeAdapter);

    // Setup initial position spinner.
    ArrayAdapter<PositionItem> positionAdapter =
        new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, positions);
    positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.positionSpinner.setAdapter(positionAdapter);

    // Register callback listeners for ad shown / hidden events.
    PictureInPictureAdManager.setOnAdShownListener(
        () ->
            runOnUiThread(
                () -> {
                  binding.showAdButton.setEnabled(false);
                  binding.hideAdButton.setEnabled(true);
                }));

    PictureInPictureAdManager.setOnAdHiddenListener(
        () ->
            runOnUiThread(
                () -> {
                  showToast("Picture-in-Picture ad hidden.");
                  binding.showAdButton.setEnabled(PictureInPictureAdManager.isAdAvailable());
                  binding.hideAdButton.setEnabled(false);
                }));

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener(
        v -> {
          binding.loadAdButton.setEnabled(false);
          binding.showAdButton.setEnabled(false);
          binding.hideAdButton.setEnabled(false);

          PictureInPictureAdManager.loadAd(
              ad ->
                  runOnUiThread(
                      () -> {
                        showToast("Picture-in-Picture ad loaded.");
                        binding.loadAdButton.setEnabled(true);
                        binding.showAdButton.setEnabled(true);
                        binding.hideAdButton.setEnabled(false);
                      }),
              adError ->
                  runOnUiThread(
                      () -> {
                        showToast("Picture-in-Picture ad failed to load: " + adError.getMessage());
                        binding.loadAdButton.setEnabled(true);
                        binding.showAdButton.setEnabled(false);
                        binding.hideAdButton.setEnabled(false);
                      }));
        });

    binding.showAdButton.setOnClickListener(
        v -> {
          if (!PictureInPictureAdManager.isAdAvailable()) {
            showToast("No ad loaded to show.");
            return;
          }

          PictureInPictureAdPresentationScope selectedScope =
              presentationScopes.get(binding.scopeSpinner.getSelectedItemPosition()).scope();
          PictureInPictureAdPosition selectedPosition =
              positions.get(binding.positionSpinner.getSelectedItemPosition()).position();

          PictureInPictureAdOptions options =
              new PictureInPictureAdOptions.Builder()
                  .setPresentationScope(selectedScope)
                  .setPosition(selectedPosition)
                  .build();

          if (getActivity() != null) {
            PictureInPictureAdManager.showAd(getActivity(), options);
          }
        });

    binding.hideAdButton.setOnClickListener(v -> PictureInPictureAdManager.hideAd());
  }

  @Override
  public void onDestroyView() {
    PictureInPictureAdManager.setOnAdShownListener(null);
    PictureInPictureAdManager.setOnAdHiddenListener(null);

    // Only destroy the ad on fragment removal if the scope is not APPLICATION.
    if (PictureInPictureAdManager.getPipAd() == null
        || PictureInPictureAdManager.getPipAd().getPresentationScope()
            != PictureInPictureAdPresentationScope.APPLICATION) {
      PictureInPictureAdManager.destroyAd();
    }

    super.onDestroyView();
  }
}
