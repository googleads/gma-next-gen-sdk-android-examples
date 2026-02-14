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

package com.example.nextgenexample.banner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.FragmentComposeBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import kotlinx.coroutines.launch

/** A [AdFragment] subclass that loads a composable banner ad. */
class ComposeBannerFragment : AdFragment<FragmentComposeBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComposeBinding
    get() = FragmentComposeBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeView.apply {
      // Dispose of the Composition when the view is detached from the window.
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent { BannerAdView() }
    }
  }

  @Composable
  fun BannerAdView(modifier: Modifier = Modifier) {

    // [START banner_screen]
    // Initialize required variables.
    val context = LocalContext.current
    var bannerAdState by remember { mutableStateOf<BannerAd?>(null) }

    // The AdView is placed at the bottom of the screen.
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
      bannerAdState?.let { bannerAd ->
        Box(modifier = Modifier.fillMaxWidth()) {
          // Display the ad within an AndroidView.
          AndroidView(
            modifier = modifier.wrapContentSize(),
            factory = { bannerAd.getView(requireActivity()) },
          )
        }
      }
    }
    // [END banner_screen]

    // [START load_ad]
    // Request an large anchored adaptive banner with a width of 360.
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360)

    // Load the ad when the screen is active.
    val coroutineScope = rememberCoroutineScope()
    val isPreviewMode = LocalInspectionMode.current
    LaunchedEffect(context) {
      bannerAdState?.destroy()
      if (!isPreviewMode) {
        coroutineScope.launch {
          when (val result = BannerAd.load(BannerAdRequest.Builder(AD_UNIT_ID, adSize).build())) {
            is AdLoadResult.Success -> {
              bannerAdState = result.ad
            }
            is AdLoadResult.Failure -> {
              showToast("Banner failed to load.")
              Log.w(Constant.TAG, "Banner ad failed to load: $result.error")
            }
          }
        }
      }
    }
    // [END load_ad]

    // [START dispose_ad]
    // Destroy the ad when the screen is disposed.
    DisposableEffect(Unit) { onDispose { bannerAdState?.destroy() } }
    // [END dispose_ad]
  }

  private companion object {
    // Sample anchored adaptive banner ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
  }
}
