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

package com.example.nextgenexample.appopen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.databinding.FragmentAppOpenBinding;

/** An [AdFragment] subclass that loads an app open ad. */
public class AppOpenFragment extends AdFragment<FragmentAppOpenBinding> {
  // Default constructor required for fragment instantiation.
  public AppOpenFragment() {}

  // Sample app open ad unit ID.
  public static String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";
  public static String KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS = "show_app_open_ad_on_all_starts";

  @Override
  protected BindingInflater<FragmentAppOpenBinding> getBindingInflater() {
    return FragmentAppOpenBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getActivity() != null) {
      SharedPreferences sharedPreferences =
          PreferenceManager.getDefaultSharedPreferences(getActivity());

      binding.showAppOpenAdAllStartsSwitch.setChecked(
          sharedPreferences.getBoolean(KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, false));

      binding.showAppOpenAdAllStartsSwitch.setOnCheckedChangeListener(
          (buttonView, isChecked) ->
              sharedPreferences
                  .edit()
                  .putBoolean(KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, isChecked)
                  .apply());
    }
  }
}
