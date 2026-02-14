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

package com.example.nextgenexample.rewarded

import android.icu.text.MessageFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentRewardedBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdPreloader

/** An [AdFragment] subclass that preloads a rewarded ad. */
class RewardedFragment : AdFragment<FragmentRewardedBinding>() {

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRewardedBinding
    get() = FragmentRewardedBinding::inflate

  private var rewardedAd: RewardedAd? = null
  private var countdownTimer: CountDownTimer? = null
  private var gamePaused = false
  private var gameOver = false
  private var timerMilliseconds = 0L
  private var coinCount = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Start preloading.
    startPreloadingWithCallback()

    startGame()

    // Create the "Play again" button.
    binding.playAgainButton.setOnClickListener { startGame() }

    // Create the "Watch video" button.
    binding.watchVideoButton.setOnClickListener {
      pollAndShowAd()
      updateUI()
    }

    updateUI()
  }

  private fun startPreloadingWithCallback() {
    val preloadCallback =
      // [Important] Do not call preload start or poll ad within the callback.
      object : PreloadCallback {
        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
          Log.i(Constant.TAG, ("Rewarded preload ad failed to load with error: " + adError.message))
        }

        override fun onAdsExhausted(preloadId: String) {
          Log.i(Constant.TAG, "Rewarded preload ad is not available.")
          showToast("Rewarded ads exhausted.")
          updateUI()
        }

        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
          Log.i(Constant.TAG, "Rewarded preload ad is available.")
          showToast("Rewarded ad preloaded.")
          updateUI()
        }
      }
    val adRequest: AdRequest = AdRequest.Builder(AD_UNIT_ID).build()
    val preload = PreloadConfiguration(adRequest)
    RewardedAdPreloader.start(AD_UNIT_ID, preload, preloadCallback)
  }

  private fun pollAndShowAd() {
    // Polling returns the next available ad and loads another ad in the background.
    val ad = RewardedAdPreloader.pollAd(AD_UNIT_ID)

    // Interact with the ad object as needed.
    ad?.apply {
      rewardedAd = ad
      Log.d(Constant.TAG, "Rewarded ad response info: ${this.getResponseInfo()}")
      this.adEventCallback =
        object : RewardedAdEventCallback {
          override fun onAdImpression() {
            Log.d(Constant.TAG, "Rewarded ad recorded an impression.")
          }

          override fun onAdPaid(value: AdValue) {
            Log.d(
              Constant.TAG,
              "Rewarded ad onPaidEvent: ${value.valueMicros} ${value.currencyCode}",
            )
          }
        }

      // Show the ad.
      ad.show(requireActivity()) { reward ->
        showToast("Reward earned: ${reward.amount} ${reward.type}")
        Log.d(Constant.TAG, "User earned a reward: ${reward.amount} ${reward.type}")
      }
    }
  }

  fun updateUI() {
    val isAdAvailable = RewardedAdPreloader.isAdAvailable(AD_UNIT_ID)
    runOnUiThread {
      if (isAdAvailable) {
        binding.txtStatus.text = getString(R.string.available)
      } else {
        binding.txtStatus.text = getString(R.string.exhausted)
      }
      binding.watchVideoButton.isEnabled = isAdAvailable
      binding.coins.text = getString(R.string.coins, coinCount)
    }
  }

  /** Update the current coin count. */
  private fun addCoins(coins: Int) {
    activity?.runOnUiThread {
      coinCount += coins
      binding.coins.text = getString(R.string.coins, coinCount)
    }
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
          addCoins(GAME_OVER_REWARD)
          binding.playAgainButton.visibility = View.VISIBLE
          rewardedAd?.let { ad ->
            binding.watchVideoButton.text =
              MessageFormat.format(
                getString(R.string.watch_video_coins),
                mapOf("count" to ad.getRewardItem().amount),
              )
            updateUI()
          }
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
    const val GAME_OVER_REWARD = 1
    // Sample rewarded ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
  }
}
