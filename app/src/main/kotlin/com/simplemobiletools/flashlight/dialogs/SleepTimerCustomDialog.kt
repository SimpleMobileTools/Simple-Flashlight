package com.simplemobiletools.flashlight.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.databinding.DialogCustomSleepTimerPickerBinding

class SleepTimerCustomDialog(val activity: Activity, val callback: (seconds: Int) -> Unit) {
    private var dialog: AlertDialog? = null
    private val binding = DialogCustomSleepTimerPickerBinding.inflate(activity.layoutInflater)

    init {
        binding.minutesHint.hint = activity.getString(R.string.minutes_raw).replaceFirstChar { it.uppercaseChar() }
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.sleep_timer) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(binding.minutes)
                }
            }
    }

    private fun dialogConfirmed() {
        val value = binding.minutes.value
        val minutes = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(minutes * 60)
        activity.hideKeyboard()
        dialog?.dismiss()
    }
}
