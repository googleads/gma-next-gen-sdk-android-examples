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
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import com.example.nextgenexample.appopen.AppOpenAdManager;
import com.example.nextgenexample.appopen.AppOpenFragment;

/** Application class that initializes, loads and show ads when activities change states. */
public class MyApplication extends Application
    implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

  private Activity currentActivity;

  @Override
  public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(this);
    ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (currentActivity == null) {
      return;
    }

    // Show app open ad if the switch to enable app open ads on all starts is on, or if returning
    // to the AppOpenFragment.
    boolean isAppOpenFragment = false;
    if (currentActivity instanceof FragmentActivity fragmentActivity) {
      FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

      Fragment navHostFragment =
          fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
      if (navHostFragment instanceof NavHostFragment) {
        FragmentManager childFragmentManager = navHostFragment.getChildFragmentManager();
        Fragment primaryNavigationFragment = childFragmentManager.getPrimaryNavigationFragment();
        isAppOpenFragment = primaryNavigationFragment instanceof AppOpenFragment;
      }
    }

    if (PreferenceManager.getDefaultSharedPreferences(currentActivity)
            .getBoolean(AppOpenFragment.KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, false)
        || isAppOpenFragment) {
      AppOpenAdManager.getInstance().showAdIfAvailable(currentActivity, null);
    }
  }

  /** ActivityLifecycleCallback methods. */
  @Override
  public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
    currentActivity = activity;
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {}

  @Override
  public void onActivityPaused(@NonNull Activity activity) {}

  @Override
  public void onActivityStopped(@NonNull Activity activity) {}

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {}
}
