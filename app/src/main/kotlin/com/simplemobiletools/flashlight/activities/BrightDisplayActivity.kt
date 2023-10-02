package com.simplemobiletools.flashlight.activities

import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.stopSleepTimerCountDown
import com.simplemobiletools.flashlight.models.Events
import com.simplemobiletools.flashlight.screens.BrightDisplayScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess

class BrightDisplayActivity : SimpleActivity() {
    private val viewModel by viewModels<BrightDisplayViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val backgroundColor by viewModel.backgroundColor.collectAsStateWithLifecycle()
                val timerVisible by viewModel.timerVisible.collectAsStateWithLifecycle()
                val timerText by viewModel.timerText.collectAsStateWithLifecycle()

                BrightDisplayScreen(
                    backgroundColor = backgroundColor,
                    onChangeColorPress = {
                        ColorPickerDialog(this, config.brightDisplayColor, true, currentColorCallback = {
                            viewModel.updateBackgroundColor(it)
                        }) { wasPositivePressed, color ->
                            if (wasPositivePressed) {
                                config.brightDisplayColor = color
                                viewModel.updateBackgroundColor(color)
                            } else {
                                viewModel.updateBackgroundColor(config.brightDisplayColor)
                            }
                        }
                    },
                    timerVisible = timerVisible,
                    timerText = timerText,
                    onTimerClosePress = {
                        stopSleepTimer()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(true)
        requestedOrientation = if (config.forcePortraitMode) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(false)
    }

    private fun toggleBrightness(increase: Boolean) {
        val layout = window.attributes
        layout.screenBrightness = (if (increase) 1 else 0).toFloat()
        window.attributes = layout
    }

    private fun stopSleepTimer() {
        viewModel.hideTimer()
        stopSleepTimerCountDown()
    }


    internal class BrightDisplayViewModel(
        application: Application
    ) : AndroidViewModel(application) {


        private val _timerText: MutableStateFlow<String> = MutableStateFlow("00:00")
        val timerText = _timerText.asStateFlow()

        private val _timerVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val timerVisible = _timerVisible.asStateFlow()

        private val _backgroundColor: MutableStateFlow<Int> = MutableStateFlow(application.config.brightDisplayColor)
        val backgroundColor = _backgroundColor.asStateFlow()

        init {
            EventBus.getDefault().register(this)
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        fun sleepTimerChanged(event: Events.SleepTimerChanged) {
            _timerText.value = event.seconds.getFormattedDuration()
            _timerVisible.value = true

            if (event.seconds == 0) {
                exitProcess(0)
            }
        }

        fun updateBackgroundColor(color: Int) {
            _backgroundColor.value = color
        }

        fun hideTimer() {
            _timerVisible.value = false
        }

        override fun onCleared() {
            super.onCleared()
            EventBus.getDefault().unregister(this)
        }
    }
}
