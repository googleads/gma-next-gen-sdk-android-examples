package com.example.nextgenexample.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.example.nextgenexample.R;
import com.example.nextgenexample.databinding.CustomVideoControlsBinding;
import com.google.android.libraries.ads.mobile.sdk.common.VideoController;
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaContent;

/**
 * This view represents the status of a video controller and also displays custom controls for the
 * video controller when appropriate.
 */
public class CustomVideoControlsView extends LinearLayout
    implements VideoController.VideoLifecycleCallbacks {
  private ImageButton playButton;
  private ImageButton muteButton;
  private View controlsView;
  private boolean isVideoPlaying;

  public CustomVideoControlsView(Context context) {
    super(context);
    init(context, null, 0);
  }

  public CustomVideoControlsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public CustomVideoControlsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(Context context, AttributeSet unusedAttrs, int unusedDefStyle) {
    CustomVideoControlsBinding binding =
        CustomVideoControlsBinding.inflate(LayoutInflater.from(context), this, true);

    playButton = binding.btnPlay;
    muteButton = binding.btnMute;
    controlsView = binding.videoControls;
    controlsView.setVisibility(View.GONE);
  }

  /*
   * Sets up the custom controls view with the provided VideoController and mute state.
   */
  @SuppressLint("SetTextI18n")
  public void initialize(MediaContent mediaContent, final boolean muted) {
    controlsView.setVisibility(View.GONE);
    VideoController videoController = mediaContent.getVideoController();
    if (mediaContent != null
        && mediaContent.getHasVideoContent()
        && videoController != null
        && videoController.isCustomControlsEnabled()) {
      controlsView.setVisibility(View.VISIBLE);
      onVideoMute(muted);
      muteButton.setOnClickListener(unusedView -> videoController.mute(!videoController.isMuted()));
      playButton.setOnClickListener(
          unusedView -> {
            if (isVideoPlaying) {
              videoController.pause();
            } else {
              videoController.play();
            }
          });
    }
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onVideoMute(final boolean muted) {
    Drawable img;
    if (muted) {
      img = ContextCompat.getDrawable(getContext(), R.drawable.video_mute);
    } else {
      img = ContextCompat.getDrawable(getContext(), R.drawable.video_unmute);
    }
    muteButton.setImageDrawable(img);
    }

  @SuppressLint("SetTextI18n")
  @Override
  public void onVideoPause() {
    Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.video_play);
    playButton.setImageDrawable(img);
    isVideoPlaying = false;
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onVideoPlay() {
    Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.video_pause);
    playButton.setImageDrawable(img);
    isVideoPlaying = true;
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onVideoStart() {
    Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.video_pause);
    playButton.setImageDrawable(img);
    isVideoPlaying = true;
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onVideoEnd() {
    Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.video_play);
    playButton.setImageDrawable(img);
    isVideoPlaying = false;
  }
}
