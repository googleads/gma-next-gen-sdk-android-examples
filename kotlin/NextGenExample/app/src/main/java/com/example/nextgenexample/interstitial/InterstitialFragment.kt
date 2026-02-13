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

package com.example.nextgenexample.interstitial

import android.icu.text.MessageFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentInterstitialBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader

/** An [AdFragment] subclass that preloads an interstitial ad. */
class InterstitialFragment : AdFragment<FragmentInterstitialBinding>() {

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentInterstitialBinding
    get() = FragmentInterstitialBinding::inflate

  private var countdownTimer: CountDownTimer? = null
  private var gamePaused = false
  private var gameOver = false
  private var timerMilliseconds = 0L

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Start preloading.
    startPreloadingWithCallback()
    startGame()

    // Initialize the UI.
    binding.playAgainButton.setOnClickListener {
      startGame()
      updateUI()
    }
    updateUI()
  }

  private fun startPreloadingWithCallback() {
    val preloadCallback =
      // [Important] Do not call preload start or poll ad within the callback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.i(
            Constant.TAG,
            ("Interstitial preload ad failed to load with error: " + adError.message),
          )
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(Constant.TAG, "Interstitial preload ad is not available.")
          showToast("Interstitial ads exhausted.")
          updateUI()
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(Constant.TAG, "Interstitial preload ad is available.")
          showToast("Interstitial ad preloaded.")
          updateUI()
        }
      }
    val adRequest: AdRequest = AdRequest.Builder(AD_UNIT_ID).build()
    val preloadConfig = PreloadConfiguration(adRequest)
    InterstitialAdPreloader.start(AD_UNIT_ID, preloadConfig, preloadCallback)
  }

  private fun pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = InterstitialAdPreloader.pollAd(AD_UNIT_ID)

    // Interact with the ad object as needed.
    ad?.apply {
      Log.d(Constant.TAG, "Interstitial ad response info: ${this.getResponseInfo()}")
      this.adEventCallback =
        object : InterstitialAdEventCallback {
          override fun onAdImpression() {
            Log.d(Constant.TAG, "Interstitial ad recorded an impression.")
          }

          override fun onAdPaid(value: AdValue) {
            Log.d(
              Constant.TAG,
              "Interstitial ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}",
            )
          }
        }

      // Show the ad.
      show(requireActivity())
    }
  }

  fun updateUI() {
    runOnUiThread {
      if (InterstitialAdPreloader.isAdAvailable(AD_UNIT_ID)) {
        binding.txtStatus.text = getString(R.string.available)
      } else {
        binding.txtStatus.text = getString(R.string.exhausted)
      }
    }
  }

  /** Set the game back to "start". */
  private fun startGame() {
    binding.playAgainButton.visibility = View.INVISIBLE
    createTimer(GAME_LENGTH_MILLISECONDS)
    gamePaused = false
    gameOver = false
  }

  /** Cancel the timer. */
  private fun pauseGame() {
    if (gameOver || gamePaused) {
      return
    }
    countdownTimer?.cancel()
    gamePaused = true
  }

  /** Create timer with the active time remaining. */
  private fun resumeGame() {
    if (gameOver || !gamePaused) {
      return
    }
    createTimer(timerMilliseconds)
    gamePaused = false
  }

  /** Resume the game if it's in progress. */
  override fun onResume() {
    super.onResume()
    resumeGame()
  }

  /** Pause the game if it's in progress. */
  override fun onPause() {
    super.onPause()
    pauseGame()
  }

  private fun createTimer(milliseconds: Long) {
    countdownTimer?.cancel()

    countdownTimer =
      object : CountDownTimer(milliseconds, COUNTDOWN_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
          timerMilliseconds = millisUntilFinished
          // Display countdown start from 5.0 seconds.
          val seconds =
            millisUntilFinished / 1000 +
              if ((millisUntilFinished % 1000).toInt() == 0) {
                0
              } else {
                1
              }
          binding.timer.text =
            MessageFormat.format(getString(R.string.seconds_left), mapOf("count" to seconds))
        }

        override fun onFinish() {
          gameOver = true
          binding.timer.text = getString(R.string.you_lose)
          binding.playAgainButton.visibility = View.VISIBLE

          // Show the alert dialog which triggers the interstitial ad.
          val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
          builder
            .setTitle(R.string.game_over)
            .setMessage("You lasted ${GAME_LENGTH_MILLISECONDS / 1000} seconds")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> pollAndShowAd() }

          val dialog: AlertDialog = builder.create()
          dialog.show()
        }
      }

    countdownTimer?.start()
  }

  private companion object {
    const val COUNTDOWN_INTERVAL = 50L
    const val GAME_LENGTH_MILLISECONDS = 5000L

    // Sample interstitial ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
  }
}
