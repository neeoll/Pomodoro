package com.example.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pomodoro.util.TimerUtil

class TimerExpiredReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationUtil.showTimerExpired(context)

        TimerUtil.setTimerState(MainActivity.TimerStatus.STOPPED, context)
        TimerUtil.setAlarmSetTime(0, context)
    }
}