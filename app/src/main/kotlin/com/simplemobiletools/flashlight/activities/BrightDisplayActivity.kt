package com.simplemobiletools.flashlight.activities

import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ColorPickerAlertDialog
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.helpers.isOreoMr1Plus
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.SleepTimer
import com.simplemobiletools.flashlight.helpers.stopSleepTimerCountDown
import com.simplemobiletools.flashlight.screens.BrightDisplayScreen
import com.simplemobiletools.flashlight.views.AnimatedSleepTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.system.exitProcess

class BrightDisplayActivity : ComponentActivity() {
    private val viewModel by viewModels<BrightDisplayViewModel>()
    private val preferences by lazy { config }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val brightDisplayColor by preferences.brightDisplayColorFlow.collectAsStateWithLifecycle(preferences.brightDisplayColor)
                val colorPickerDialogState = getColorPickerDialogState(brightDisplayColor)

                ScreenContent(colorPickerDialogState::show)
            }
        }

        if (isOreoMr1Plus()) {
            WindowCompat.getInsetsController(window, window.decorView.rootView)
        }
    }

    @Composable
    private fun getColorPickerDialogState(
        @ColorInt
        brightDisplayColor: Int
    ) = rememberAlertDialogState().apply {
        DialogMember {
            ColorPickerAlertDialog(
                alertDialogState = this,
                color = brightDisplayColor,
                removeDimmedBackground = true,
                onActiveColorChange = viewModel::updateBackgroundColor,
                onButtonPressed = { wasPositivePressed, color ->
                    if (wasPositivePressed) {
                        config.brightDisplayColor = color
                        viewModel.updateBackgroundColor(color)
                    } else {
                        viewModel.updateBackgroundColor(config.brightDisplayColor)
                    }
                }
            )
        }
    }

    @Composable
    private fun ScreenContent(onChangeColorButtonPress: () -> Unit) {
        val backgroundColor by viewModel.backgroundColor.collectAsStateWithLifecycle()
        val contrastColor by remember { derivedStateOf { backgroundColor.getContrastColor() } }
        val timerVisible by viewModel.timerVisible.collectAsStateWithLifecycle()
        val timerText by viewModel.timerText.collectAsStateWithLifecycle()
        BrightDisplayScreen(
            backgroundColor = backgroundColor,
            contrastColor = contrastColor,
            onChangeColorPress = onChangeColorButtonPress,
            sleepTimer = {
                AnimatedSleepTimer(timerText = timerText, timerVisible = timerVisible, onTimerClosePress = ::stopSleepTimer)
            }
        )
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
            SleepTimer.timeLeft
                .onEach { seconds ->
                    _timerText.value = seconds.getFormattedDuration()
                    _timerVisible.value = true

                    if (seconds == 0) {
                        exitProcess(0)
                    }
                }
                .launchIn(viewModelScope)
        }

        fun updateBackgroundColor(color: Int) {
            _backgroundColor.value = color
        }

        fun hideTimer() {
            _timerVisible.value = false
        }
    }
}
