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

import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.NativeAdType;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.SwipeGestureDirection;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest;
import java.util.List;

/** Java code snippets for the developer guide. */
final class NativeAdOptionsSnippets {

  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";

  private void setCustomClickGesture() {
    // [START set_custom_click_gesture]
    NativeAdRequest adRequest =
        new NativeAdRequest.Builder(AD_UNIT_ID, List.of(NativeAdType.NATIVE))
            .enableCustomClickGestureDirection(
                SwipeGestureDirection.RIGHT, /* customClickGestureAllowTaps= */ true)
            .build();
    // [END set_custom_click_gesture]
  }
}
