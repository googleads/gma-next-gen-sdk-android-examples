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
public class SqueezeBackFragment extends AdFragment<FragmentSqueezebackBinding> {

  public SqueezeBackFragment() {}

  @Override
  protected BindingInflater<FragmentSqueezebackBinding> getBindingInflater() {
    return FragmentSqueezebackBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Register callback listeners for ad shown / hidden events.
    SqueezeBackAdManager.setOnAdShownListener(
        () ->
            runOnUiThread(
                () -> {
                  binding.showAdButton.setEnabled(false);
                  binding.hideAdButton.setEnabled(true);
                }));

    SqueezeBackAdManager.setOnAdHiddenListener(
        () ->
            runOnUiThread(
                () -> {
                  showToast("Squeezeback ad hidden.");
                  binding.showAdButton.setEnabled(SqueezeBackAdManager.isAdAvailable());
                  binding.hideAdButton.setEnabled(false);
                }));

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener(
        v -> {
          binding.loadAdButton.setEnabled(false);
          binding.showAdButton.setEnabled(false);
          binding.hideAdButton.setEnabled(false);

          SqueezeBackAdManager.loadAd(
              ad ->
                  runOnUiThread(
                      () -> {
                        showToast("Squeezeback ad loaded.");
                        binding.loadAdButton.setEnabled(true);
                        binding.showAdButton.setEnabled(true);
                        binding.hideAdButton.setEnabled(false);
                      }),
              adError ->
                  runOnUiThread(
                      () -> {
                        showToast("Squeezeback ad failed to load: " + adError.getMessage());
                        binding.loadAdButton.setEnabled(true);
                        binding.showAdButton.setEnabled(false);
                        binding.hideAdButton.setEnabled(false);
                      }));
        });

    binding.showAdButton.setOnClickListener(
        v -> {
          if (!SqueezeBackAdManager.isAdAvailable()) {
            showToast("No ad loaded to show.");
            return;
          }

          if (getActivity() != null) {
            SqueezeBackAdManager.showAd(getActivity());
          }
        });

    binding.hideAdButton.setOnClickListener(v -> SqueezeBackAdManager.hideAd());
  }

  @Override
  public void onDestroyView() {
    SqueezeBackAdManager.setOnAdShownListener(null);
    SqueezeBackAdManager.setOnAdHiddenListener(null);
    SqueezeBackAdManager.destroyAd();
    super.onDestroyView();
  }
}
