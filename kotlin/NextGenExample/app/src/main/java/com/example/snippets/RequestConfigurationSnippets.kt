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

import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AgeRestrictedTreatment
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration

/** Kotlin code snippets for [RequestConfiguration] in the NextGen developer guide. */
private class RequestConfigurationSnippets {

  private fun setChildAgeTreatment() {
    // [START set_child_age_treatment]
    val requestConfiguration =
      RequestConfiguration.Builder()
        // Indicate that ad requests should have child age treatment.
        .setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD)
        .build()
    MobileAds.setRequestConfiguration(requestConfiguration)
    // [END set_child_age_treatment]
  }

  private fun setTeenAgeTreatment() {
    // [START set_teen_age_treatment]
    val requestConfiguration =
      RequestConfiguration.Builder()
        // Indicate that ad requests should have teenage treatment.
        .setAgeRestrictedTreatment(AgeRestrictedTreatment.TEEN)
        .build()
    MobileAds.setRequestConfiguration(requestConfiguration)
    // [END set_teen_age_treatment]
  }

  private fun setUnspecifiedAgeTreatment() {
    // [START set_unspecified_age_treatment]
    val requestConfiguration =
      RequestConfiguration.Builder()
        // Indicate that ad requests should have unspecified age treatment.
        .setAgeRestrictedTreatment(AgeRestrictedTreatment.UNSPECIFIED)
        .build()
    MobileAds.setRequestConfiguration(requestConfiguration)
    // [END set_unspecified_age_treatment]
  }
}
