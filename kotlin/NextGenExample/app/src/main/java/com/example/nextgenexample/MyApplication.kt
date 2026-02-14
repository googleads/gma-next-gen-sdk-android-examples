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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.nextgenexample.appopen.AppOpenAdManager
import com.example.nextgenexample.appopen.AppOpenFragment

/** Application class that initializes, loads and show ads when activities change states. */
class MyApplication :
  Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

  private var currentActivity: Activity? = null

  override fun onCreate() {
    super<Application>.onCreate()
    registerActivityLifecycleCallbacks(this)

    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

  /**
   * DefaultLifecycleObserver method that shows the app open ad when the app moves to foreground.
   */
  override fun onStart(owner: LifecycleOwner) {
    currentActivity?.let { activity ->
      // Show app open ad if the switch to enable app open ads on all starts is on, or if returning
      // to the AppOpenFragment.

      val isAppOpenFragment =
        ((activity as? FragmentActivity)
            ?.supportFragmentManager
            ?.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment)
          ?.childFragmentManager
          ?.primaryNavigationFragment is AppOpenFragment

      if (
        PreferenceManager.getDefaultSharedPreferences(activity)
          .getBoolean(AppOpenFragment.KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, false) ||
          isAppOpenFragment
      ) {
        AppOpenAdManager.showAdIfAvailable(activity, null)
        return
      }
    }
  }

  /** ActivityLifecycleCallback methods. */
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {
    currentActivity = activity
  }

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}
}
