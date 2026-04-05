package com.example.test1.util

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    
    companion object {
        const val WORK_TAG = "daily_reminder"
        const val PREFS_NAME = "reminder_prefs"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false)
    }
    
    fun getReminderHour(): Int {
        return prefs.getInt(KEY_REMINDER_HOUR, 20)
    }
    
    fun getReminderMinute(): Int {
        return prefs.getInt(KEY_REMINDER_MINUTE, 0)
    }
    
    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        if (enabled) {
            scheduleReminder()
        } else {
            cancelReminder()
        }
    }
    
    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
        
        if (isReminderEnabled()) {
            scheduleReminder()
        }
    }
    
    fun scheduleReminder() {
        val hour = getReminderHour()
        val minute = getReminderMinute()
        
        val currentTime = Calendar.getInstance()
        val reminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (reminderTime.before(currentTime)) {
            reminderTime.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val delay = reminderTime.timeInMillis - currentTime.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    
    fun cancelReminder() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}
