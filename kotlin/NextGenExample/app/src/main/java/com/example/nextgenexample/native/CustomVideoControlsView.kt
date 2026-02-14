package com.example.nextgenexample.native

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.nextgenexample.R
import com.google.android.libraries.ads.mobile.sdk.common.VideoController
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaContent

/**
 * This view represents the status of a video controller and also displays custom controls for the
 * video controller when appropriate.
 */
class CustomVideoControlsView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
  LinearLayout(context, attrs, defStyle), VideoController.VideoLifecycleCallbacks {
  private val playButton: ImageButton
  private val muteButton: ImageButton
  private val controlsView: View
  private var isVideoPlaying: Boolean = true

  init {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    inflater.inflate(R.layout.custom_video_controls, this, true)
    playButton = findViewById(R.id.btn_play)
    muteButton = findViewById(R.id.btn_mute)
    controlsView = findViewById(R.id.video_controls)
    controlsView.visibility = View.GONE
  }

  fun destroy() {
    controlsView.visibility = View.GONE
  }

  /*
   * Sets up the custom controls view with the provided VideoController
   * and mute state, video lifecycle callbacks.
   */
  fun initialize(mediaContent: MediaContent?, muted: Boolean) {
    controlsView.visibility = View.GONE
    val videoController = mediaContent?.videoController
    if (
      mediaContent != null && videoController != null && videoController.isCustomControlsEnabled
    ) {
      controlsView.visibility = View.VISIBLE
      onVideoMute(muted)
      muteButton.setOnClickListener { videoController.mute(!videoController.isMuted) }
      playButton.setOnClickListener {
        if (isVideoPlaying) {
          videoController.pause()
        } else {
          videoController.play()
        }
      }
    }
  }

  override fun onVideoMute(muted: Boolean) {
    val muteResource = if (muted) R.drawable.video_mute else R.drawable.video_unmute
    muteButton.setImageResource(muteResource)
    super.onVideoMute(muted)
  }

  override fun onVideoPause() {
    playButton.setImageResource(R.drawable.video_play)
    isVideoPlaying = false
    super.onVideoPause()
  }

  override fun onVideoPlay() {
    playButton.setImageResource(R.drawable.video_pause)
    isVideoPlaying = true
    super.onVideoPlay()
  }

  override fun onVideoStart() {
    playButton.setImageResource(R.drawable.video_pause)
    isVideoPlaying = true
    super.onVideoStart()
  }

  override fun onVideoEnd() {
    playButton.setImageResource(R.drawable.video_play)
    isVideoPlaying = false
    super.onVideoEnd()
  }
}
