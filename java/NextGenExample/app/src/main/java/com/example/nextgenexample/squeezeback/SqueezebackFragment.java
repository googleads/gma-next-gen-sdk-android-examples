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

package com.example.nextgenexample.squeezeback;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.databinding.FragmentSqueezebackBinding;

/** A Fragment subclass that demonstrates squeezeback ads. */
public class SqueezebackFragment extends AdFragment<FragmentSqueezebackBinding> {

  public SqueezebackFragment() {}

  @Override
  protected BindingInflater<FragmentSqueezebackBinding> getBindingInflater() {
    return FragmentSqueezebackBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.loadAdButton.setEnabled(!SqueezebackAdManager.isAdShowing());
    binding.showAdButton.setEnabled(SqueezebackAdManager.isAdAvailable());
    binding.hideAdButton.setEnabled(SqueezebackAdManager.isAdShowing());

    // Register callback listeners for ad shown / hidden events.
    SqueezebackAdManager.setOnAdShownListener(() ->
        runOnUiThread(() -> {
          if (getView() == null || binding == null) {
            return;
          }
          binding.loadAdButton.setEnabled(false);
          binding.showAdButton.setEnabled(false);
          binding.hideAdButton.setEnabled(true);
        }));

    SqueezebackAdManager.setOnAdHiddenListener(() ->
        runOnUiThread(() -> {
          if (getView() == null || binding == null) {
            return;
          }
          showToast("Squeezeback ad hidden.");
          binding.loadAdButton.setEnabled(!SqueezebackAdManager.isAdShowing());
          binding.showAdButton.setEnabled(SqueezebackAdManager.isAdAvailable());
          binding.hideAdButton.setEnabled(false);
        }));

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener(v -> {
      if (SqueezebackAdManager.isAdShowing()) {
        return;
      }
      binding.loadAdButton.setEnabled(false);
      binding.showAdButton.setEnabled(false);
      binding.hideAdButton.setEnabled(false);

      SqueezebackAdManager.loadAd(
          ad ->
              runOnUiThread(() -> {
                if (getView() == null || binding == null) {
                  return;
                }
                showToast("Squeezeback ad loaded.");
                binding.loadAdButton.setEnabled(!SqueezebackAdManager.isAdShowing());
                binding.showAdButton.setEnabled(SqueezebackAdManager.isAdAvailable());
                binding.hideAdButton.setEnabled(SqueezebackAdManager.isAdShowing());
              }),
          adError ->
              runOnUiThread(() -> {
                if (getView() == null || binding == null) {
                  return;
                }
                showToast("Squeezeback ad failed to load: " + adError.getMessage());
                binding.loadAdButton.setEnabled(!SqueezebackAdManager.isAdShowing());
                binding.showAdButton.setEnabled(SqueezebackAdManager.isAdAvailable());
                binding.hideAdButton.setEnabled(SqueezebackAdManager.isAdShowing());
              }));
    });

    binding.showAdButton.setOnClickListener(v -> {
      if (!SqueezebackAdManager.isAdAvailable()) {
        showToast("No ad loaded to show.");
        return;
      }

      if (getActivity() != null) {
        SqueezebackAdManager.showAd(getActivity());
      }
    });

    binding.hideAdButton.setOnClickListener(v -> SqueezebackAdManager.hideAd());
  }

  @Override
  public void onDestroyView() {
    SqueezebackAdManager.setOnAdShownListener(null);
    SqueezebackAdManager.setOnAdHiddenListener(null);
    SqueezebackAdManager.destroyAd();
    super.onDestroyView();
  }
}
