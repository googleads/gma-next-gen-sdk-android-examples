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

package com.example.nextgenexample.fullscreennative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.databinding.FragmentFullScreenNativeBinding
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FullScreenNativeFragment : AdFragment<FragmentFullScreenNativeBinding>() {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentFullScreenNativeBinding
    get() = FragmentFullScreenNativeBinding::inflate

  private val viewModel: NativeAdViewModel by lazy {
    ViewModelProvider(requireParentFragment())[NativeAdViewModel::class]
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    viewModel.nativeAd?.let { displayNativeAd(it) }
  }

  override fun onDestroy() {
    (requireActivity() as AppCompatActivity).supportActionBar?.show()
    super.onDestroy()
  }

  private fun displayNativeAd(nativeAd: NativeAd) {
    CoroutineScope(Dispatchers.Main.immediate).launch {
      // Set the view element with the native ad assets.
      binding.adBody.text = nativeAd.body
      binding.adCallToAction.text = nativeAd.callToAction
      binding.adHeadline.text = nativeAd.headline
      binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)

      // Hide views for assets that don't have data.
      binding.adAppIcon.visibility = if (nativeAd.icon == null) View.INVISIBLE else View.VISIBLE

      // Inform the Google Mobile Ads SDK that you have finished populating the native ad views
      // with this native ad.
      binding.fullScreenNativeAd.registerNativeAd(nativeAd, binding.adMedia)
    }
  }
}
