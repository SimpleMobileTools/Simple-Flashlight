package com.simplemobiletools.flashlight.activities

import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.math.MathUtils
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.*
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.*
import com.simplemobiletools.flashlight.screens.*
import kotlinx.coroutines.flow.*

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                MainScreen(
                    flashlightButton = {
                        val flashlightActive by viewModel.flashlightOn.collectAsStateWithLifecycle()

                        FlashlightButton(
                            onFlashlightPress = { viewModel.toggleFlashlight() },
                            flashlightActive = flashlightActive,
                        )
                    },
                    brightnessSlider = {
                        val brightnessBarVisible by viewModel.brightnessBarVisible.collectAsStateWithLifecycle()
                        val brightnessBarValue by viewModel.brightnessBarValue.collectAsStateWithLifecycle()

                        BrightnessSlider(
                            visible = brightnessBarVisible,
                            value = brightnessBarValue,
                            onValueChange = viewModel::updateBrightnessBarValue,
                        )
                    },
                )

                AppLaunched()
                CheckAppOnSdCard()
            }
        }
    }

    @Composable
    private fun AppLaunched(
        donateAlertDialogState: AlertDialogState = getDonateAlertDialogState(),
        rateStarsAlertDialogState: AlertDialogState = getRateStarsAlertDialogState(),
    ) {
        LaunchedEffect(Unit) {
            appLaunchedCompose(
                appId = BuildConfig.APPLICATION_ID,
                showDonateDialog = donateAlertDialogState::show,
                showRateUsDialog = rateStarsAlertDialogState::show,
                showUpgradeDialog = {}
            )
        }
    }

    @Composable
    private fun getDonateAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                DonateAlertDialog(alertDialogState = this)
            }
        }

    @Composable
    private fun getRateStarsAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            RateStarsAlertDialog(alertDialogState = this, onRating = ::rateStarsRedirectAndThankYou)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    internal class MainViewModel(
        application: Application
    ) : AndroidViewModel(application) {

        private val preferences = application.config

        private lateinit var camera: MyCameraImpl

        init {
            camera = MyCameraImpl.newInstance(application, object : CameraTorchListener {
                override fun onTorchEnabled(isEnabled: Boolean) {
                    camera.onTorchEnabled(isEnabled)
                    if (isEnabled && camera.supportsBrightnessControl()) {
                        _brightnessBarValue.value = camera.getCurrentBrightnessLevel().toFloat() / camera.getMaximumBrightnessLevel()
                    }
                }

                override fun onTorchUnavailable() {
                    camera.onCameraNotAvailable()
                }
            })
        }

        val flashlightOn = camera.flashlightOnFlow

        val brightnessBarVisible = flashlightOn.map {
            it && camera.supportsBrightnessControl()
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)

        private val _brightnessBarValue: MutableStateFlow<Float> = MutableStateFlow(1f)
        val brightnessBarValue = _brightnessBarValue.asStateFlow()

        init {
            MyCameraImpl.cameraError
                .onEach { getApplication<Application>().toast(R.string.camera_error) }
                .launchIn(viewModelScope)
        }

        fun updateBrightnessBarValue(newValue: Float) {
            _brightnessBarValue.value = newValue
            val max = camera.getMaximumBrightnessLevel()
            val min = MIN_BRIGHTNESS_LEVEL
            val newLevel = MathUtils.lerp(min.toFloat(), max.toFloat(), newValue)
            camera.updateBrightnessLevel(newLevel.toInt())
            preferences.brightnessLevel = newLevel.toInt()
        }

        fun onResume() {
            camera.handleCameraSetup()
        }

        override fun onCleared() {
            super.onCleared()
            releaseCamera()
        }

        fun toggleFlashlight() {
            camera.toggleFlashlight()
        }

        private fun releaseCamera() {
            camera.releaseCamera()
        }
    }
}
