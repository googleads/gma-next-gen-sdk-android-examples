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

package com.example.nextgenexample.native

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.nextgenexample.Constant
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

/** A Fragment that hosts a Jetpack Compose screen to display a native ad. */
class NativeComposeFragment : Fragment() {

  /**
   * Sets up a ComposeView to host the Jetpack Compose UI, allowing Compose to be used within a
   * Fragment-based navigation structure.
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return ComposeView(requireContext()).apply {
      // `setContent` is the entry point for building a Compose UI in a ComposeView.
      setContent { NativeScreen() }
    }
  }

  /**
   * The main composable for the screen. The `@Composable` annotation marks a function as a
   * UI-building block.
   */
  @Composable
  fun NativeScreen() {
    // `remember` stores state across recompositions. `mutableStateOf` creates an observable
    // state that triggers UI updates when its value changes.
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var nativeAdIsDisposed by remember { mutableStateOf(false) }

    // State for ad loading options.
    var requestVideo by remember { mutableStateOf(false) }
    var startMuted by remember { mutableStateOf(true) }

    /** Loads a native ad based on the current configuration. */
    fun loadAd() {
      val adUnitId = if (requestVideo) VIDEO_AD_UNIT_ID else IMAGE_AD_UNIT_ID
      val videoOptions: VideoOptions = VideoOptions.Builder().setStartMuted(startMuted).build()
      val adRequest =
        NativeAdRequest.Builder(adUnitId, listOf(NativeAd.NativeAdType.NATIVE))
          .setVideoOptions(videoOptions)
          .build()
      val adCallback =
        object : NativeAdLoaderCallback {
          override fun onNativeAdLoaded(nativeAd: NativeAd) {
            Log.d(Constant.TAG, "Native ad loaded.")
            // Update the state with the loaded ad, triggering a recomposition to display it.
            if (!nativeAdIsDisposed) {
              nativeAdState = nativeAd
            } else {
              // If the view is already gone, destroy the ad to prevent memory leaks.
              nativeAd.destroy()
            }
          }

          override fun onAdFailedToLoad(adError: LoadAdError) {
            Log.e(Constant.TAG, "Native ad failed to load: $adError")
            requireActivity().runOnUiThread {
              Toast.makeText(requireContext(), "Ad failed to load.", Toast.LENGTH_SHORT).show()
            }
          }
        }
      NativeAdLoader.load(adRequest, adCallback)
    }

    // `DisposableEffect` cleans up resources when the composable leaves the screen.
    DisposableEffect(Unit) {
      onDispose {
        // This `onDispose` block runs on cleanup, destroying the ad to prevent memory leaks.
        nativeAdIsDisposed = true
        nativeAdState?.destroy()
        nativeAdState = null
      }
    }

    // `Column` arranges its children vertically. `Modifier` adds properties like size and padding.
    Column(modifier = Modifier.fillMaxWidth()) {
      // Renders the ad if it's loaded.
      nativeAdState?.let { DisplayNativeAdView(ad = it) }

      // Ad configuration controls.
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = requestVideo, onCheckedChange = { requestVideo = it })
        Text(text = "Request video")
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = startMuted, onCheckedChange = { startMuted = it })
        Text(text = "Start muted")
      }
      Button(onClick = { loadAd() }) { Text(text = "Refresh Ad") }
    }
  }

  /** Displays the native ad content. */
  @Composable
  fun DisplayNativeAdView(ad: NativeAd) {
    // `NativeAdView` is a custom composable (in NativeComposableUtility.kt) that wraps the
    // SDK's `NativeAdView` for use in Compose.
    NativeAdView(ad) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
          // Check if ad assets are available before displaying them.
          ad.icon?.let { icon ->
            NativeAdIconView(Modifier.padding(5.dp)) {
              // Convert the drawable to a bitmap for the `Image` composable.
              icon.drawable?.toBitmap()?.let { Image(bitmap = it.asImageBitmap(), "Icon") }
            }
          }
          Column {
            ad.headline?.let { NativeAdHeadlineView { Text(text = it) } }
            ad.starRating?.let { NativeAdStarRatingView { Text(text = "Rated $it") } }
          }
        }

        // The MediaView must be at least 120x120 dp.
        // To ensure the MediaView consistently meets the size requirements for video ads,
        // use the aspectRatio modifier from Compose. This will force the composable to adhere to
        // the video's aspect ratio while respecting the width constraint.
        val mediaModifier =
          if (ad.mediaContent.hasVideoContent) {
            val aspectRatio = ad.mediaContent.aspectRatio
            Modifier.fillMaxWidth()
              .let {
                // Enforce the aspect ratio if it's available, otherwise default to a 16:9 ratio
                // to ensure a valid size.
                if (aspectRatio > 0) it.aspectRatio(aspectRatio) else it.aspectRatio(16f / 9f)
              }
              .heightIn(min = 120.dp) // Keep heightIn as a safeguard
              .padding(vertical = 5.dp)
          } else {
            Modifier.fillMaxWidth().padding(vertical = 5.dp)
          }
        NativeAdMediaView(modifier = mediaModifier)

        ad.body?.let { NativeAdBodyView(modifier = Modifier.padding(5.dp)) { Text(text = it) } }

        Row(
          Modifier.align(Alignment.End).padding(5.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ad.price?.let { NativeAdPriceView(Modifier.padding(5.dp)) { Text(text = it) } }
          ad.store?.let { NativeAdStoreView(Modifier.padding(5.dp)) { Text(text = it) } }
          ad.callToAction?.let {
            NativeAdCallToActionView(Modifier.padding(5.dp)) { NativeAdButton(text = it) }
          }
        }
      }
    }
  }

  /** Defines constants for ad unit IDs. */
  private companion object {
    // Sample native image ad unit ID.
    const val IMAGE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    // Sample native video ad unit ID.
    const val VIDEO_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"
  }
}
