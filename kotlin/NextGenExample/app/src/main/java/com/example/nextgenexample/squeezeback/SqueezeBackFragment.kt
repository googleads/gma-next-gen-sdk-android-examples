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
class SqueezeBackFragment : AdFragment<FragmentSqueezebackBinding>() {

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSqueezebackBinding
    get() = FragmentSqueezebackBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Register callback listeners for ad shown / hidden events.
    SqueezeBackAdManager.onAdShownListener = {
      runOnUiThread {
        binding.showAdButton.isEnabled = false
        binding.hideAdButton.isEnabled = true
      }
    }

    SqueezeBackAdManager.onAdHiddenListener = {
      runOnUiThread {
        showToast("Squeezeback ad hidden.")
        binding.showAdButton.isEnabled = SqueezeBackAdManager.isAdAvailable()
        binding.hideAdButton.isEnabled = false
      }
    }

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener {
      binding.loadAdButton.isEnabled = false
      binding.showAdButton.isEnabled = false
      binding.hideAdButton.isEnabled = false

      SqueezeBackAdManager.loadAd(
        onAdLoaded = {
          runOnUiThread {
            showToast("Squeezeback ad loaded.")
            binding.loadAdButton.isEnabled = true
            binding.showAdButton.isEnabled = true
            binding.hideAdButton.isEnabled = false
          }
        },
        onAdFailedToLoad = { adError ->
          runOnUiThread {
            showToast("Squeezeback ad failed to load: ${adError.message}")
            binding.loadAdButton.isEnabled = true
            binding.showAdButton.isEnabled = false
            binding.hideAdButton.isEnabled = false
          }
        },
      )
    }

    binding.showAdButton.setOnClickListener {
      if (!SqueezeBackAdManager.isAdAvailable()) {
        showToast("No ad loaded to show.")
        return@setOnClickListener
      }

      val currentActivity = activity
      if (currentActivity != null) {
        SqueezeBackAdManager.showAd(currentActivity)
      }
    }

    binding.hideAdButton.setOnClickListener { SqueezeBackAdManager.hideAd() }
  }

  override fun onDestroyView() {
    SqueezeBackAdManager.onAdShownListener = null
    SqueezeBackAdManager.onAdHiddenListener = null
    SqueezeBackAdManager.destroyAd()
    super.onDestroyView()
  }
}
