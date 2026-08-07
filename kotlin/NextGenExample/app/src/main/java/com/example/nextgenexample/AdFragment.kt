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

package com.example.nextgenexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class AdFragment<T : ViewBinding> : Fragment() {

  // Properties to override.
  abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T

  private var _binding: T? = null
  // This property is only valid between onCreateView and onDestroyView.
  val binding
    get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = bindingInflater(inflater, container, false)

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  fun showToast(text: String) {
    activity?.runOnUiThread {
      Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
  }

  fun runOnUiThread(action: Runnable?) {
    val activity = activity ?: return
    activity.runOnUiThread(action)
  }
}

// CI Kick
