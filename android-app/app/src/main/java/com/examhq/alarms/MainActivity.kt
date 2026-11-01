package com.examhq.alarms

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import java.time.format.DateTimeFormatter

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private val prefs by lazy { getSharedPreferences("exam_hq_progress", Context.MODE_PRIVATE) }
    private var tasks = listOf<StudyTask>()
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); Notifications.ensure(this); build(); requestPermissionsAndSchedule() }
    override fun onResume() { super.onResume(); if (::content.isInitialized) { updateStatus(); if (canExact()) { try { AlarmScheduler.scheduleAll(this) } catch (_: Exception) {} } } }
    private fun build() {
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,22,18,30); setBackgroundColor(0xff0a1020.toInt()) }
        scroll.addView(content); setContentView(scroll)
        text("EXAM HQ", 13f, 0xff9cbcff.toInt(), true)
        text("Alarms", 34f, -1, true)
        text("November 2026 · Your original planner, unchanged", 15f, 0xff9eacc5.toInt())
        val status = TextView(this).apply { tag="status"; textSize=14f; setTextColor(-1); setPadding(14,14,14,14); setBackgroundColor(0xff15233a.toInt()) }
        content.addView(status, params())
        button("Test alarm (rings in 10 seconds)") { testAlarm() }
        button("Reschedule all task alarms") { requestPermissionsAndSchedule(force=true) }
        button("Exact-alarm permission") { openAlarmSettings() }
        button("Full-screen alarm permission (Android 14+)") { openFullScreenSettings() }
        text("Your tasks", 22f, -1, true)
        tasks = Planner.load(this)
        val grouped = tasks.groupBy { it.date }
        grouped.forEach { (date, dayTasks) ->
            text(date.format(DateTimeFormatter.ofPattern("EEE, d MMM")), 17f, 0xffbcd1ff.toInt(), true)
            dayTasks.forEach { task ->
                val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(8,8,8,8) }
                val check = CheckBox(this).apply {
                    isChecked = prefs.getBoolean(task.id, false); buttonTintList = android.content.res.ColorStateList.valueOf(0xff69d6a5.toInt())
                    setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(task.id, checked).apply() }
                }
                row.addView(check)
                val details = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(6,0,0,0) }
                details.addView(TextView(this).apply { text="${task.alarmAt.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))} · ${task.subject}"; textSize=14f; setTextColor(0xff9cbcff.toInt()) })
                details.addView(TextView(this).apply { text="${task.chapter}: ${task.topic}"; textSize=13f; setTextColor(-1) })
                details.addView(TextView(this).apply { text="5-minute warning · ${task.slotName}"; textSize=11f; setTextColor(0xff9eacc5.toInt()) })
                row.addView(details, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                content.addView(row, params())
            }
        }
        text("Progress is stored on this phone. Alarm times are split evenly across each original study block, in the planner’s task order. Exam-day quick revision is assigned 6:00–6:30 AM because the source planner does not specify an exact time.", 12f, 0xff9eacc5.toInt())
        updateStatus()
    }
    private fun params() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin=7 }
    private fun text(value: String, size: Float, color: Int, bold: Boolean=false) { content.addView(TextView(this).apply { text=value; textSize=size; setTextColor(color); if(bold)setTypeface(null,1); setPadding(0,8,0,8) }, params()) }
    private fun button(label: String, action: () -> Unit) { content.addView(Button(this).apply { text=label; setOnClickListener { action() } }, params()) }
    private fun updateStatus() {
        val done = tasks.count { prefs.getBoolean(it.id,false) }
        val status = content.findViewWithTag<TextView>("status") ?: return
        status.text = "$done / ${tasks.size} tasks completed\n${if (canExact()) "Exact alarms allowed" else "Exact-alarm access needed"} · ${if (notificationsAllowed()) "Notifications allowed" else "Notifications permission needed"}"
    }
    private fun canExact(): Boolean = Build.VERSION.SDK_INT < 31 || (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    private fun notificationsAllowed(): Boolean = Build.VERSION.SDK_INT < 33 || (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()
    private fun requestPermissionsAndSchedule(force: Boolean=false) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 33)
        if (Build.VERSION.SDK_INT >= 31 && !canExact()) {
            try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } catch (_: Exception) { openAlarmSettings() }
            return
        }
        try { AlarmScheduler.scheduleAll(this); if (::content.isInitialized) updateStatus(); if(force) Toast.makeText(this,"Upcoming task alarms scheduled",Toast.LENGTH_SHORT).show() }
        catch (_: SecurityException) { Toast.makeText(this,"Allow exact alarms in Settings, then tap Reschedule",Toast.LENGTH_LONG).show() }
    }
    private fun openAlarmSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 31) Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")) else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        try { startActivity(intent) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }
    }
    private fun openFullScreenSettings() {
        if (Build.VERSION.SDK_INT >= 34) {
            try { startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))) }
            catch (_: Exception) { openAlarmSettings() }
        } else Toast.makeText(this, "This setting is only needed on Android 14 and newer", Toast.LENGTH_SHORT).show()
    }
    private fun testAlarm() {
        val i = Intent(this, AlarmReceiver::class.java).apply { putExtra("task_id","test_alarm"); putExtra("subject","Exam HQ test"); putExtra("chapter","Alarm system check"); putExtra("topic","If you can hear this, your alarm is working.") }
        val pi = android.app.PendingIntent.getBroadcast(this, 777001, i, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        try { am.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis()+10_000L, android.app.PendingIntent.getActivity(this,777002,Intent(this,MainActivity::class.java),android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)), pi); Toast.makeText(this,"Test alarm set for 10 seconds",Toast.LENGTH_SHORT).show() }
        catch (_: SecurityException) { Toast.makeText(this,"Allow exact alarms first",Toast.LENGTH_LONG).show(); openAlarmSettings() }
    }
}
