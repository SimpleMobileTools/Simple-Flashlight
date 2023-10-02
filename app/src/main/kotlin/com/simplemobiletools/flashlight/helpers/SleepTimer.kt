package com.simplemobiletools.flashlight.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.simplemobiletools.commons.helpers.isSPlus
import com.simplemobiletools.flashlight.extensions.config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

object SleepTimer {
    private var sleepTimer: CountDownTimer? = null
    private val _timeLeft = MutableSharedFlow<Int>()
    val timeLeft = _timeLeft.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun cancel() {
        sleepTimer?.cancel()
        sleepTimer = null
    }

    context(Context)
    fun startTimer() {
        val millisInFuture = config.sleepInTS - System.currentTimeMillis() + 1000L
        sleepTimer?.cancel()
        sleepTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                scope.launch {
                    _timeLeft.emit(seconds)
                }
            }

            override fun onFinish() {
                config.sleepInTS = 0
                scope.launch {
                    _timeLeft.emit(0)
                }
                stopSleepTimerCountDown()
                exitProcess(0)
            }
        }

        sleepTimer?.start()
    }
}

internal fun Context.startSleepTimerCountDown() {
    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).apply {
        if (!isSPlus() || canScheduleExactAlarms()) {
            setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                config.sleepInTS,
                getShutDownPendingIntent()
            )
        } else {
            setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                config.sleepInTS,
                getShutDownPendingIntent()
            )
        }
    }
    SleepTimer.startTimer()
}

internal fun Context.stopSleepTimerCountDown() {
    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getShutDownPendingIntent())
    SleepTimer.cancel()
    config.sleepInTS = 0
}

internal fun Context.getShutDownPendingIntent() =
    PendingIntent.getBroadcast(this, 0, Intent(this, ShutDownReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
