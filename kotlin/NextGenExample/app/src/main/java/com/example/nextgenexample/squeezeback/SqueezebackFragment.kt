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

package com.example.nextgenexample.squeezeback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.databinding.FragmentSqueezebackBinding
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi

/** A [Fragment] subclass that demonstrates squeezeback ads. */
@OptIn(ExperimentalApi::class)
class SqueezebackFragment : AdFragment<FragmentSqueezebackBinding>() {

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSqueezebackBinding
    get() = FragmentSqueezebackBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    updateButtonStates()

    // Register callback listeners for ad shown / hidden events.
    SqueezebackAdManager.onAdShownListener = { runOnUiThread { updateButtonStates() } }

    SqueezebackAdManager.onAdHiddenListener = {
      runOnUiThread {
        showToast("Squeezeback ad hidden.")
        updateButtonStates()
      }
    }

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener { loadAd() }

    binding.showAdButton.setOnClickListener { showAd() }

    binding.hideAdButton.setOnClickListener { SqueezebackAdManager.hideAd() }
  }

  private fun loadAd() {
    if (SqueezebackAdManager.isAdShowing) {
      return
    }
    binding.loadAdButton.isEnabled = false
    binding.showAdButton.isEnabled = false
    binding.hideAdButton.isEnabled = false

    SqueezebackAdManager.loadAd(
      onAdLoaded = {
        runOnUiThread {
          showToast("Squeezeback ad loaded.")
          updateButtonStates()
        }
      },
      onAdFailedToLoad = { adError ->
        runOnUiThread {
          showToast("Squeezeback ad failed to load: ${adError.message}")
          updateButtonStates()
        }
      },
    )
  }

  private fun showAd() {
    if (!SqueezebackAdManager.isAdAvailable()) {
      showToast("No ad loaded to show.")
      return
    }

    val currentActivity = activity
    if (currentActivity != null) {
      SqueezebackAdManager.showAd(currentActivity)
    }
  }

  private fun updateButtonStates() {
    if (this.view == null) return
    binding.loadAdButton.isEnabled = !SqueezebackAdManager.isAdShowing
    binding.showAdButton.isEnabled = SqueezebackAdManager.isAdAvailable()
    binding.hideAdButton.isEnabled = SqueezebackAdManager.isAdShowing
  }

  override fun onDestroyView() {
    SqueezebackAdManager.onAdShownListener = null
    SqueezebackAdManager.onAdHiddenListener = null
    SqueezebackAdManager.destroyAd()
    super.onDestroyView()
  }
}
