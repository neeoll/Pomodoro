package com.example.pomodoro.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.pomodoro.R

class UserInterfaceUtil {
    companion object {
        private const val UI_COLOR_ID = "com.example.pomodoro.color"

        fun getUiColor(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return when (preferences.getInt(UI_COLOR_ID, R.color.salmon)) {
                R.color.salmon -> R.drawable.nav_item_color1
                R.color.sky_blue -> R.drawable.nav_item_color2
                R.color.lavender -> R.drawable.nav_item_color3
                else -> R.drawable.nav_item_color1
            }
        }

        fun setUiColor(resourceValue: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(UI_COLOR_ID, resourceValue)
            editor.apply()
        }
    }
}