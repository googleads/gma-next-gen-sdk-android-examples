// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.snippets

import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.common.AdFormat
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.AdapterInitializationConfig
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Kotlin code snippets for the developer guide. */
@OptIn(ExperimentalApi::class)
private class ConfigurableInitSnippets {

  // Create a config that sets allowed ad unit ids and an initialization timeout.
  private fun createAdUnitAdapterInitConfig(): AdapterInitializationConfig {

    // [START create_ad_unit_adapter_init_config]
    val adapterConfig =
      AdapterInitializationConfig.Builder()
        .setAllowedAdUnitIds(setOf(AD_UNIT_ID))
        .setInitializationTimeoutMs(5000)
        .build()

    // [END create_ad_unit_adapter_init_config]

    return adapterConfig
  }

  // Create a config that sets allowed ad formats ids and an initialization timeout.
  private fun createAdFormatAdapterInitConfig(): AdapterInitializationConfig {

    // [START create_ad_format_adapter_init_config]
    val adapterConfig =
      AdapterInitializationConfig.Builder()
        .setAllowedAdFormats(setOf(AdFormat.APP_OPEN_AD))
        .setInitializationTimeoutMs(5000)
        .build()

    // [END create_ad_format_adapter_init_config]

    return adapterConfig
  }

  // Create a config that setAllowedAdapterClasses and an initialization timeout.
  private fun createIncludeSpecificAdapterInitConfig(): AdapterInitializationConfig {

    // [START create_include_adapter_init_config]
    val adapterConfig =
      AdapterInitializationConfig.Builder()
        .setAllowedAdapterClasses(setOf(ADAPTER_JAVA_CLASS_NAME))
        .setInitializationTimeoutMs(5000)
        .build()

    // [END create_include_adapter_init_config]

    return adapterConfig
  }

  // Create a config that setExcludedAdapterClasses and an initialization timeout.
  private fun createSpecificExclusionAdapterInitConfig(): AdapterInitializationConfig {

    // [START create_exclude_adapter_init_config]
    val adapterConfig =
      AdapterInitializationConfig.Builder()
        .setExcludedAdapterClasses(setOf(ADAPTER_JAVA_CLASS_NAME))
        .setInitializationTimeoutMs(5000)
        .build()

    // [END create_exclude_adapter_init_config]

    return adapterConfig
  }

  private fun loadAppOpenAdForAdapterInitConfig(
    context: Context,
    adapterInitializationConfig: AdapterInitializationConfig,
    adUnitId: String,
    appId: String,
  ) {

    // [START make_adapter_init_config_request]
    val initConfig =
      InitializationConfig.Builder(appId)
        .setAdapterInitializationConfig(adapterInitializationConfig)
        .build()

    // Initialize the GMA Next-Gen SDK with a config containing your adapter initialization
    // settings.
    val backgroundScope = CoroutineScope(Dispatchers.IO)
    backgroundScope.launch {
      MobileAds.initialize(context = context, initConfig) {

        // Make your ad request after adapter initialization completes.
        AppOpenAd.load(
          AdRequest.Builder(adUnitId = adUnitId)
            // Set this to guarantee that the ad request
            // doesn't wait for any other uninitialized adapters.
            .skipUninitializedAdapters()
            .build(),
          adLoadCallback =
            object : AdLoadCallback<AppOpenAd> {

              override fun onAdLoaded(ad: AppOpenAd) {
                // TODO: Your ad logic here
                // ....

                // Execute non-time-sensitive tasks after ad load completes.
                performPostLaunchSetup()
              }

              override fun onAdFailedToLoad(adError: LoadAdError) {
                // TODO: Handle load error.
                // ....

                // Ensure non-time-sensitive post-launch tasks execute even if ad load fails.
                performPostLaunchSetup()
              }
            },
        )
      }
    }

    // [END make_adapter_init_config_request]
  }

  private fun performPostLaunchSetup() {

    // [START empty_init_config]

    // Provide initializeAdapters with an empty configuration to initialize
    // any adapters not initialized with configurable initialization.
    MobileAds.initializeAdapters(AdapterInitializationConfig.Builder().build())

    // [END empty_init_config]
  }

  private companion object {
    const val AD_UNIT_ID = "" // TODO: replace this
    const val ADAPTER_JAVA_CLASS_NAME = "com.google.ads.mediation.example.ExampleMediationAdapter"
  }
}
