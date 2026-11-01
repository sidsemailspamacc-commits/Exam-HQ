package com.examhq.alarms

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build

object Notifications {
    const val CHANNEL = "exam_hq_alarms"
    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Exam HQ alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Task pre-alarms and full-screen study alarms"
                setBypassDnd(false)
            })
        }
    }
    fun alarmNotification(context: Context, intent: Intent): Notification {
        ensure(context)
        val screen = Intent(context, AlarmActivity::class.java).apply { putExtras(intent) }
        val full = PendingIntent.getActivity(context, 9201, screen, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Exam HQ · ${intent.getStringExtra("subject") ?: "Study task"}")
            .setContentText(intent.getStringExtra("topic") ?: "Your scheduled study task")
            .setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true).setAutoCancel(false).setFullScreenIntent(full, true).setContentIntent(full).build()
    }
    fun showPreAlarm(context: Context, intent: Intent) {
        ensure(context)
        val open = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 9202, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(context, CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Exam HQ · 5-minute warning")
            .setContentText("${intent.getStringExtra("subject")}: ${intent.getStringExtra("chapter")} — ${intent.getStringExtra("topic")}")
            .setContentIntent(pi).setAutoCancel(true).setCategory(Notification.CATEGORY_ALARM).build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(intent.getStringExtra("task_id").hashCode(), n)
    }
}
