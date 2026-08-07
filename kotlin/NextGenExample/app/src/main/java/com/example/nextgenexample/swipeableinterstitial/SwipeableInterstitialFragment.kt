/*
 * Copyright 2026 Google LLC
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

package com.example.nextgenexample.swipeableinterstitial

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentSwipeableInterstitialBinding
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.VideoController
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A simple [Fragment] subclass demonstrating swipeable interstitial ads in a vertical feed. */
@OptIn(ExperimentalApi::class)
class SwipeableInterstitialFragment : AdFragment<FragmentSwipeableInterstitialBinding>() {
  override val bindingInflater:
    (LayoutInflater, ViewGroup?, Boolean) -> FragmentSwipeableInterstitialBinding
    get() = FragmentSwipeableInterstitialBinding::inflate

  private var swipeableAd: SwipeableInterstitialAd? = null
  private var holdStartTimeMs: Long = 0L
  private var holdDurationMs: Long = 0L
  private var requestedMaxScreenHoldSeconds = 0
  private var lastSettledPosition = 0
  private val pageChangeCallback =
    object : ViewPager2.OnPageChangeCallback() {
      override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        if (state == ViewPager2.SCROLL_STATE_IDLE) {
          val position = binding.viewPager.currentItem
          val previousPosition = lastSettledPosition
          // Ignore redundant page-settle callbacks when snapping back to the same slide
          // after a partial swipe, so we do not restart the hold timer or reload the ad.
          if (position == previousPosition) return
          lastSettledPosition = position
          binding.backButton.visibility = if (position % 2 == 0) View.VISIBLE else View.GONE
          Log.d(TAG, "Page settled: $position")
          // In this simulated vertical feed, even positions are content
          // slides where a fresh ad is preloaded. Odd positions are ad slots
          // where the ad displays and locks the screen once settled.
          if (position % 2 == 1) {
            adLoadState.value = AdLoadState.IDLE
            showSwipeableInterstitialAd()
            if (previousPosition % 2 == 0) {
              startHoldScreen()
            }
          } else {
            reloadSwipeableInterstitialAd()
          }
        }
      }
    }

  private enum class AdLoadState(val statusText: String, val statusColor: Int) {
    IDLE("", Color.TRANSPARENT),
    LOADING("Ad loading...", Color.YELLOW),
    LOADED("Ad loaded", Color.GREEN),
    FAILED("Ad failed to load", Color.RED),
  }

  private val adLoadState = MutableLiveData(AdLoadState.IDLE)
  private var screenHoldJob: Job? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
    // Disable RecyclerView overscroll and item change animations to prevent
    // video rendering conflicts and crossfade flashing on the background.
    preventOverscrollArtifacts(binding.viewPager)
    binding.viewPager.adapter = SwipeablePagerAdapter()
    binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
    adLoadState.observe(viewLifecycleOwner) { _ ->
      if (binding.viewPager.adapter == null) return@observe
      binding.viewPager.post {
        if (binding.viewPager.adapter == null) return@post
        val contentPosition =
          if (binding.viewPager.currentItem % 2 == 0) {
            binding.viewPager.currentItem
          } else {
            binding.viewPager.currentItem - 1
          }
        binding.viewPager.adapter?.notifyItemChanged(contentPosition)
      }
    }
    binding.backButton.visibility = View.VISIBLE
    binding.backButton.setOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    viewLifecycleOwner.lifecycleScope.launch { reloadSwipeableInterstitialAd() }
  }

  override fun onDestroy() {
    super.onDestroy()
    discardAd()
  }

  override fun onDestroyView() {
    screenHoldJob?.cancel()
    binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
    super.onDestroyView()
  }

  override fun onResume() {
    super.onResume()
    // Hide the host activity's action bar so the swipeable feed renders fullscreen.
    (activity as? AppCompatActivity)?.supportActionBar?.hide()
    checkScreenHoldOnReturn()
  }

  override fun onPause() {
    // Restore the host activity's action bar when leaving the swipeable feed.
    (activity as? AppCompatActivity)?.supportActionBar?.show()
    super.onPause()
  }

  private fun checkScreenHoldOnReturn() {
    if (binding.viewPager.isUserInputEnabled || holdStartTimeMs == 0L) return
    val elapsed = System.currentTimeMillis() - holdStartTimeMs
    if (elapsed >= holdDurationMs) {
      screenHoldJob?.cancel()
      binding.viewPager.isUserInputEnabled = true
      holdStartTimeMs = 0L
      Log.d(TAG, "Screen hold completed during background/overlay.")
    }
  }

  fun reloadSwipeableInterstitialAd() {
    if (adLoadState.value == AdLoadState.LOADING) return
    discardAd()
    binding.viewPager.isUserInputEnabled = false

    // By default, swipeable interstitial ads match the fullscreen size. If requesting a custom
    // ad size, it must fill at least 60% of the screen size.
    val requestBuilder = SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
    if (requestedMaxScreenHoldSeconds > 0) {
      requestBuilder.setMaxScreenHoldDurationSeconds(requestedMaxScreenHoldSeconds)
    }
    SwipeableInterstitialAd.load(
      requestBuilder.build(),
      object : AdLoadCallback<SwipeableInterstitialAd> {
        override fun onAdLoaded(ad: SwipeableInterstitialAd) {
          swipeableAd = ad
          registerAdEventCallbacks(ad)
          registerVideoLifecycleCallbacks(ad)
          runOnUiThread {
            adLoadState.value = AdLoadState.LOADED
            if (binding.viewPager.currentItem % 2 == 0) {
              binding.viewPager.isUserInputEnabled = true
            }
            showSwipeableInterstitialAd()
          }
          Log.d(TAG, "Ad loaded.")
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          swipeableAd = null
          runOnUiThread {
            if (binding.viewPager.currentItem % 2 == 0) {
              binding.viewPager.isUserInputEnabled = true
            }
            adLoadState.value = AdLoadState.FAILED
          }
          Log.d(TAG, "Ad failed to load: $adError")
        }
      },
    )
  }

  fun registerAdEventCallbacks(swipeableAd: SwipeableInterstitialAd) {
    swipeableAd.adEventCallback =
      object : SwipeableInterstitialAdEventCallback {
        override fun onAdClicked() {
          // Called when a click is recorded for an ad.
          Log.d(TAG, "Ad recorded a click.")
        }

        override fun onAdDismissedFullScreenContent() {
          // Called when the ad full screen content is dismissed.
          Log.d(TAG, "Ad dismissed full screen content.")
          checkScreenHoldOnReturn()
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError
        ) {
          // Called when ad full screen content failed to show.
          Log.d(TAG, "Ad failed to show full screen content.")
        }

        override fun onAdImpression() {
          // Called when an impression is recorded for an ad.
          Log.d(TAG, "Ad recorded an impression.")
        }

        override fun onAdShowedFullScreenContent() {
          // Called when ad full screen content has been shown.
          Log.d(TAG, "Ad showed full screen content.")
        }

        override fun onAdScreenHoldTimerStarted() {
          Log.d(TAG, "Ad started screen hold timer.")
        }
      }
  }

  fun showSwipeableInterstitialAd() {
    if (swipeableAd == null || binding.viewPager.adapter == null) {
      return
    }
    // Notify the adapter to bind the ad view to the active or upcoming ad
    // container.
    binding.viewPager.post {
      if (binding.viewPager.adapter == null) return@post
      val adPosition =
        if (binding.viewPager.currentItem % 2 == 1) {
          binding.viewPager.currentItem
        } else {
          binding.viewPager.currentItem + 1
        }
      binding.viewPager.adapter?.notifyItemChanged(adPosition)
    }
  }

  fun startHoldScreen() {
    val ad = swipeableAd
    val holdTime = ad?.getScreenHoldDuration() ?: 0
    if (holdTime <= 0) {
      binding.viewPager.isUserInputEnabled = true
      return
    }

    // Disable scrolling during screen hold.
    binding.viewPager.isUserInputEnabled = false
    holdStartTimeMs = System.currentTimeMillis()
    holdDurationMs = holdTime * 1000L

    // Launch a coroutine to unlock the interface once elapsed.
    screenHoldJob?.cancel()
    screenHoldJob =
      viewLifecycleOwner.lifecycleScope.launch {
        delay(holdDurationMs)
        binding.viewPager.isUserInputEnabled = true
        holdStartTimeMs = 0L
        Log.d(TAG, "Screen hold completed")
      }
  }

  private fun preventOverscrollArtifacts(viewPager: ViewPager2) {
    for (i in 0 until viewPager.childCount) {
      val child = viewPager.getChildAt(i)
      if (child is RecyclerView) {
        child.overScrollMode = View.OVER_SCROLL_NEVER
        child.itemAnimator = null
        break
      }
    }
  }

  fun discardAd() {
    swipeableAd?.let { ad ->
      activity?.let { act ->
        val adView = ad.getView(act)
        (adView.parent as? ViewGroup)?.removeView(adView)
      }
      ad.destroy()
    }
    swipeableAd = null
  }

  fun registerVideoLifecycleCallbacks(swipeableAd: SwipeableInterstitialAd) {
    swipeableAd.videoLifecycleCallbacks =
      object : VideoController.VideoLifecycleCallbacks {
        override fun onVideoStart() {
          // Optional: Pause any app video content when the ad video starts.
          Log.d(TAG, "Video started.")
        }

        override fun onVideoPlay() {
          // Optional: Pause any app video content when the ad video plays.
          Log.d(TAG, "Video played.")
        }

        override fun onVideoPause() {
          // Optional: Resume any app video content when the ad video pauses.
          Log.d(TAG, "Video paused.")
        }

        override fun onVideoEnd() {
          // Optional: Resume any app video content when the ad video ends.
          Log.d(TAG, "Video ended.")
        }

        override fun onVideoMute(isMuted: Boolean) {
          // Optional: Handle ad video mute state changes.
          Log.d(TAG, "Video mute: $isMuted")
        }
      }
  }

  private inner class SwipeablePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_CONTENT = 0
    private val TYPE_AD = 1

    override fun getItemViewType(position: Int): Int {
      return if (position % 2 == 0) TYPE_CONTENT else TYPE_AD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      return if (viewType == TYPE_CONTENT) {
        val view = inflater.inflate(R.layout.item_swipeable_content, parent, false)
        ContentViewHolder(view)
      } else {
        val view = inflater.inflate(R.layout.item_swipeable_ad, parent, false)
        AdViewHolder(view)
      }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      if (holder is ContentViewHolder) {
        val state = adLoadState.value ?: AdLoadState.IDLE
        holder.adLoadStateLabel.text = state.statusText
        holder.adLoadStateLabel.setTextColor(state.statusColor)
        val ad = swipeableAd
        holder.loadedHoldTimeLabel.text =
          if (ad != null) {
            "Loaded hold time: ${ad.getScreenHoldDuration()}s"
          } else {
            " "
          }

        // Remove any old listener before setting the checked state to prevent triggering
        // the listener from a recycled view.
        holder.screenHoldRadioGroup.setOnCheckedChangeListener(null)
        val targetId = SECONDS_TO_ID[requestedMaxScreenHoldSeconds] ?: R.id.radio_0s
        holder.screenHoldRadioGroup.check(targetId)
        holder.screenHoldRadioGroup.setOnCheckedChangeListener { _, checkedId ->
          val newHoldSeconds = ID_TO_SECONDS[checkedId] ?: 0
          if (newHoldSeconds != requestedMaxScreenHoldSeconds) {
            requestedMaxScreenHoldSeconds = newHoldSeconds
            viewLifecycleOwner.lifecycleScope.launch { reloadSwipeableInterstitialAd() }
          }
        }
      } else if (holder is AdViewHolder) {
        val current = binding.viewPager.currentItem
        if (position == current || (current % 2 == 0 && position == current + 1)) {
          swipeableAd?.let { ad ->
            val adView = ad.getView(requireActivity())
            if (adView.parent == null) {
              holder.adContainer.addView(adView)
            } else if (adView.parent != holder.adContainer) {
              (adView.parent as? ViewGroup)?.removeView(adView)
              holder.adContainer.addView(adView)
            }
          }
        }
      }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
      super.onViewRecycled(holder)
      if (holder is AdViewHolder) {
        holder.adContainer.removeAllViews()
      }
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val screenHoldRadioGroup: RadioGroup = view.findViewById(R.id.screen_hold_radio_group)
      val loadedHoldTimeLabel: TextView = view.findViewById(R.id.loaded_hold_time_label)
      val adLoadStateLabel: TextView = view.findViewById(R.id.ad_load_state_label)
    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val adContainer: FrameLayout = view.findViewById(R.id.ad_container)
    }
  }

  private companion object {
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/3763914985"
    const val TAG = "SwipeableInterstitialFragment"
    val ID_TO_SECONDS = mapOf(R.id.radio_0s to 0, R.id.radio_5s to 5)
    val SECONDS_TO_ID = mapOf(0 to R.id.radio_0s, 5 to R.id.radio_5s)
  }
}
