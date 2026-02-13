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

package com.example.nextgenexample.rewardedInterstitial

import android.icu.text.MessageFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentRewardedInterstitialBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback

/** A simple [Fragment] subclass that loads a rewarded interstitial ad. */
class RewardedInterstitialSingleLoadFragment :
  AdFragment<FragmentRewardedInterstitialBinding>(), AdDialogFragment.AdDialogListener {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentRewardedInterstitialBinding
    get() = FragmentRewardedInterstitialBinding::inflate

  private var rewardedInterstitialAd: RewardedInterstitialAd? = null
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
    }

    // Display the current coin count.
    binding.coins.text = getString(R.string.coins, coinCount)

    return binding.root
  }

  /** Dismiss the dialog and attempt to show the ad. */
  override fun onDialogCountdownFinish(dialog: DialogFragment) {
    dialog.dismiss()
    showRewardedInterstitialAd()
  }

  /** Loads a new ad if one isn't already loaded. */
  private fun loadAd() {
    if (rewardedInterstitialAd != null) {
      Log.d(Constant.TAG, "Rewarded interstitial ad already loaded.")
      return
    }

    RewardedInterstitialAd.load(
      AdRequest.Builder(AD_UNIT_ID).build(),
      object : AdLoadCallback<RewardedInterstitialAd> {
        override fun onAdLoaded(ad: RewardedInterstitialAd) {
          rewardedInterstitialAd = ad
          showToast("Rewarded interstitial ad loaded.")
          Log.d(Constant.TAG, "Rewarded interstitial ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          rewardedInterstitialAd = null
          showToast("Rewarded interstitial ad failed to load.")
          Log.w(Constant.TAG, "Rewarded interstitial ad failed to load: $adError")
        }
      },
    )
  }

  /** Show the ad if it's ready or attempt to load an ad. */
  private fun showRewardedInterstitialAd() {
    if (rewardedInterstitialAd == null) {
      loadAd()
      Log.d(Constant.TAG, "The rewarded interstitial ad wasn't ready yet.")
      return
    }

    // Listen for ad events.
    rewardedInterstitialAd?.adEventCallback =
      object : RewardedInterstitialAdEventCallback {
        override fun onAdShowedFullScreenContent() {
          showToast("Rewarded interstitial ad shown.")
          Log.d(Constant.TAG, "Rewarded interstitial ad showed.")
        }

        override fun onAdDismissedFullScreenContent() {
          rewardedInterstitialAd = null
          showToast("Rewarded interstitial ad dismissed.")
          Log.d(Constant.TAG, "Rewarded interstitial ad dismissed.")
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          rewardedInterstitialAd = null
          showToast("Rewarded interstitial ad failed to show.")
          Log.w(Constant.TAG, "Rewarded interstitial ad failed to show: $fullScreenContentError")
        }

        override fun onAdImpression() {
          Log.d(Constant.TAG, "Rewarded interstitial ad recorded an impression.")
        }

        override fun onAdClicked() {
          Log.d(Constant.TAG, "Rewarded interstitial ad recorded a click.")
        }
      }
    // Show the ad.
    rewardedInterstitialAd?.show(
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

  /** Create the game timer, which counts down to zero and shows the "Play again" button. */
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
          AdDialogFragment().show(childFragmentManager, AdDialogFragment.TAG)
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

    // Sample rewarded interstitial ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
  }
}
