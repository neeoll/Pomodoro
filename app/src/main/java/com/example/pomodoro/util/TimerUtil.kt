package com.example.pomodoro.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.pomodoro.MainActivity

class TimerUtil {
    companion object {

        private const val TIMER_LENGTH_ID = "com.example.pomodoro.timer_length"
        private const val POMODORO_TIMER_LENGTH_ID = "com.example.pomodoro.pomodoro_timer_length"
        private const val SHORT_TIMER_LENGTH_ID = "com.example.pomodoro.short_timer_length"
        private const val LONG_TIMER_LENGTH_ID = "com.example.pomodoro.long_timer_length"

        fun getTimerLengthOrdinal(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(TIMER_LENGTH_ID, 0)
        }

        fun getTimerLength(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return when (preferences.getInt(TIMER_LENGTH_ID, 0)) {
                0 -> preferences.getInt(POMODORO_TIMER_LENGTH_ID, 25)
                1 -> preferences.getInt(SHORT_TIMER_LENGTH_ID, 5)
                2 -> preferences.getInt(LONG_TIMER_LENGTH_ID, 30)
                else -> 25
            }
        }

        fun setTimerLength(state: MainActivity.TimerState, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_LENGTH_ID, ordinal)
            editor.apply()
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.example.pomodoro.previous_timer_length"

        fun getPreviousTimerLengthSeconds(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.example.pomodoro.timer_state"

        fun getTimerState(context: Context): MainActivity.TimerStatus {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerStatus.values()[ordinal]
        }

        fun setTimerState(status: MainActivity.TimerStatus, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = status.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID = "com.example.pomodoro.previous_timer_length"

        fun getSecondsRemaining(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        private const val ALARM_SET_TIME_ID = "com.example.pomodoro.background_time"

        fun getAlarmSetTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmSetTime(time: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }
    }
}