/*
 * Copyright 2025 Google LLC
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentComposeBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/** An [AdFragment] subclass that loads a composable banner ad. */
class ComposeLazyBannerFragment : AdFragment<FragmentComposeBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComposeBinding
    get() = FragmentComposeBinding::inflate

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Initialize the Lazy List Composable.
    binding.composeView.apply {
      // Dispose of the Composition when the view is detached from the window.
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent { LazyBannerScreen() }
    }
  }

  @Composable
  fun LazyBannerScreen(modifier: Modifier = Modifier) {
    val isPreviewMode = LocalInspectionMode.current
    var isLoadingAds by remember { mutableStateOf(true) }
    var loadedAds by remember { mutableStateOf<List<BannerAd>>(emptyList()) }
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), 360)

    // Load the filler content.
    val fillerText: List<String> =
      context?.resources?.getStringArray(R.array.lazy_banner_filler_content)?.toList()
        ?: emptyList()

    // Load ads on launch of the composition.
    LaunchedEffect(Unit) {
      if (!isPreviewMode) {
        loadedAds = loadBannerAds(AD_COUNT, adSize)
      }
      isLoadingAds = false
    }

    // Display a loading indicator if ads are still being fetched.
    if (isLoadingAds) {
      Box(modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.size(100.dp).align(Alignment.Center))
      }
    } else {
      // Display a lazy list with loaded ads and filler content.
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(loadedAds) { bannerAd ->
          Column {
            Box(modifier = Modifier.fillMaxWidth()) {
              // Display the ad within an AndroidView.
              AndroidView(
                modifier = modifier.wrapContentSize(),
                factory = { bannerAd.getView(requireActivity()) },
              )
            }
            // Display the filler content.
            fillerText.forEach { content ->
              Box(
                modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(8.dp)
              ) {
                Text(
                  text = content,
                  modifier.padding(8.dp),
                  style = MaterialTheme.typography.bodyMedium,
                )
              }
            }
          }
        }
      }
    }

    // Clean up the BannerAds after use.
    DisposableEffect(Unit) { onDispose { loadedAds.forEach { adView -> adView.destroy() } } }
  }

  private suspend fun loadBannerAds(count: Int, adSize: AdSize): List<BannerAd> = supervisorScope {
    List(count) {
        async {
          val adRequest = BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()
          when (val result = BannerAd.load(adRequest)) {
            is AdLoadResult.Success -> {
              result.ad
            }
            is AdLoadResult.Failure -> {
              showToast("Banner failed to load.")
              Log.w(Constant.TAG, "Banner ad failed to load: $result.error")
              null
            }
          }
        }
      }
      .awaitAll()
      .filterNotNull()
  }

  private companion object {
    // Sample anchored adaptive banner ad unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    private const val AD_COUNT = 5
  }
}
