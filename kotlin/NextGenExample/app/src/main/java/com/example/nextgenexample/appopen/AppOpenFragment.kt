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

package com.example.nextgenexample.appopen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.databinding.FragmentAppOpenBinding

/** A simple [Fragment] subclass that loads an app open ad. */
class AppOpenFragment : AdFragment<FragmentAppOpenBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAppOpenBinding
    get() = FragmentAppOpenBinding::inflate

  // SharedPreferences instance.
  private val sharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    super.onCreateView(inflater, container, savedInstanceState)

    binding.showAppOpenAdAllStartsSwitch.isChecked =
      sharedPreferences.getBoolean(KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, false)

    binding.showAppOpenAdAllStartsSwitch.setOnCheckedChangeListener { _, isChecked ->
      sharedPreferences.edit().putBoolean(KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, isChecked).apply()
    }

    return binding.root
  }

  companion object {
    // Sample app open ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    // Constant for SharedPreferences.
    const val KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS = "show_app_open_ad_on_all_starts"
  }
}
