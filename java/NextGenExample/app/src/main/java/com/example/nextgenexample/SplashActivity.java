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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.example.nextgenexample.appopen.AppOpenAdManager;
import com.example.nextgenexample.databinding.ActivitySplashBinding;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Splash Activity that inflates splash activity xml. */
public class SplashActivity extends AppCompatActivity {

  // Sample AdMob App ID.
  private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
  private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
  private final AtomicBoolean gatherConsentFinished = new AtomicBoolean(false);
  private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
  private ActivitySplashBinding binding;
  private TextView counterTextView;
  private long secondsRemaining = 0;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivitySplashBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    googleMobileAdsConsentManager =
        GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
    googleMobileAdsConsentManager.gatherConsent(
        this,
        consentError -> {
          if (consentError != null) {
            // Consent not obtained in current session.
            Log.w(
                Constant.TAG,
                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
          }

          gatherConsentFinished.set(true);

          if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
          }
          if (secondsRemaining <= 0) {
            startMainActivity();
          }
        });

    // This sample attempts to load ads using consent obtained in the previous session.
    if (googleMobileAdsConsentManager.canRequestAds()) {
      initializeMobileAdsSdk();
    }

    // Create a timer so the SplashActivity will be displayed for a fixed amount of time.
    createTimer();
  }

  /** Create the countdown timer, which counts down to zero and shows the app open ad. */
  private void createTimer() {
    SharedPreferences sharedPrefs =
        PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);

    // Do not show the app open ad by default.
    boolean shouldShowAppOpenAd =
        AppOpenAdManager.getInstance().isAppOpenAdOnColdStartEnabled(this);

    // For better usability of this sample, shorten the splash screen timer if an app open ad will
    // not be shown.
    long timerDurationInSeconds = shouldShowAppOpenAd ? 5L : 2L;

    counterTextView = binding.timer;

    CountDownTimer countDownTimer =
        new CountDownTimer(timerDurationInSeconds * 1000, 50) {
          @Override
          public void onTick(long millisUntilFinished) {
            secondsRemaining = millisUntilFinished / 1000 + 1;
            counterTextView.setText(
                getString(R.string.splash_activity_loading_text, secondsRemaining));
          }

          @Override
          public void onFinish() {
            secondsRemaining = 0;
            counterTextView.setText(getString(R.string.splash_activity_done_loading_text));

            if (shouldShowAppOpenAd) {
              AppOpenAdManager.getInstance()
                  .showAdIfAvailable(SplashActivity.this, () -> {
                    // Check if the consent form is currently on screen before moving to the
                    // main activity.
                    if (gatherConsentFinished.get()) {
                      startMainActivity();
                    }
                  });
            } else {
              // Check if the consent form is currently on screen before moving to the
              // main activity.
              if (gatherConsentFinished.get()) {
                startMainActivity();
              }
            }
          }
        };

    countDownTimer.start();
  }

  /** Start the MainActivity. */
  void startMainActivity() {
    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
    startActivity(intent);
  }

  private void initializeMobileAdsSdk() {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
      return;
    }

    new Thread(
            () -> {
              // Initialize the Google Mobile Ads SDK on a background thread.
              MobileAds.initialize(
                  this,
                  new InitializationConfig.Builder(APP_ID).build(),
                  initializationStatus -> {
                    // Adapter initialization is complete.
                  });

              // Set your test devices.
              MobileAds.setRequestConfiguration(
                  new RequestConfiguration.Builder()
                      .setTestDeviceIds(List.of(Constant.TEST_DEVICE_HASHED_ID))
                      .build());

              if (googleMobileAdsConsentManager.canRequestAds()) {
                // Load an app open ad when Mobile Ads SDK initialization is complete.
                AppOpenAdManager.getInstance().startPreloading();
              }
            })
        .start();
  }
}
