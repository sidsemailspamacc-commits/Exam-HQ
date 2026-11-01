package com.examhq.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId

object AlarmScheduler {
    private fun pi(context: Context, cls: Class<*>, task: StudyTask, offset: Int): PendingIntent {
        val intent = Intent(context, cls).apply {
            putExtra("task_id", task.id); putExtra("subject", task.subject); putExtra("chapter", task.chapter)
            putExtra("topic", task.topic); putExtra("slot", task.slotLabel); putExtra("pre_alarm", offset == -5)
        }
        val code = (task.id.hashCode() and 0x0fffffff) + if (offset == -5) 1 else 2
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    fun scheduleAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        Planner.load(context).forEach { task ->
            val at = task.alarmAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (at <= now) return@forEach
            val preAt = at - 5 * 60_000L
            if (preAt > now) set(context, am, preAt, pi(context, PreAlarmReceiver::class.java, task, -5))
            set(context, am, at, pi(context, AlarmReceiver::class.java, task, 0))
        }
    }
    private fun set(context: Context, am: AlarmManager, time: Long, alarmPi: PendingIntent) {
        val show = PendingIntent.getActivity(context, 9200, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(time, show), alarmPi)
    }
    fun scheduleSnooze(context: Context, taskId: String, subject: String, chapter: String, topic: String, minutes: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("task_id", taskId); putExtra("subject", subject); putExtra("chapter", chapter); putExtra("topic", topic); putExtra("snoozed", true)
        }
        val pi = PendingIntent.getBroadcast(context, (taskId.hashCode() and 0x0fffffff) + 7, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        set(context, am, System.currentTimeMillis() + minutes * 60_000L, pi)
    }
}
