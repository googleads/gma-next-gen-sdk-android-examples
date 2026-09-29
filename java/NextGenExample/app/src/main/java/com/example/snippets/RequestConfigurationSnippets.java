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

import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.AgeRestrictedTreatment;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;

/** Java code snippets for {@link RequestConfiguration} in the NextGen developer guide. */
final class RequestConfigurationSnippets {

  private void setChildAgeTreatment() {
    // [START set_child_age_treatment]
    RequestConfiguration requestConfiguration =
        new RequestConfiguration.Builder()
            // Indicate that ad requests should have child age treatment.
            .setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD)
            .build();
    MobileAds.setRequestConfiguration(requestConfiguration);
    // [END set_child_age_treatment]
  }

  private void setTeenAgeTreatment() {
    // [START set_teen_age_treatment]
    RequestConfiguration requestConfiguration =
        new RequestConfiguration.Builder()
            // Indicate that ad requests should have teenage treatment.
            .setAgeRestrictedTreatment(AgeRestrictedTreatment.TEEN)
            .build();
    MobileAds.setRequestConfiguration(requestConfiguration);
    // [END set_teen_age_treatment]
  }

  private void setUnspecifiedAgeTreatment() {
    // [START set_unspecified_age_treatment]
    RequestConfiguration requestConfiguration =
        new RequestConfiguration.Builder()
            // Indicate that ad requests should have unspecified age treatment.
            .setAgeRestrictedTreatment(AgeRestrictedTreatment.UNSPECIFIED)
            .build();
    MobileAds.setRequestConfiguration(requestConfiguration);
    // [END set_unspecified_age_treatment]
  }

  private void setCoarseLocationCollectionEnabled() {
    // [START set_coarse_location_collection_enabled]
    RequestConfiguration requestConfiguration =
        new RequestConfiguration.Builder()
            // Disable automatic coarse location fetching by the SDK.
            .setCoarseLocationCollectionEnabled(false)
            .build();
    MobileAds.setRequestConfiguration(requestConfiguration);
    // [END set_coarse_location_collection_enabled]
  }
}
