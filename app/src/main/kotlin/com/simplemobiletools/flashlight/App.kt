package com.simplemobiletools.flashlight

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.simplemobiletools.commons.extensions.checkUseEnglish
import org.greenrobot.eventbus.EventBus
import com.simplemobiletools.flashlight.models.Timer
import com.simplemobiletools.flashlight.models.TimerState
import com.simplemobiletools.flashlight.helpers.TimerHelper
import com.simplemobiletools.flashlight.models.TimerEvent
import com.simplemobiletools.flashlight.extensions.timerHelper
import com.simplemobiletools.flashlight.services.TimerStopService
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.simplemobiletools.flashlight.services.*
//import androidx.lifecycle.ProcessLifecycleOwner

class App : Application() {
    private var countDownTimers = mutableMapOf<Int, CountDownTimer>()

    override fun onCreate() {
        super.onCreate()
        //ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        EventBus.getDefault().register(this)
        checkUseEnglish()
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        EventBus.getDefault().post(TimerStopService)
        timerHelper.getTimer{ timer  ->
                    EventBus.getDefault().post(TimerEvent.Start(timer.id!!, (timer.state as TimerState.Running).tick))
            }
        }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Reset) {
        updateTimerState(event.timerId, TimerState.Idle)
        countDownTimers[event.timerId]?.cancel()
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Start) {
        val countDownTimer = object : CountDownTimer(event.duration, 1000) {
            override fun onTick(tick: Long) {
                updateTimerState(event.timerId, TimerState.Running(event.duration, tick))
            }

            override fun onFinish() {
                EventBus.getDefault().post(TimerEvent.Finish(event.timerId, event.duration))
                EventBus.getDefault().post(TimerStopService)
            }
        }.start()
        countDownTimers[event.timerId] = countDownTimer
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Finish) {
        timerHelper.getTimer() { timer ->
           // val pendingIntent = getOpenTimerTabIntent(event.timerId)
            //val notification = getTimerNotification(timer, pendingIntent, false)
            //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

          /*  try {
                notificationManager.notify(event.timerId, notification)
            } catch (e: Exception) {
                showErrorToast(e)
            }*/

            updateTimerState(event.timerId, TimerState.Finished)
            /*Handler(Looper.getMainLooper()).postDelayed({
                hideNotification(event.timerId)
            }, config.timerMaxReminderSecs * 1000L)*/
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Pause) {
        timerHelper.getTimer() { timer ->
            updateTimerState(event.timerId, TimerState.Paused(event.duration, (timer.state as TimerState.Running).tick))
            countDownTimers[event.timerId]?.cancel()
        }
    }
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerStopService) {
        stopService()
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TimerEvent.Refresh) {
        /*if (!isStopping) {
            updateNotification()
        }*/}
    private fun updateTimerState(timerId: Int, state: TimerState) {
        timerHelper.getTimer() { timer ->
            val newTimer = timer.copy(state = state)
            timerHelper.insertOrUpdateTimer(newTimer) {
                EventBus.getDefault().post(TimerEvent.Refresh)
            }
        }
    }

    }

