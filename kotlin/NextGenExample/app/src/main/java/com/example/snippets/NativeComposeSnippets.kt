package com.example.snippets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.nextgenexample.native.NativeAdView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd

// [START compose_native_ad]
@Composable
fun DisplayNativeAdView(nativeAd: NativeAd) {
  // `NativeAdView` is a custom composable that wraps the SDK's `NativeAdView`.
  NativeAdView(nativeAd) {
    // Access assets from the ad object
    nativeAd.headline?.let { Text(text = it) }
    nativeAd.body?.let { Text(text = it) }
  }
}
// [END compose_native_ad]
