package com.example.pomodoro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.SettingsMenuBinding
import com.example.pomodoro.util.TimerUtil
import com.example.pomodoro.util.UserInterfaceUtil
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            Log.d("MAIN", "Wakeup Time: ${df.format(Date(wakeUpTime))}")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            TimerUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            TimerUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerStatus {
        STOPPED, PAUSED, RUNNING
    }

    enum class TimerState {
        POMODORO, SHORT, LONG
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerStatus = TimerStatus.STOPPED
    private var secondsRemaining = 0L

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        updateSelectedTimer()

        binding.pomodoro.setOnClickListener {
            if (timerStatus != TimerStatus.RUNNING) {
                TimerUtil.setTimerLength(TimerState.POMODORO, this)
                setNewTimerLength()
                initTimer()
                updateSelectedTimer()
            }
        }
        binding.shortBreak.setOnClickListener {
            if (timerStatus != TimerStatus.RUNNING) {
                TimerUtil.setTimerLength(TimerState.SHORT, this)
                setNewTimerLength()
                initTimer()
                updateSelectedTimer()
            }
        }
        binding.longBreak.setOnClickListener {
            if (timerStatus != TimerStatus.RUNNING) {
                TimerUtil.setTimerLength(TimerState.LONG, this)
                setNewTimerLength()
                initTimer()
                updateSelectedTimer()
            }
        }

        binding.settings.setOnClickListener {
            showCustomDialog()
        }

        binding.btnStart.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
        binding.btnStart.setOnClickListener {
            startTimer()
            timerStatus = TimerStatus.RUNNING
        }

        binding.btnPause.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
        binding.btnPause.setOnClickListener {
            if (timerStatus == TimerStatus.RUNNING) {
                timer.cancel()
                timerStatus = TimerStatus.PAUSED
            }
        }

        binding.btnStop.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
        binding.btnStop.setOnClickListener {
            if (timerStatus == TimerStatus.RUNNING) {
                timer.cancel()
                onTimerFinished()
            }
        }
    }

    private fun updateSelectedTimer() {
        when (TimerUtil.getTimerLengthOrdinal(this)) {
            TimerState.POMODORO.ordinal -> {
                binding.pomodoro.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
                binding.shortBreak.background = null
                binding.longBreak.background = null
            }
            TimerState.SHORT.ordinal -> {
                binding.pomodoro.background = null
                binding.shortBreak.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
                binding.longBreak.background = null
            }
            TimerState.LONG.ordinal -> {
                binding.pomodoro.background = null
                binding.shortBreak.background = null
                binding.longBreak.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()

        if (timerStatus == TimerStatus.RUNNING){
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime)
        }

        TimerUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        TimerUtil.setSecondsRemaining(secondsRemaining, this)
        TimerUtil.setTimerState(timerStatus, this)
    }

    private fun initTimer() {
        timerStatus = TimerUtil.getTimerState(this)
        Log.d("MAIN", "TimerStatus: $timerStatus")

        if (timerStatus == TimerStatus.STOPPED) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining = if (timerStatus == TimerStatus.RUNNING || timerStatus == TimerStatus.PAUSED) {
            TimerUtil.getSecondsRemaining(this)
        } else {
            timerLengthSeconds
        }
        Log.d("MAIN", "Seconds Remaining: $secondsRemaining")

        val alarmSetTime = TimerUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0) { secondsRemaining -= nowSeconds - alarmSetTime }

        if (secondsRemaining <= 0) {
            onTimerFinished()
        } else if (timerStatus == TimerStatus.RUNNING) {
            startTimer()
        }

        updateProgressBar()
    }

    private fun onTimerFinished() {
        timerStatus = TimerStatus.STOPPED
        setNewTimerLength()
        binding.progressBar.progress = 0
        TimerUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
        NotificationUtil.showTimerExpired(this)
        updateProgressBar()
    }

    private fun startTimer() {
        timerStatus = TimerStatus.RUNNING

        timer = object: CountDownTimer(secondsRemaining * 1000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateProgressBar()
            }

            override fun onFinish() = onTimerFinished()

        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = TimerUtil.getTimerLength(this)
        timerLengthSeconds = lengthInMinutes * 60L
        binding.progressBar.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        binding.progressBar.max = timerLengthSeconds.toInt()
    }

    private fun updateProgressBar() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinutesUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinutesUntilFinished.toString()
        binding.countdown.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr
            else "0$secondsStr"
        }"

        binding.progressBar.progress = secondsRemaining.toInt()
    }

    private fun showCustomDialog() {
        val dialogBinding: SettingsMenuBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.settings_menu,
                null,
                false
            )

        val customDialog = AlertDialog.Builder(this, 0).create()
        customDialog.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setView(dialogBinding.root)
            setCancelable(false)
        }.show()

        dialogBinding.close.setOnClickListener {
            customDialog.dismiss()
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        dialogBinding.pomodoroTime.setText(
            preferences.getInt(R.string.pomodoro_time.toString(), 25).toString())
        dialogBinding.shortBreakTime.setText(
            preferences.getInt(R.string.short_break_time.toString(), 5).toString())
        dialogBinding.longBreakTime.setText(
            preferences.getInt(R.string.long_break_time.toString(), 30).toString())

        dialogBinding.color1.setOnClickListener {
            dialogBinding.color1.setImageResource(R.drawable.ic_check)
            dialogBinding.color2.setImageResource(0)
            dialogBinding.color3.setImageResource(0)
            UserInterfaceUtil.setUiColor(R.color.salmon, this)
            applyUiChanges(dialogBinding, R.drawable.nav_item_color1)
        }
        dialogBinding.color2.setOnClickListener {
            dialogBinding.color1.setImageResource(0)
            dialogBinding.color2.setImageResource(R.drawable.ic_check)
            dialogBinding.color3.setImageResource(0)
            UserInterfaceUtil.setUiColor(R.color.sky_blue, this)
            applyUiChanges(dialogBinding, R.drawable.nav_item_color2)
        }
        dialogBinding.color3.setOnClickListener {
            dialogBinding.color1.setImageResource(0)
            dialogBinding.color2.setImageResource(0)
            dialogBinding.color3.setImageResource(R.drawable.ic_check)
            UserInterfaceUtil.setUiColor(R.color.lavender, this)
            applyUiChanges(dialogBinding, R.drawable.nav_item_color3)
        }

        dialogBinding.applySettings.setBackgroundResource(UserInterfaceUtil.getUiColor(this))
        dialogBinding.applySettings.setOnClickListener {
            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putInt("com.example.pomodoro.pomodoro_timer_length", Integer.parseInt("${dialogBinding.pomodoroTime.text}"))
            editor.putInt("com.example.pomodoro.short_timer_length", Integer.parseInt("${dialogBinding.shortBreakTime.text}"))
            editor.putInt("com.example.pomodoro.long_timer_length", Integer.parseInt("${dialogBinding.longBreakTime.text}"))
            editor.apply()
            setNewTimerLength()
            initTimer()
            customDialog.dismiss()
        }
    }

    private fun applyUiChanges(dialogBinding: SettingsMenuBinding, drawable: Int) {
        dialogBinding.applySettings.background = null
        dialogBinding.applySettings.setBackgroundResource(drawable)
        when (TimerUtil.getTimerLengthOrdinal(this)) {
            0 -> binding.pomodoro.setBackgroundResource(drawable)
            1 -> binding.shortBreak.setBackgroundResource(drawable)
            2 -> binding.longBreak.setBackgroundResource(drawable)
        }
        binding.btnStart.setBackgroundResource(drawable)
        binding.btnPause.setBackgroundResource(drawable)
        binding.btnStop.setBackgroundResource(drawable)
    }
}