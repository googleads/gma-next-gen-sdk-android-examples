package com.example.nextgenexample.rewardedinterstitial;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.nextgenexample.R;
import java.util.Objects;

/**
 * A dialog fragment to inform the users about an upcoming video ad and let the user click the
 * cancel button to skip the ad.
 */
public class AdDialogFragment extends DialogFragment {
  private static final long COUNTDOWN_LENGTH_MILLISECONDS = 5000;
  private CountDownTimer countDownTimer;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);

    createTimer();

    return new AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.watch_video_for_reward))
        .setMessage(getString(R.string.video_starting, COUNTDOWN_LENGTH_MILLISECONDS))
        .setNegativeButton(
            getString(R.string.no_thanks),
            (dialogInterface, which) -> {
              if (countDownTimer != null) {
                countDownTimer.cancel();
              }
            })
        .create();
  }

  private void createTimer() {
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }

    countDownTimer =
        new CountDownTimer(COUNTDOWN_LENGTH_MILLISECONDS, 50) {
          @Override
          public void onTick(long millisUntilFinished) {
            int seconds = (int) (millisUntilFinished / 1000);
            if (millisUntilFinished % 1000 != 0) {
              seconds++;
            }
            if (getDialog() instanceof AlertDialog alertDialog) {
              alertDialog.setMessage(getString(R.string.video_starting, seconds));
            }
          }

          @Override
          public void onFinish() {
            getParentFragmentManager().setFragmentResult("adDialogCountdownComplete", Bundle.EMPTY);
            dismiss();
          }
        };

    countDownTimer.start();
  }

  @Override
  public void onCancel(@NonNull DialogInterface dialog) {
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
    super.onCancel(dialog);
  }

  @Override
  public void onDestroy() {
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
    super.onDestroy();
  }
}
