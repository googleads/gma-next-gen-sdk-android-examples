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

package com.example.nextgenexample.rewarded

import android.icu.text.MessageFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentRewardedBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback

/** A simple [Fragment] subclass that loads a rewarded ad. */
class RewardedSingleLoadFragment : AdFragment<FragmentRewardedBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRewardedBinding
    get() = FragmentRewardedBinding::inflate

  private var rewardedAd: RewardedAd? = null
  private var countdownTimer: CountDownTimer? = null
  private var gamePaused = false
  private var gameOver = false
  private var timerMilliseconds = 0L
  private var coinCount = 0

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
      binding.watchVideoButton.visibility = View.INVISIBLE
    }

    // Create the "Watch video" button.
    binding.watchVideoButton.visibility = View.INVISIBLE
    binding.watchVideoButton.setOnClickListener {
      showRewardedAd()
      binding.watchVideoButton.visibility = View.INVISIBLE
    }

    // Display the current coin count.
    binding.coins.text = getString(R.string.coins, coinCount)

    return binding.root
  }

  /** Loads a new ad if one isn't already loaded. */
  private fun loadAd() {
    if (rewardedAd != null) {
      Log.d(Constant.TAG, "Rewarded ad already loaded.")
      return
    }

    RewardedAd.load(
      AdRequest.Builder(AD_UNIT_ID).build(),
      object : AdLoadCallback<RewardedAd> {
        override fun onAdLoaded(ad: RewardedAd) {
          rewardedAd = ad
          showToast("Rewarded ad loaded.")
          Log.d(Constant.TAG, "Rewarded ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          rewardedAd = null
          showToast("Rewarded ad failed to load.")
          Log.w(Constant.TAG, "Rewarded ad failed to load: $adError")
        }
      },
    )
  }

  /** Show the ad if it's ready. Otherwise attempt to load an ad and restart the game. */
  private fun showRewardedAd() {
    if (rewardedAd == null) {
      loadAd()
      startGame()
      Log.d(Constant.TAG, "The rewarded ad wasn't ready yet.")
      return
    }

    // Listen for ad events.
    rewardedAd?.adEventCallback =
      object : RewardedAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          showToast("Rewarded ad shown.")
          Log.d(Constant.TAG, "Rewarded ad showed.")
        }

        override fun onAdDismissedFullScreenContent() {
          rewardedAd = null
          showToast("Rewarded ad dismissed.")
          Log.d(Constant.TAG, "Rewarded ad dismissed.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          rewardedAd = null
          showToast("Rewarded ad failed to show.")
          Log.w(Constant.TAG, "Rewarded ad failed to show: $fullScreenContentError")
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Rewarded ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Rewarded ad recorded a click.")
        }
      }
    // Show the ad.
    rewardedAd?.show(
      requireActivity(),
      object : OnUserEarnedRewardListener {
        override fun onUserEarnedReward(reward: RewardItem) {
          addCoins(reward.amount)
          showToast("Reward earned: ${reward.amount} ${reward.type}")
          Log.d(Constant.TAG, "User earned a reward: ${reward.amount} ${reward.type}")
        }
      },
    )
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
            binding.watchVideoButton.visibility = View.VISIBLE
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
