/*
 * Copyright 2026 Google LLC
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

package com.example.nextgenexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nextgenexample.databinding.FragmentMenuBinding

/** A simple [Fragment] subclass as the default destination in the navigation. */
class MenuFragment : Fragment() {

  private var _binding: FragmentMenuBinding? = null

  // This property is only valid between onCreateView and onDestroyView.
  private val binding
    get() = _binding!!

  // A record for an example, including resource identifiers for its title and navigation action.
  private data class ExampleData(@StringRes val titleResId: Int, @IdRes val navActionId: Int)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentMenuBinding.inflate(inflater, container, false)

    // Create a list of ad examples and their corresponding navigation actions.
    val examples =
      listOf(
        ExampleData(R.string.app_open, R.id.action_MenuFragment_to_AppOpenFragment),
        ExampleData(R.string.banner, R.id.action_MenuFragment_to_BannerFragment),
        ExampleData(
          R.string.collapsible_banner,
          R.id.action_MenuFragment_to_CollapsibleBannerFragment,
        ),
        ExampleData(R.string.inline_banner, R.id.action_MenuFragment_to_InlineBannerFragment),
        ExampleData(R.string.interstitial, R.id.action_MenuFragment_to_InterstitialFragment),
        ExampleData(R.string.native_ad, R.id.action_MenuFragment_to_NativeFragment),
        ExampleData(R.string.compose_native_ad, R.id.action_MenuFragment_to_NativeComposeFragment),
        ExampleData(R.string.preloading, R.id.action_MenuFragment_to_PreloadingFragment),
        ExampleData(
          R.string.full_screen_native,
          R.id.action_MenuFragment_to_FullScreenNativeControllerFragment,
        ),
        ExampleData(R.string.custom_native, R.id.action_MenuFragment_to_CustomNativeFragment),
        ExampleData(R.string.rewarded, R.id.action_MenuFragment_to_RewardedFragment),
        ExampleData(
          R.string.rewarded_interstitial,
          R.id.action_MenuFragment_to_RewardedInterstitialFragment,
        ),
        ExampleData(R.string.icon_ad, R.id.action_MenuFragment_to_IconAdFragment),
        ExampleData(R.string.webview_api_for_ads, R.id.action_MenuFragment_to_InAppBrowserFragment),
        ExampleData(R.string.compose_banner, R.id.action_MenuFragment_to_ComposeBannerFragment),
        ExampleData(
          R.string.compose_lazy_banner,
          R.id.action_MenuFragment_to_LazyComposeBannerFragment,
        ),
        ExampleData(
          R.string.ad_manager_multiple_ad_sizes,
          R.id.action_MenuFragment_to_AdManagerMultipleAdSizesFragment,
        ),
        ExampleData(
          R.string.ad_manager_custom_targeting,
          R.id.action_MenuFragment_to_AdManagerCustomTargetingFragment,
        ),
        ExampleData(
          R.string.ad_manager_category_exclusion,
          R.id.action_MenuFragment_to_AdManagerCategoryExclusionFragment,
        ),
        ExampleData(
          R.string.ad_manager_fluid_size,
          R.id.action_MenuFragment_to_AdManagerFluidSizeFragment,
        ),
        ExampleData(
          R.string.interstitial_single_load,
          R.id.action_MenuFragment_to_InterstitialSingleLoadFragment,
        ),
        ExampleData(
          R.string.swipeable_interstitial,
          R.id.action_MenuFragment_to_SwipeableInterstitialFragment,
        ),
        ExampleData(
          R.string.rewarded_single_load,
          R.id.action_MenuFragment_to_RewardedSingleLoadFragment,
        ),
        ExampleData(
          R.string.rewarded_interstitial_single_load,
          R.id.action_MenuFragment_to_RewardedInterstitialSingleLoadFragment,
        ),
      )

    // Set an ArrayAdapter with just the example titles.
    val examplesTitles = examples.map { getString(it.titleResId) }
    binding.listView.adapter =
      ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, examplesTitles)
    binding.listView.setOnItemClickListener { _, _, position, _ ->
      findNavController().navigate(examples[position].navActionId)
    }

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
