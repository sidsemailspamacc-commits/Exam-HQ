package com.examhq.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, RingingService::class.java).apply {
            putExtras(intent); action = RingingService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= 26) context.startForegroundService(service) else context.startService(service)
    }
}

class PreAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.showPreAlarm(context, intent)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { AlarmScheduler.scheduleAll(context) }
}
