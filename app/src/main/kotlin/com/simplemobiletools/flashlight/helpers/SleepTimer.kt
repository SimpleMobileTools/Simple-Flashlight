package com.simplemobiletools.flashlight.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.simplemobiletools.commons.helpers.isSPlus
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.models.Events
import org.greenrobot.eventbus.EventBus
import kotlin.system.exitProcess

private var isActive = false
private var sleepTimer: CountDownTimer? = null

internal fun Context.toggleSleepTimer() {
    if (isActive) {
        stopSleepTimerCountDown()
    } else {
        startSleepTimerCountDown()
    }
}

internal fun Context.startSleepTimerCountDown() {
    val millisInFuture = config.sleepInTS - System.currentTimeMillis() + 1000L
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
    sleepTimer?.cancel()
    sleepTimer = object : CountDownTimer(millisInFuture, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds = (millisUntilFinished / 1000).toInt()
            EventBus.getDefault().post(Events.SleepTimerChanged(seconds))
        }

        override fun onFinish() {
            config.sleepInTS = 0
            EventBus.getDefault().post(Events.SleepTimerChanged(0))
            stopSleepTimerCountDown()
            exitProcess(0)
        }
    }

    sleepTimer?.start()
    isActive = true
}

internal fun Context.stopSleepTimerCountDown() {
    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getShutDownPendingIntent())
    sleepTimer?.cancel()
    sleepTimer = null
    isActive = false
    config.sleepInTS = 0
}

internal fun Context.getShutDownPendingIntent() =
    PendingIntent.getBroadcast(this, 0, Intent(this, ShutDownReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
