/*
 * Copyright 2025 Google LLC
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

package com.example.nextgenexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.nextgenexample.databinding.FragmentMenuBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A [Fragment] subclass as the default destination in the navigation. */
public class MenuFragment extends Fragment {

  // A record for an example, including resource identifiers for its title and navigation action.
  private record ExampleData(@StringRes int titleResId, @IdRes Integer navActionId) {}

  private FragmentMenuBinding binding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMenuBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();

    // Create a list of ad examples and their corresponding navigation actions.
    List<ExampleData> examples =
        new ArrayList<ExampleData>() {
          {
            add(new ExampleData(R.string.app_open, R.id.action_MenuFragment_to_AppOpenFragment));
            add(new ExampleData(R.string.banner, R.id.action_MenuFragment_to_BannerFragment));
            add(
                new ExampleData(
                    R.string.collapsible_banner,
                    R.id.action_MenuFragment_to_CollapsibleBannerFragment));
            add(
                new ExampleData(
                    R.string.inline_banner, R.id.action_MenuFragment_to_InlineBannerFragment));
            add(
                new ExampleData(
                    R.string.interstitial_single_load,
                    R.id.action_MenuFragment_to_InterstitialSingleLoadFragment));
            add(
                new ExampleData(
                    R.string.swipeable_interstitial,
                    R.id.action_MenuFragment_to_SwipeableInterstitialFragment));
            add(
                new ExampleData(
                    R.string.rewarded_single_load,
                    R.id.action_MenuFragment_to_RewardedSingleLoadFragment));
            add(new ExampleData(R.string.native_ad, R.id.action_MenuFragment_to_NativeFragment));
            add(
                new ExampleData(
                    R.string.preloading, R.id.action_MenuFragment_to_PreloadingFragment));
            add(
                new ExampleData(
                    R.string.full_screen_native,
                    R.id.action_MenuFragment_to_FullScreenNativeControllerFragment));
            add(
                new ExampleData(
                    R.string.custom_native, R.id.action_MenuFragment_to_CustomNativeFragment));
            add(
                new ExampleData(
                    R.string.rewarded_interstitial_single_load,
                    R.id.action_MenuFragment_to_RewardedInterstitialSingleLoadFragment));
            add(new ExampleData(R.string.icon_ad, R.id.action_MenuFragment_to_IconFragment));
            add(
                new ExampleData(
                    R.string.webview_api_for_ads,
                    R.id.action_MenuFragment_to_InAppBrowserFragment));
            add(
                new ExampleData(
                    R.string.ad_manager_multiple_ad_sizes,
                    R.id.action_MenuFragment_to_AdManagerMultipleAdSizesFragment));
            add(
                new ExampleData(
                    R.string.ad_manager_category_exclusion,
                    R.id.action_MenuFragment_to_AdManagerCategoryExclusionFragment));
            add(
                new ExampleData(
                    R.string.ad_manager_fluid_size,
                    R.id.action_MenuFragment_to_AdManagerFluidSizeFragment));
            add(
                new ExampleData(
                    R.string.ad_manager_custom_targeting,
                    R.id.action_MenuFragment_to_AdManagerCustomTargetingFragment));
          }
        };

    // Set an ArrayAdapter with just the example titles.
    List<String> examplesTitles =
        examples.stream()
            .map(example -> getResources().getString(example.titleResId))
            .collect(Collectors.toList());
    binding.listView.setAdapter(
        new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, examplesTitles));

    binding.listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          NavController navController = Navigation.findNavController(rootView);
          navController.navigate((int) examples.get(position).navActionId);
        });

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null; // Release binding reference.
  }
}
