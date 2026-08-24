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
import android.os.Bundle;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;

/** Java code snippets for modifying the initialization config. */
final class InitializationConfigSnippets {

  private void forceUseCronet(Context context) {
    // [START force_use_cronet]
    Bundle extras = new Bundle();
    extras.putBoolean("force_use_cronet", true);

    InitializationConfig config = new InitializationConfig.Builder().setExtras(extras).build();

    MobileAds.initialize(context, config, initializationStatus -> {});
    // [END force_use_cronet]
  }
}
