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

package com.example.snippets;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.common.AdFormat;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.initialization.AdapterInitializationConfig;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import java.util.Set;

/** Java code snippets for the developer guide. */
@OptIn(markerClass = ExperimentalApi.class)
final class ConfigurableInitSnippets {

  private static final String AD_UNIT_ID = ""; // TODO: replace this
  private static final String ADAPTER_JAVA_CLASS_NAME =
      "com.google.ads.mediation.example.ExampleMediationAdapter";

  // Create a config that sets allowed ad unit ids and an initialization timeout.
  private AdapterInitializationConfig createAdUnitAdapterInitConfig() {

    // [START create_ad_unit_adapter_init_config]
    AdapterInitializationConfig adapterConfig =
        new AdapterInitializationConfig.Builder()
            .setAllowedAdUnitIds(Set.of(AD_UNIT_ID))
            .setInitializationTimeoutMs(5000)
            .build();
    // [END create_ad_unit_adapter_init_config]

    return adapterConfig;
  }

  // Create a config that sets allowed ad formats ids and an initialization timeout.
  private AdapterInitializationConfig createAdFormatAdapterInitConfig() {

    // [START create_ad_format_adapter_init_config]
    AdapterInitializationConfig adapterConfig =
        new AdapterInitializationConfig.Builder()
            .setAllowedAdFormats(Set.of(AdFormat.APP_OPEN_AD))
            .setInitializationTimeoutMs(5000)
            .build();
    // [END create_ad_format_adapter_init_config]

    return adapterConfig;
  }

  // Create a config that setAllowedAdapterClasses and an initialization timeout.
  private AdapterInitializationConfig createIncludeSpecificAdapterInitConfig() {

    // [START create_include_adapter_init_config]
    AdapterInitializationConfig adapterConfig =
        new AdapterInitializationConfig.Builder()
            .setAllowedAdapterClasses(Set.of(ADAPTER_JAVA_CLASS_NAME))
            .setInitializationTimeoutMs(5000)
            .build();
    // [END create_include_adapter_init_config]

    return adapterConfig;
  }

  // Create a config that setExcludedAdapterClasses and an initialization timeout.
  private AdapterInitializationConfig createSpecificExclusionAdapterInitConfig() {

    // [START create_exclude_adapter_init_config]
    AdapterInitializationConfig adapterConfig =
        new AdapterInitializationConfig.Builder()
            .setExcludedAdapterClasses(Set.of(ADAPTER_JAVA_CLASS_NAME))
            .setInitializationTimeoutMs(5000)
            .build();
    // [END create_exclude_adapter_init_config]

    return adapterConfig;
  }

  private void loadAppOpenAdForAdapterInitConfig(
      Context context,
      AdapterInitializationConfig adapterInitializationConfig,
      String adUnitId,
      String appId) {

    // [START make_adapter_init_config_request]
    InitializationConfig initConfig =
        new InitializationConfig.Builder(appId)
            .setAdapterInitializationConfig(adapterInitializationConfig)
            .build();

    // Initialize the GMA Next-Gen SDK with a config containing your adapter initialization
    // settings.
    MobileAds.initialize(
        context,
        initConfig,
        initializationStatus -> {
          // Make your ad request after adapter initialization completes.
          AppOpenAd.load(
              new AdRequest.Builder(adUnitId)
                  // Set this to guarantee that the ad request
                  // doesn't wait for any other uninitialized adapters.
                  .skipUninitializedAdapters()
                  .build(),
              new AdLoadCallback<AppOpenAd>() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                  // TODO: Your ad logic here
                  // ....

                  // Handle necessary, but non time-sensitive tasks after the ad has loaded.
                  performPostLaunchSetup();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError error) {
                  // TODO: Handle load error
                }
              });
        });
    // [END make_adapter_init_config_request]
  }

  private void performPostLaunchSetup() {

    // [START empty_init_config]
    // Provide initializeAdapters with an empty configuration to initialize
    // any adapters not initialized with configurable initialization.
    MobileAds.initializeAdapters(new AdapterInitializationConfig.Builder().build());
    // [END empty_init_config]
  }
}
