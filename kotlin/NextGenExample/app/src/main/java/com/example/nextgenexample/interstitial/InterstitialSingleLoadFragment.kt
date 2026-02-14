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

package com.example.nextgenexample.interstitial

import android.icu.text.MessageFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentInterstitialBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

/** A simple [Fragment] subclass that loads an interstitial ad. */
class InterstitialSingleLoadFragment : AdFragment<FragmentInterstitialBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentInterstitialBinding
    get() = FragmentInterstitialBinding::inflate

  private var interstitialAd: InterstitialAd? = null
  private var countdownTimer: CountDownTimer? = null
  private var gamePaused = false
  private var gameOver = false
  private var timerMilliseconds = 0L

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    super.onCreateView(inflater, container, savedInstanceState)

    binding.txtStatus.visibility = View.GONE

    loadAd()
    startGame()

    // Create the "Play again" button.
    binding.playAgainButton.visibility = View.INVISIBLE
    binding.playAgainButton.setOnClickListener {
      loadAd()
      startGame()
    }

    return binding.root
  }

  /** Loads a new ad if one isn't already loaded. */
  private fun loadAd() {
    if (interstitialAd != null) {
      Log.d(Constant.TAG, "Interstitial ad already loaded.")
      return
    }

    InterstitialAd.load(
      AdRequest.Builder(AD_UNIT_ID).build(),
      object : AdLoadCallback<InterstitialAd> {
        override fun onAdLoaded(ad: InterstitialAd) {
          interstitialAd = ad
          showToast("Interstitial loaded.")
          Log.d(Constant.TAG, "Interstitial ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          interstitialAd = null
          showToast("Interstitial ad failed to load.")
          Log.w(Constant.TAG, "Interstitial ad failed to load: $adError")
        }
      },
    )
  }

  /** Show the ad if it's ready. Otherwise attempt to load an ad and restart the game. */
  private fun showInterstitial() {
    if (interstitialAd == null) {
      loadAd()
      startGame()
      Log.d(Constant.TAG, "The interstitial ad wasn't ready yet.")
      return
    }

    // Listen for ad events.
    interstitialAd?.adEventCallback =
      object : InterstitialAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          showToast("Interstitial ad shown.")
          Log.d(Constant.TAG, "Interstitial ad showed.")
        }

        override fun onAdDismissedFullScreenContent() {
          interstitialAd = null
          showToast("Interstitial ad dismissed.")
          Log.d(Constant.TAG, "Interstitial ad dismissed.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          interstitialAd = null
          showToast("Interstitial ad failed to show.")
          Log.w(Constant.TAG, "Interstitial ad failed to show: $fullScreenContentError")
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Interstitial ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Interstitial ad recorded a click.")
        }
      }
    // Show the ad.
    interstitialAd?.show(requireActivity())
  }

  /**
   * Create the game timer, which counts down to the end of the level and shows the "Play again"
   * button.
   */
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
            .setPositiveButton("OK") { _, _ -> showInterstitial() }

          val dialog: AlertDialog = builder.create()
          dialog.show()
        }
      }

    countdownTimer?.start()
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

  private companion object {
    const val COUNTDOWN_INTERVAL = 50L
    const val GAME_LENGTH_MILLISECONDS = 5000L

    // Sample interstitial ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
  }
}
