package com.prohua.liftwakeup.util

import android.R.string.cancel
import android.content.Context.ALARM_SERVICE
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Created by Deep on 2017/11/30 0030.
 */
object AlarmTimer {

    /**
     * 设置周期性闹钟
     *
     * @param context
     * @param firstTime
     * @param cycTime
     * @param action
     * @param AlarmManagerType
     * 闹钟的类型，常用的有5个值：AlarmManager.ELAPSED_REALTIME、
     * AlarmManager.ELAPSED_REALTIME_WAKEUP、AlarmManager.RTC、
     * AlarmManager.RTC_WAKEUP、AlarmManager.POWER_OFF_WAKEUP
     */
    fun setRepeatingAlarmTimer(context: Context, firstTime: Long,
                               cycTime: Long, action: String, AlarmManagerType: Int) {
        val myIntent = Intent()
        myIntent.action = action
        val sender = PendingIntent.getBroadcast(context, 0, myIntent,
                0)
        val alarm = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setRepeating(AlarmManagerType, firstTime, cycTime, sender)
    }

    /**
     * 设置定时闹钟
     *
     * @param context
     * @param firstTime
     * @param cycTime
     * @param action
     * @param AlarmManagerType
     * 闹钟的类型，常用的有5个值：AlarmManager.ELAPSED_REALTIME、
     * AlarmManager.ELAPSED_REALTIME_WAKEUP、AlarmManager.RTC、
     * AlarmManager.RTC_WAKEUP、AlarmManager.POWER_OFF_WAKEUP
     */
    fun setAlarmTimer(context: Context, cycTime: Long,
                      action: String, AlarmManagerType: Int) {
        val myIntent = Intent()
        myIntent.action = action
        val sender = PendingIntent.getBroadcast(context, 0, myIntent,
                0)
        val alarm = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.set(AlarmManagerType, cycTime, sender)
    }

    /**
     * 取消闹钟
     *
     * @param context
     * @param action
     */
    fun cancelAlarmTimer(context: Context, action: String) {
        val myIntent = Intent()
        myIntent.action = action
        val sender = PendingIntent.getBroadcast(context, 0, myIntent,
                0)
        val alarm = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(sender)
    }
}