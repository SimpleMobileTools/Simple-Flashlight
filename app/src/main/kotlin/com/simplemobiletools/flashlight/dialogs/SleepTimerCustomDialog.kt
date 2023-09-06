package com.simplemobiletools.flashlight.dialogs

import android.app.Activity
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.databinding.DialogCustomSleepTimerPickerBinding

class SleepTimerCustomDialog(val activity: Activity, val callback: (seconds: Int) -> Unit) {
    private var dialog: AlertDialog? = null
    private val binding = DialogCustomSleepTimerPickerBinding.inflate(activity.layoutInflater)

    init {
        binding.dialogRadioView.check(R.id.dialog_radio_minutes)
        binding.timerValue.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialogConfirmed()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.sleep_timer) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(binding.timerValue)
                }
            }
    }

    private fun dialogConfirmed() {
        val value = binding.timerValue.value
        val minutes = Integer.valueOf(value.ifEmpty { "0" })
        val multiplier = getMultiplier(binding.dialogRadioView.checkedRadioButtonId)
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog?.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_seconds -> 1
        R.id.dialog_radio_minutes -> 60
        else -> 60
    }
}
