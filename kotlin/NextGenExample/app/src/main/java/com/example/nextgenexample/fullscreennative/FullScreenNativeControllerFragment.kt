/*
 * Copyright 2024 Google LLC
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

package com.example.nextgenexample.fullscreennative

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentFullScreenNativeControllerBinding
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** A fragment class that manages the full screen native fragment. */
class FullScreenNativeControllerFragment : AdFragment<FragmentFullScreenNativeControllerBinding>() {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentFullScreenNativeControllerBinding
    get() = FragmentFullScreenNativeControllerBinding::inflate

  private val viewModel: NativeAdViewModel by lazy {
    ViewModelProvider(this)[NativeAdViewModel::class]
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.loadAdButton.setOnClickListener {
      binding.loadAdButton.isEnabled = false
      binding.showAdButton.isEnabled = false
      loadAd()
    }
    binding.showAdButton.setOnClickListener {
      binding.loadAdButton.isEnabled = true
      binding.showAdButton.isEnabled = false
      showAd()
    }
  }

  private fun loadAd() {
    val adRequest =
      NativeAdRequest.Builder(AD_UNIT_ID, listOf(NativeAd.NativeAdType.NATIVE)).build()
    // Define the callback to handle successful ad loading or failed ad loading.
    val adCallback =
      object : NativeAdLoaderCallback {
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
          Log.d(Constant.TAG, "Native ad loaded.")
          showToast("Native ad loaded.")
          setEventCallback(nativeAd)
          viewModel.nativeAd = nativeAd
          CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.showAdButton.isEnabled = true
          }
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.d(Constant.TAG, "Native ad failed to load: $adError")
          showToast("Ad failed to load.")
          CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.loadAdButton.isEnabled = true
          }
        }
      }
    // Load the native ad with our request and callback.
    NativeAdLoader.load(adRequest, adCallback)
  }

  private fun setEventCallback(nativeAd: NativeAd) {
    nativeAd.adEventCallback =
      object : NativeAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          Log.d(Constant.TAG, "Native ad showed full screen content.")
        }

        override fun onAdDismissedFullScreenContent() {
          Log.d(Constant.TAG, "Native ad dismissed full screen content.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          Log.d(
            Constant.TAG,
            "Native ad failed to show full screen content with error: $fullScreenContentError",
          )
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Native ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Native ad recorded a click.")
        }
      }
  }

  private fun showAd() {
    childFragmentManager.commit {
      add(R.id.full_screen_native_ad_controller, FullScreenNativeFragment())
      setReorderingAllowed(true)
      addToBackStack(null)
    }
  }

  companion object {
    // Sample native ad unit ID: ca-app-pub-3940256099942544/2247696110.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
  }
}

class NativeAdViewModel : ViewModel() {
  var nativeAd: NativeAd? = null
}
