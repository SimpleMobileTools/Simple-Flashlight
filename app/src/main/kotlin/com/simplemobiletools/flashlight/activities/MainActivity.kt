package com.simplemobiletools.flashlight.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.math.MathUtils
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.onEventValue
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.PermissionRequiredDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialogAlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.dialogs.SleepTimerCustomDialog
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.extensions.startAboutActivity
import com.simplemobiletools.flashlight.helpers.*
import com.simplemobiletools.flashlight.screens.*
import com.simplemobiletools.flashlight.views.AnimatedSleepTimer
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    companion object {
        private const val MAX_STROBO_DELAY = 2000L
        private const val MIN_STROBO_DELAY = 10L
    }

    private val preferences by lazy { config }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLaunched(BuildConfig.APPLICATION_ID)

        setContent {
            AppThemeSurface {
                val showMoreApps = onEventValue { !resources.getBoolean(R.bool.hide_google_relations) }
                val sosPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        if (it) {
                            cameraPermissionGranted(true)
                        } else {
                            toast(R.string.camera_permission)
                        }
                    }
                )
                val stroboscopePermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        if (it) {
                            cameraPermissionGranted(false)
                        } else {
                            toast(R.string.camera_permission)
                        }
                    }
                )

                val sleepTimerDialogState = rememberAlertDialogState().apply {
                    DialogMember {
                        SleepTimerRadioDialog(alertDialogState = this)
                    }
                }

                MainScreen(
                    flashlightButton = {
                        val flashlightActive by viewModel.flashlightOn.collectAsStateWithLifecycle()

                        FlashlightButton(
                            onFlashlightPress = { viewModel.toggleFlashlight() },
                            flashlightActive = flashlightActive,
                        )
                    },
                    brightDisplayButton = {
                        val showBrightDisplayButton by preferences.brightDisplayFlow.collectAsStateWithLifecycle(
                            config.brightDisplay,
                            minActiveState = Lifecycle.State.CREATED
                        )
                        if (showBrightDisplayButton) {
                            BrightDisplayButton(
                                onBrightDisplayPress = {
                                    startActivity(Intent(applicationContext, BrightDisplayActivity::class.java))
                                }
                            )
                        }
                    },
                    sosButton = {
                        val showSosButton by preferences.sosFlow.collectAsStateWithLifecycle(config.sos, minActiveState = Lifecycle.State.CREATED)
                        val sosActive by viewModel.sosActive.collectAsStateWithLifecycle()

                        if (showSosButton) {
                            SosButton(
                                sosActive = sosActive,
                                onSosButtonPress = {
                                    toggleStroboscope(true, sosPermissionLauncher)
                                },
                            )
                        }
                    },
                    stroboscopeButton = {
                        val showStroboscopeButton by preferences.stroboscopeFlow.collectAsStateWithLifecycle(
                            config.stroboscope,
                            minActiveState = Lifecycle.State.CREATED
                        )
                        val stroboscopeActive by viewModel.stroboscopeActive.collectAsStateWithLifecycle()

                        if (showStroboscopeButton) {
                            StroboscopeButton(
                                stroboscopeActive = stroboscopeActive,
                                onStroboscopeButtonPress = {
                                    toggleStroboscope(false, stroboscopePermissionLauncher)
                                },
                            )
                        }
                    },
                    slidersSection = {
                        val brightnessBarVisible by viewModel.brightnessBarVisible.collectAsStateWithLifecycle(false)
                        val brightnessBarValue by viewModel.brightnessBarValue.collectAsStateWithLifecycle()
                        val stroboscopeBarVisible by viewModel.stroboscopeBarVisible.collectAsStateWithLifecycle(false)
                        val stroboscopeBarValue by viewModel.stroboscopeBarValue.collectAsStateWithLifecycle()

                        MainScreenSlidersSection(
                            showBrightnessBar = brightnessBarVisible,
                            brightnessBarValue = brightnessBarValue,
                            onBrightnessBarValueChange = {
                                viewModel.updateBrightnessBarValue(it)
                            },
                            showStroboscopeBar = stroboscopeBarVisible,
                            stroboscopeBarValue = stroboscopeBarValue,
                            onStroboscopeBarValueChange = {
                                viewModel.updateStroboscopeBarValue(it)
                            },
                        )
                    },
                    sleepTimer = {
                        val timerVisible by viewModel.timerVisible.collectAsStateWithLifecycle()
                        val timerText by viewModel.timerText.collectAsStateWithLifecycle()

                        AnimatedSleepTimer(
                            timerText = timerText,
                            timerVisible = timerVisible,
                            onTimerClosePress = { stopSleepTimer() },
                        )
                    },
                    showMoreApps = showMoreApps,
                    openSettings = ::launchSettings,
                    openAbout = ::launchAbout,
                    openSleepTimer = {
                        showSleepTimerPermission {
                            sleepTimerDialogState.show()
                        }
                    },
                    moreAppsFromUs = ::launchMoreAppsFromUsIntent
                )
            }
        }

        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()

        requestedOrientation = if (preferences.forcePortraitMode) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR
        invalidateOptionsMenu()

        checkShortcuts()
    }

    override fun onStart() {
        super.onStart()

        if (preferences.sleepInTS == 0L) {
            viewModel.hideTimer()
            (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getShutDownPendingIntent())
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
            FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name, 0, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun checkAppOnSDCard() {
        if (!baseConfig.wasAppOnSDShown && isAppInstalledOnSDCard()) {
            baseConfig.wasAppOnSDShown = true
            ConfirmationDialog(this, "", R.string.app_on_sd_card, R.string.ok, 0) {}
        }
    }

    private fun toggleStroboscope(isSOS: Boolean, launcher: ManagedActivityResultLauncher<String, Boolean>) {
        // use the old Camera API for stroboscope, the new Camera Manager is way too slow
        if (isNougatPlus()) {
            cameraPermissionGranted(isSOS)
        } else {
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted(isSOS)
            } else {
                launcher.launch(permission)
            }
        }
    }

    private fun cameraPermissionGranted(isSOS: Boolean) {
        if (isSOS) {
            viewModel.enableSos()
        } else {
            viewModel.enableStroboscope()
        }
    }

    @Composable
    private fun SleepTimerRadioDialog(
        alertDialogState: AlertDialogState
    ) {
        val items = ArrayList(listOf(10, 30, 60, 5 * 60, 10 * 60, 30 * 60).map {
            RadioItem(it, secondsToString(it))
        })

        if (items.none { it.id == preferences.lastSleepTimerSeconds }) {
            items.add(RadioItem(preferences.lastSleepTimerSeconds, secondsToString(config.lastSleepTimerSeconds)))
        }

        items.sortBy { it.id }
        items.add(RadioItem(-1, getString(R.string.custom)))

        RadioGroupDialogAlertDialog(
            alertDialogState = alertDialogState,
            items = items.toImmutableList(),
            selectedItemId = preferences.lastSleepTimerSeconds,
            callback = {
                if (it as Int == -1) {
                    SleepTimerCustomDialog(this) {
                        if (it > 0) {
                            pickedSleepTimer(it)
                        }
                    }
                } else if (it > 0) {
                    pickedSleepTimer(it)
                }
            }
        )
    }

    private fun showSleepTimerPermission(callback: () -> Unit) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (isSPlus() && !alarmManager.canScheduleExactAlarms()) {
            PermissionRequiredDialog(
                this,
                com.simplemobiletools.commons.R.string.allow_alarm_sleep_timer,
                positiveActionCallback = { openRequestExactAlarmSettings(baseConfig.appId) },
                negativeActionCallback = { callback() }
            )
            return
        }
        callback()
    }

    private fun secondsToString(seconds: Int): String {
        val finalHours = seconds / 3600
        val finalMinutes = (seconds / 60) % 60
        val finalSeconds = seconds % 60
        val parts = mutableListOf<String>()
        if (finalHours != 0) {
            parts.add(resources.getQuantityString(R.plurals.hours, finalHours, finalHours))
        }
        if (finalMinutes != 0) {
            parts.add(resources.getQuantityString(R.plurals.minutes, finalMinutes, finalMinutes))
        }
        if (finalSeconds != 0) {
            parts.add(resources.getQuantityString(R.plurals.seconds, finalSeconds, finalSeconds))
        }
        return parts.joinToString(separator = " ")
    }

    private fun pickedSleepTimer(seconds: Int) {
        preferences.lastSleepTimerSeconds = seconds
        preferences.sleepInTS = System.currentTimeMillis() + seconds * 1000
        startSleepTimer()
    }

    private fun startSleepTimer() {
        viewModel.showTimer()
        startSleepTimerCountDown()
    }

    private fun stopSleepTimer() {
        viewModel.hideTimer()
        stopSleepTimerCountDown()
    }

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = preferences.appIconColor
        if (isNougatMR1Plus() && preferences.lastHandledShortcutColor != appIconColor) {
            val createNewContact = getBrightDisplayShortcut(appIconColor)

            try {
                shortcutManager.dynamicShortcuts = Arrays.asList(createNewContact)
                preferences.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getBrightDisplayShortcut(appIconColor: Int): ShortcutInfo {
        val brightDisplay = getString(R.string.bright_display)
        val drawable = resources.getDrawable(R.drawable.shortcut_bright_display)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_bright_display_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, BrightDisplayActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(this, "bright_display")
            .setShortLabel(brightDisplay)
            .setLongLabel(brightDisplay)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
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
            if (preferences.turnFlashlightOn) {
                camera.enableFlashlight()
            }
        }

        private val _timerText: MutableStateFlow<String> = MutableStateFlow("00:00")
        val timerText = _timerText.asStateFlow()

        private val _timerVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val timerVisible = _timerVisible.asStateFlow()

        val flashlightOn = camera.flashlightOnFlow

        val brightnessBarVisible = flashlightOn.map {
            it && camera.supportsBrightnessControl()
        }

        private val _sosActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val sosActive = _sosActive.asStateFlow()

        private val _brightnessBarValue: MutableStateFlow<Float> = MutableStateFlow(0f)
        val brightnessBarValue = _brightnessBarValue.asStateFlow()

        private val _stroboscopeActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val stroboscopeActive = _stroboscopeActive.asStateFlow()

        val stroboscopeBarVisible = stroboscopeActive.map { it }

        private val _stroboscopeBarValue: MutableStateFlow<Float> = MutableStateFlow(0f)
        val stroboscopeBarValue = _stroboscopeBarValue.asStateFlow()

        init {
            _stroboscopeBarValue.value = preferences.stroboscopeProgress.toFloat() / MAX_STROBO_DELAY

            SleepTimer.timeLeft
                .onEach { seconds ->
                    _timerText.value = seconds.getFormattedDuration()
                    _timerVisible.value = true

                    if (seconds == 0) {
                        exitProcess(0)
                    }
                }
                .launchIn(viewModelScope)

            MyCameraImpl.cameraError
                .onEach { getApplication<Application>().toast(R.string.camera_error) }
                .launchIn(viewModelScope)

            camera.stroboscopeDisabled
                .onEach { _stroboscopeActive.value = false }
                .launchIn(viewModelScope)

            camera.sosDisabled
                .onEach { _sosActive.value = false }
                .launchIn(viewModelScope)
        }

        fun hideTimer() {
            _timerVisible.value = false
        }

        fun showTimer() {
            _timerVisible.value = true
        }

        fun updateBrightnessBarValue(newValue: Float) {
            _brightnessBarValue.value = newValue
            val max = camera.getMaximumBrightnessLevel()
            val min = MIN_BRIGHTNESS_LEVEL
            val newLevel = MathUtils.lerp(min.toFloat(), max.toFloat(), newValue)
            camera.updateBrightnessLevel(newLevel.toInt())
            preferences.brightnessLevel = newLevel.toInt()
        }

        fun updateStroboscopeBarValue(newValue: Float) {
            _stroboscopeBarValue.value = newValue
            val max = MAX_STROBO_DELAY
            val min = MIN_STROBO_DELAY
            val newLevel = MathUtils.lerp(min.toFloat(), max.toFloat(), 1 - newValue)
            camera.stroboFrequency = newLevel.toLong()
            preferences.stroboscopeFrequency = newLevel.toLong()
            preferences.stroboscopeProgress = ((1 - newLevel) * MAX_STROBO_DELAY).toInt()
        }

        fun onResume() {
            camera.handleCameraSetup()

            if (preferences.turnFlashlightOn) {
                camera.enableFlashlight()
            }

            if (!preferences.stroboscope && _stroboscopeActive.value) {
                camera.stopStroboscope()
            }

            if (!preferences.sos && _sosActive.value) {
                camera.stopSOS()
            }
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

        fun enableSos() {
            _sosActive.value = camera.toggleSOS()
        }

        fun enableStroboscope() {
            _stroboscopeActive.value = camera.toggleStroboscope()
        }
    }
}
