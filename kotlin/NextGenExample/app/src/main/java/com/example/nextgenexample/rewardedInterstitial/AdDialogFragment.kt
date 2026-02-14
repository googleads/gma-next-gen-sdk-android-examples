package com.example.nextgenexample.rewardedInterstitial

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.nextgenexample.R

/**
 * A dialog fragment to inform the users about an upcoming video ad and let the user click the
 * cancel button to skip the ad.
 */
class AdDialogFragment : DialogFragment() {
  // Use this instance of the interface to deliver action events.
  private lateinit var listener: AdDialogListener
  private var countdownTimer: CountDownTimer? = null
  private var timerMilliseconds = 0L

  // The fragment that creates an instance of this dialog fragment must
  // implement this interface to receive event callbacks. Each method passes
  // the DialogFragment in case the host needs to query it.
  interface AdDialogListener {
    fun onDialogCountdownFinish(dialog: DialogFragment)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    // Verify that the parent fragment implements the callback interface.
    try {
      // Instantiate the AdDialogListener so you can send events to
      // the host.
      listener = parentFragment as AdDialogListener
    } catch (e: ClassCastException) {
      // The fragment doesn't implement the interface. Throw exception.
      throw ClassCastException(("$context must implement AdDialogListener"))
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    super.onCreateDialog(savedInstanceState)

    createTimer()

    return AlertDialog.Builder(requireContext())
      .setTitle(getString(R.string.watch_video_for_reward))
      .setMessage(getString(R.string.video_starting, COUNTDOWN_LENGTH_MILLISECONDS))
      .setNegativeButton(getString(R.string.no_thanks)) { _, _ -> countdownTimer?.cancel() }
      .create()
  }

  /** Create the countdown timer, which attempts to show an ad at zero. */
  private fun createTimer() {
    countdownTimer?.cancel()

    countdownTimer =
      object : CountDownTimer(COUNTDOWN_LENGTH_MILLISECONDS, COUNTDOWN_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
          timerMilliseconds = millisUntilFinished
          // Display countdown start from 5.0 seconds.
          val textView = dialog?.findViewById(android.R.id.message) as? TextView
          val seconds =
            millisUntilFinished / 1000 +
              if ((millisUntilFinished % 1000).toInt() == 0) {
                0
              } else {
                1
              }
          textView?.text = getString(R.string.video_starting, seconds)
        }

        override fun onFinish() {
          listener.onDialogCountdownFinish(this@AdDialogFragment)
        }
      }

    countdownTimer?.start()
  }

  /** Stop the timer when the back button is pressed. */
  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)
    countdownTimer?.cancel()
  }

  companion object {
    const val COUNTDOWN_INTERVAL = 50L
    const val COUNTDOWN_LENGTH_MILLISECONDS = 5000L
    const val TAG = "AdDialog"
  }
}
