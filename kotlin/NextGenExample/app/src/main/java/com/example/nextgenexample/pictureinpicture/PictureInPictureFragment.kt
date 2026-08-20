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

package com.example.nextgenexample.pictureinpicture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.databinding.FragmentPictureInPictureBinding
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdOptions
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPosition
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPresentationScope

/** A [Fragment] subclass that demonstrates Picture-in-Picture (PiP) ads. */
@OptIn(ExperimentalApi::class)
class PictureInPictureFragment : AdFragment<FragmentPictureInPictureBinding>() {

  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentPictureInPictureBinding
    get() = FragmentPictureInPictureBinding::inflate

  private val presentationScopes =
    listOf(
      "Screen" to PictureInPictureAdPresentationScope.SCREEN,
      "Application" to PictureInPictureAdPresentationScope.APPLICATION,
    )

  private val positions =
    listOf(
      "Default" to PictureInPictureAdPosition.DEFAULT,
      "Top Left" to PictureInPictureAdPosition.TOP_LEFT,
      "Top Right" to PictureInPictureAdPosition.TOP_RIGHT,
      "Bottom Left" to PictureInPictureAdPosition.BOTTOM_LEFT,
      "Bottom Right" to PictureInPictureAdPosition.BOTTOM_RIGHT,
    )

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Setup presentation scope spinner.
    val scopeAdapter =
      ArrayAdapter(
          requireContext(),
          android.R.layout.simple_spinner_item,
          presentationScopes.map { it.first },
        )
        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    binding.scopeSpinner.adapter = scopeAdapter

    // Setup initial position spinner.
    val positionAdapter =
      ArrayAdapter(
          requireContext(),
          android.R.layout.simple_spinner_item,
          positions.map { it.first },
        )
        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    binding.positionSpinner.adapter = positionAdapter

    // Register callback listeners for ad shown / hidden events.
    PictureInPictureAdManager.onAdShownListener = {
      runOnUiThread {
        binding.showAdButton.isEnabled = false
        binding.hideAdButton.isEnabled = true
      }
    }

    PictureInPictureAdManager.onAdHiddenListener = {
      runOnUiThread {
        showToast("Picture-in-Picture ad hidden.")
        binding.showAdButton.isEnabled = PictureInPictureAdManager.isAdAvailable()
        binding.hideAdButton.isEnabled = false
      }
    }

    // Setup button listeners.
    binding.loadAdButton.setOnClickListener {
      binding.loadAdButton.isEnabled = false
      binding.showAdButton.isEnabled = false
      binding.hideAdButton.isEnabled = false

      PictureInPictureAdManager.loadAd(
        onAdLoaded = {
          runOnUiThread {
            showToast("Picture-in-Picture ad loaded.")
            binding.loadAdButton.isEnabled = true
            binding.showAdButton.isEnabled = true
            binding.hideAdButton.isEnabled = false
          }
        },
        onAdFailedToLoad = { adError ->
          runOnUiThread {
            showToast("Picture-in-Picture ad failed to load: ${adError.message}")
            binding.loadAdButton.isEnabled = true
            binding.showAdButton.isEnabled = false
            binding.hideAdButton.isEnabled = false
          }
        },
      )
    }

    binding.showAdButton.setOnClickListener {
      if (!PictureInPictureAdManager.isAdAvailable()) {
        showToast("No ad loaded to show.")
        return@setOnClickListener
      }

      val selectedScope = presentationScopes[binding.scopeSpinner.selectedItemPosition].second
      val selectedPosition = positions[binding.positionSpinner.selectedItemPosition].second

      val options =
        PictureInPictureAdOptions.Builder()
          .setPresentationScope(selectedScope)
          .setPosition(selectedPosition)
          .build()

      activity?.let {
        PictureInPictureAdManager.showAd(it, options)
      }
    }

    binding.hideAdButton.setOnClickListener {
      PictureInPictureAdManager.hideAd()
    }
  }

  override fun onDestroyView() {
    PictureInPictureAdManager.onAdShownListener = null
    PictureInPictureAdManager.onAdHiddenListener = null

    // Only destroy the ad on fragment removal if the scope is not APPLICATION.
    if (
      PictureInPictureAdManager.pipAd?.presentationScope !=
        PictureInPictureAdPresentationScope.APPLICATION
    ) {
      PictureInPictureAdManager.destroyAd()
    }

    super.onDestroyView()
  }
}
