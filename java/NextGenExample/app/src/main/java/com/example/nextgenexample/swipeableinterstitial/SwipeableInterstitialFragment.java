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
 * - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nextgenexample.swipeableinterstitial;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.nextgenexample.AdFragment;
import com.example.nextgenexample.R;
import com.example.nextgenexample.databinding.FragmentSwipeableInterstitialBinding;
import com.example.nextgenexample.databinding.ItemSwipeableAdBinding;
import com.example.nextgenexample.databinding.ItemSwipeableContentBinding;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.VideoController;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.swipeableinterstitial.SwipeableInterstitialAdRequest;
import java.util.Map;

/** A simple [Fragment] subclass demonstrating swipeable interstitial ads in a vertical feed. */
@ExperimentalApi
public class SwipeableInterstitialFragment
    extends AdFragment<FragmentSwipeableInterstitialBinding> {
  // Default constructor required for fragment instantiation.
  public SwipeableInterstitialFragment() {}

  private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/3763914985";
  private static final String TAG = "SwipeableInterstitialFragment";
  private static final Map<Integer, Integer> ID_TO_SECONDS =
      Map.of(
          R.id.radio_0s, 0,
          R.id.radio_5s, 5);
  private static final Map<Integer, Integer> SECONDS_TO_ID =
      Map.of(
          0, R.id.radio_0s,
          5, R.id.radio_5s);
  private SwipeableInterstitialAd swipeableAd;
  private long holdStartTimeMs = 0L;
  private long holdDurationMs = 0L;
  private int requestedMaxScreenHoldSeconds = 0;
  private int lastSettledPosition = 0;

  private enum AdLoadState {
    IDLE("", Color.TRANSPARENT),
    LOADING("Ad loading...", Color.YELLOW),
    LOADED("Ad loaded", Color.GREEN),
    FAILED("Ad failed to load", Color.RED);

    public final String statusText;
    public final int statusColor;

    AdLoadState(String statusText, int statusColor) {
      this.statusText = statusText;
      this.statusColor = statusColor;
    }
  }

  private final MutableLiveData<AdLoadState> adLoadState = new MutableLiveData<>(AdLoadState.IDLE);
  private final Handler screenHoldHandler = new Handler(Looper.getMainLooper());
  private final ViewPager2.OnPageChangeCallback pageChangeCallback =
      new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrollStateChanged(int state) {
          super.onPageScrollStateChanged(state);
          if (state == ViewPager2.SCROLL_STATE_IDLE && binding != null) {
            int position = binding.viewPager.getCurrentItem();
            int previousPosition = lastSettledPosition;
            // Ignore redundant page-settle callbacks when snapping back to the same slide
            // after a partial swipe, so we do not restart the hold timer or reload the ad.
            if (position == previousPosition) {
              return;
            }
            lastSettledPosition = position;
            binding.backButton.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Page settled: " + position);
            // In this simulated vertical feed, even positions are content
            // slides where a fresh ad is preloaded. Odd positions are ad slots
            // where the ad displays and locks the screen once settled.
            if (position % 2 == 1) {
              adLoadState.setValue(AdLoadState.IDLE);
              showSwipeableInterstitialAd();
              if (previousPosition % 2 == 0) {
                startHoldScreen();
              }
            } else {
              reloadSwipeableInterstitialAd();
            }
          }
        }
      };

  @Override
  protected BindingInflater<FragmentSwipeableInterstitialBinding> getBindingInflater() {
    return FragmentSwipeableInterstitialBinding::inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
    // Disable RecyclerView overscroll and item change animations to prevent
    // video rendering conflicts and crossfade flashing on the background.
    preventOverscrollArtifacts(binding.viewPager);
    binding.viewPager.setAdapter(new SwipeablePagerAdapter());
    binding.viewPager.registerOnPageChangeCallback(pageChangeCallback);
    adLoadState.observe(
        getViewLifecycleOwner(),
        state -> {
          if (binding == null || binding.viewPager.getAdapter() == null) {
            return;
          }
          binding.viewPager.post(
              () -> {
                if (binding == null || binding.viewPager.getAdapter() == null) {
                  return;
                }
                int contentPosition =
                    binding.viewPager.getCurrentItem() % 2 == 0
                        ? binding.viewPager.getCurrentItem()
                        : binding.viewPager.getCurrentItem() - 1;
                binding.viewPager.getAdapter().notifyItemChanged(contentPosition);
              });
        });
    binding.backButton.setVisibility(View.VISIBLE);
    binding.backButton.setOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    binding.viewPager.post(this::reloadSwipeableInterstitialAd);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    discardAd();
  }

  @Override
  public void onDestroyView() {
    screenHoldHandler.removeCallbacksAndMessages(null);
    binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
    super.onDestroyView();
  }

  @Override
  public void onResume() {
    super.onResume();
    // Hide the host activity's action bar so the swipeable feed renders fullscreen.
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity != null && activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().hide();
    }
    checkScreenHoldOnReturn();
  }

  @Override
  public void onPause() {
    // Restore the host activity's action bar when leaving the swipeable feed.
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity != null && activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().show();
    }
    super.onPause();
  }

  private void checkScreenHoldOnReturn() {
    if (binding == null || binding.viewPager.isUserInputEnabled() || holdStartTimeMs == 0L) {
      return;
    }
    long elapsed = System.currentTimeMillis() - holdStartTimeMs;
    if (elapsed >= holdDurationMs) {
      screenHoldHandler.removeCallbacksAndMessages(null);
      binding.viewPager.setUserInputEnabled(true);
      holdStartTimeMs = 0L;
      Log.d(TAG, "Screen hold completed during background/overlay.");
    }
  }

  public void reloadSwipeableInterstitialAd() {
    if (adLoadState.getValue() == AdLoadState.LOADING) {
      return;
    }
    discardAd();
    adLoadState.setValue(AdLoadState.LOADING);
    binding.viewPager.setUserInputEnabled(false);

    SwipeableInterstitialAdRequest request =
        createSwipeableInterstitialRequest(requestedMaxScreenHoldSeconds);
    SwipeableInterstitialAd.load(
        request,
        new AdLoadCallback<>() {
          @Override
          public void onAdLoaded(@NonNull SwipeableInterstitialAd ad) {
            swipeableAd = ad;
            registerAdEventCallbacks(ad);
            registerVideoLifecycleCallbacks(ad);
            runOnUiThread(
                () -> {
                  adLoadState.setValue(AdLoadState.LOADED);
                  if (binding.viewPager.getCurrentItem() % 2 == 0) {
                    binding.viewPager.setUserInputEnabled(true);
                  }
                  showSwipeableInterstitialAd();
                });
            Log.d(TAG, "Ad loaded.");
          }

          @Override
          public void onAdFailedToLoad(@NonNull LoadAdError adError) {
            swipeableAd = null;
            runOnUiThread(
                () -> {
                  if (binding.viewPager.getCurrentItem() % 2 == 0) {
                    binding.viewPager.setUserInputEnabled(true);
                  }
                  adLoadState.setValue(AdLoadState.FAILED);
                });
            Log.d(TAG, "Ad failed to load: " + adError);
          }
        });
  }

  /**
   * Creates a swipeable interstitial ad request with a screen hold duration. By default, swipeable
   * interstitial ads match the fullscreen size. If requesting a custom ad size, it must fill at
   * least 60% of the screen size.
   *
   * @param maxScreenHoldDurationSeconds The maximum screen hold duration in seconds to allow ads
   *     that require screen hold.
   */
  public SwipeableInterstitialAdRequest createSwipeableInterstitialRequest(
      int maxScreenHoldDurationSeconds) {
    return new SwipeableInterstitialAdRequest.Builder(AD_UNIT_ID)
        .setMaxScreenHoldDurationSeconds(maxScreenHoldDurationSeconds)
        .build();
  }

  public void registerAdEventCallbacks(SwipeableInterstitialAd swipeableAd) {
    swipeableAd.setAdEventCallback(
        new SwipeableInterstitialAdEventCallback() {
          @Override
          public void onAdShowedFullScreenContent() {
            // Called when ad full screen content has been shown.
            Log.d(TAG, "Ad showed full screen content.");
          }

          @Override
          public void onAdDismissedFullScreenContent() {
            // Called when the ad full screen content is dismissed.
            Log.d(TAG, "Ad dismissed full screen content.");
          }

          @Override
          public void onAdImpression() {
            // Called when an impression is recorded for an ad.
            Log.d(TAG, "Ad recorded an impression.");
          }

          @Override
          public void onAdClicked() {
            // Called when a click is recorded for an ad.
            Log.d(TAG, "Ad recorded a click.");
          }

          @Override
          public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
            // Called when ad full screen content failed to show.
            Log.d(TAG, "Ad failed to show full screen content.");
          }

          @Override
          public void onAdScreenHoldTimerStarted() {
            Log.d(TAG, "Ad started screen hold timer.");
          }
        });
  }

  public void showSwipeableInterstitialAd() {
    if (swipeableAd == null || binding == null || binding.viewPager.getAdapter() == null) {
      return;
    }
    // Notify the adapter to bind the ad view to the active or upcoming ad
    // container.
    binding.viewPager.post(
        () -> {
          if (binding == null || binding.viewPager.getAdapter() == null) {
            return;
          }
          int adPosition =
              binding.viewPager.getCurrentItem() % 2 == 1
                  ? binding.viewPager.getCurrentItem()
                  : binding.viewPager.getCurrentItem() + 1;
          binding.viewPager.getAdapter().notifyItemChanged(adPosition);
        });
  }

  public void startHoldScreen() {
    SwipeableInterstitialAd ad = swipeableAd;
    int holdTime = (ad != null) ? ad.getScreenHoldDuration() : 0;
    if (holdTime <= 0) {
      if (binding != null) {
        binding.viewPager.setUserInputEnabled(true);
      }
      return;
    }

    if (binding == null) {
      return;
    }
    // Disable scrolling during screen hold.
    binding.viewPager.setUserInputEnabled(false);

    // Post a delayed action to unlock the interface once elapsed.
    holdDurationMs = holdTime * 1000L;
    screenHoldHandler.removeCallbacksAndMessages(null);
    screenHoldHandler.postDelayed(
        () -> {
          if (binding != null) {
            binding.viewPager.setUserInputEnabled(true);
            Log.d(TAG, "Screen hold completed");
          }
        },
        holdDurationMs);
  }

  private void preventOverscrollArtifacts(ViewPager2 viewPager) {
    for (int i = 0; i < viewPager.getChildCount(); i++) {
      View child = viewPager.getChildAt(i);
      if (child instanceof RecyclerView) {
        child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        ((RecyclerView) child).setItemAnimator(null);
        break;
      }
    }
  }

  public void discardAd() {
    if (swipeableAd != null) {
      Activity activity = getActivity();
      if (activity != null) {
        View adView = swipeableAd.getView(activity);
        if (adView.getParent() instanceof ViewGroup) {
          ((ViewGroup) adView.getParent()).removeView(adView);
        }
      }
      swipeableAd.destroy();
      swipeableAd = null;
    }
  }

  public void registerVideoLifecycleCallbacks(SwipeableInterstitialAd swipeableAd) {
    swipeableAd.setVideoLifecycleCallbacks(
        new VideoController.VideoLifecycleCallbacks() {
          @Override
          public void onVideoStart() {
            // Optional: Pause any app video content when the ad video starts.
            Log.d(TAG, "Video started.");
          }

          @Override
          public void onVideoPlay() {
            // Optional: Pause any app video content when the ad video plays.
            Log.d(TAG, "Video played.");
          }

          @Override
          public void onVideoPause() {
            // Optional: Resume any app video content when the ad video pauses.
            Log.d(TAG, "Video paused.");
          }

          @Override
          public void onVideoEnd() {
            // Optional: Resume any app video content when the ad video ends.
            Log.d(TAG, "Video ended.");
          }

          @Override
          public void onVideoMute(boolean booleanMute) {
            // Optional: Handle ad video mute state changes.
            Log.d(TAG, "Video mute state changed: " + booleanMute);
          }
        });
  }

  private class SwipeablePagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_AD = 1;

    @Override
    public int getItemViewType(int position) {
      return (position % 2 == 0) ? TYPE_CONTENT : TYPE_AD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      if (viewType == TYPE_CONTENT) {
        ItemSwipeableContentBinding binding =
            ItemSwipeableContentBinding.inflate(inflater, parent, false);
        return new ContentViewHolder(binding);
      } else {
        ItemSwipeableAdBinding binding =
            ItemSwipeableAdBinding.inflate(inflater, parent, false);
        return new AdViewHolder(binding);
      }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof ContentViewHolder contentHolder) {
        AdLoadState state =
            adLoadState.getValue() != null ? adLoadState.getValue() : AdLoadState.IDLE;
        contentHolder.adLoadStateLabel.setText(state.statusText);
        contentHolder.adLoadStateLabel.setTextColor(state.statusColor);
        contentHolder.loadedHoldTimeLabel.setText(
            swipeableAd != null
                ? "Loaded hold time: " + swipeableAd.getScreenHoldDuration() + "s"
                : " ");

        // Remove any old listener before setting the checked state to prevent triggering
        // the listener from a recycled view.
        contentHolder.screenHoldRadioGroup.setOnCheckedChangeListener(null);
        Integer targetIdObj =
            SECONDS_TO_ID.getOrDefault(requestedMaxScreenHoldSeconds, R.id.radio_0s);
        int targetId = targetIdObj != null ? targetIdObj : R.id.radio_0s;
        contentHolder.screenHoldRadioGroup.check(targetId);
        contentHolder.screenHoldRadioGroup.setOnCheckedChangeListener(
            (group, checkedId) -> {
              Integer newHoldSecondsObj = ID_TO_SECONDS.getOrDefault(checkedId, 0);
              int newHoldSeconds = newHoldSecondsObj != null ? newHoldSecondsObj : 0;
              if (newHoldSeconds != requestedMaxScreenHoldSeconds) {
                requestedMaxScreenHoldSeconds = newHoldSeconds;
                binding.viewPager.post(
                    SwipeableInterstitialFragment.this::reloadSwipeableInterstitialAd);
              }
            });
      } else if (holder instanceof AdViewHolder adHolder) {
        int current = binding.viewPager.getCurrentItem();
        if (position == current || (current % 2 == 0 && position == current + 1)) {
          if (swipeableAd != null) {
            View adView = swipeableAd.getView(requireActivity());
            if (adView.getParent() == null) {
              adHolder.adContainer.addView(adView);
            } else if (adView.getParent() != adHolder.adContainer) {
              ((ViewGroup) adView.getParent()).removeView(adView);
              adHolder.adContainer.addView(adView);
            }
          }
        }
      }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
      super.onViewRecycled(holder);
      if (holder instanceof AdViewHolder adHolder) {
        adHolder.adContainer.removeAllViews();
      }
    }

    @Override
    public int getItemCount() {
      // Returns a large number to simulate an infinite scrolling vertical feed.
      return Integer.MAX_VALUE;
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
      RadioGroup screenHoldRadioGroup;
      TextView loadedHoldTimeLabel;
      TextView adLoadStateLabel;

      ContentViewHolder(ItemSwipeableContentBinding binding) {
        super(binding.getRoot());
        screenHoldRadioGroup = binding.screenHoldRadioGroup;
        loadedHoldTimeLabel = binding.loadedHoldTimeLabel;
        adLoadStateLabel = binding.adLoadStateLabel;
      }
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
      FrameLayout adContainer;

      AdViewHolder(ItemSwipeableAdBinding binding) {
        super(binding.getRoot());
        adContainer = binding.adContainer;
      }
    }
  }
}
