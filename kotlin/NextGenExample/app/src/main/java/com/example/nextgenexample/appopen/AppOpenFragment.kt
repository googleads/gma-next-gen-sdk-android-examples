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
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.databinding.FragmentAppOpenBinding

/** A fragment that demonstrates how to configure app open ads. */
class AppOpenFragment : AdFragment<FragmentAppOpenBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAppOpenBinding
    get() = FragmentAppOpenBinding::inflate

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    super.onCreateView(inflater, container, savedInstanceState)

    val context = requireContext()

    binding.showAppOpenAdSwitch.isChecked =
      // Set the switch state based on the user's saved preference.
      AppOpenAdManager.isAppOpenAdEnabled(context)

    binding.showAppOpenAdSwitch.setOnCheckedChangeListener { _, isChecked ->
      // Save the user's preference when the switch is toggled.
      AppOpenAdManager.setAppOpenAdEnabled(context, isChecked)
    }

    return binding.root
  }
}
