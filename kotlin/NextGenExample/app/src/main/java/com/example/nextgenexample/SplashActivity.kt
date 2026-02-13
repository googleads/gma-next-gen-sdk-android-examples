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

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.nextgenexample.appopen.AppOpenAdManager
import com.example.nextgenexample.appopen.AppOpenFragment
import com.example.nextgenexample.databinding.ActivitySplashBinding
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.ump.FormError
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Splash Activity that inflates splash activity xml. */
class SplashActivity : AppCompatActivity() {

  private val isMobileAdsInitializeCalled = AtomicBoolean(false)
  private val gatherConsentFinished = AtomicBoolean(false)
  private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
  private lateinit var binding: ActivitySplashBinding
  private lateinit var counterTextView: TextView
  private var secondsRemaining = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivitySplashBinding.inflate(layoutInflater)
    setContentView(binding.root)

    googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
    googleMobileAdsConsentManager.gatherConsent(this) { consentError: FormError? ->
      if (consentError != null) {
        // Consent not obtained in current session.
        Log.d(Constant.TAG, "${consentError.errorCode}: ${consentError.message}")
      }

      gatherConsentFinished.set(true)

      if (googleMobileAdsConsentManager.canRequestAds) {
        initializeMobileAdsSdk()
      }
      if (secondsRemaining <= 0) {
        startMainActivity()
      }
    }

    // This sample attempts to load ads using consent obtained in the previous session.
    if (googleMobileAdsConsentManager.canRequestAds) {
      initializeMobileAdsSdk()
    }

    // Create a timer so the SplashActivity will be displayed for a fixed amount of time.
    createTimer()
  }

  /** Create the countdown timer, which counts down to zero and shows the app open ad. */
  private fun createTimer() {
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
    // Do not show the app open ad by default.
    val shouldShowAppOpenAd =
      sharedPrefs.getBoolean(AppOpenFragment.KEY_SHOW_APP_OPEN_AD_ON_ALL_STARTS, false)
    // For better usability of this sample, shorten the splash screen timer if an app open ad will
    // be shown.
    val timerDurationInSeconds = if (shouldShowAppOpenAd) 5L else 2L

    counterTextView = binding.timer
    val countDownTimer: CountDownTimer =
      object : CountDownTimer(timerDurationInSeconds * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
          secondsRemaining = millisUntilFinished / 1000 + 1
          counterTextView.text = getString(R.string.splash_activity_loading_text, secondsRemaining)
        }

        override fun onFinish() {
          secondsRemaining = 0
          counterTextView.text = getString(R.string.splash_activity_done_loading_text)

          if (shouldShowAppOpenAd) {
            AppOpenAdManager.showAdIfAvailable(this@SplashActivity) {
              // Check if the consent form is currently on screen before moving to the
              // main activity.
              if (gatherConsentFinished.get()) {
                startMainActivity()
              }
            }
          } else {
            // Check if the consent form is currently on screen before moving to the
            // main activity.
            if (gatherConsentFinished.get()) {
              startMainActivity()
            }
          }
        }
      }
    countDownTimer.start()
  }

  /** Start the MainActivity. */
  fun startMainActivity() {
    val intent = Intent(this@SplashActivity, MainActivity::class.java)
    startActivity(intent)
  }

  private fun initializeMobileAdsSdk() {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
      return
    }

    val backgroundScope = CoroutineScope(Dispatchers.IO)
    backgroundScope.launch {
      // Initialize the Google Mobile Ads SDK on a background thread.
      MobileAds.initialize(
        this@SplashActivity,
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        InitializationConfig.Builder(APP_ID).build(),
      ) {
        // Adapter initialization is complete.
      }

      // Set your test devices.
      MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
          .setTestDeviceIds(listOf(Constant.TEST_DEVICE_HASHED_ID))
          .build()
      )

      if (googleMobileAdsConsentManager.canRequestAds) {
        // Load an app open ad when Mobile Ads SDK initialization is complete.
        AppOpenAdManager.loadAd(this@SplashActivity)
      }
    }
  }

  private companion object {
    // Sample AdMob App ID.
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
  }
}
