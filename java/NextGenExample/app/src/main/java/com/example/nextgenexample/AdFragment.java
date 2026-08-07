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

package com.example.nextgenexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.snackbar.Snackbar;

/** A [Fragment] with a self-contained example of an ad's features. */
public abstract class AdFragment<T extends ViewBinding> extends Fragment {
  /** Interface for the ViewBinding inflation function. */
  public interface BindingInflater<T extends ViewBinding> {
    T inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent);
  }

  // Property to override.
  protected abstract BindingInflater<T> getBindingInflater();

  // This property is only valid between onCreateView and onDestroyView.
  protected T binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = getBindingInflater().inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  protected void showToast(@NonNull final String text) {
    if (getActivity() != null) {
      getActivity()
          .runOnUiThread(
              () -> {
                if (getView() != null) {
                  Snackbar.make(getView(), text, Snackbar.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                }
              });
    }
  }

  protected void runOnUiThread(Runnable action) {
    Activity activity = getActivity();
    if (activity == null) {
      return;
    }
    activity.runOnUiThread(action);
  }
}

// CI Kick
