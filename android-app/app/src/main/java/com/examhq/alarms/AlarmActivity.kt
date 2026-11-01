package com.examhq.alarms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlarmActivity : Activity() {
    private var taskId = ""
    private var subject = ""
    private var chapter = ""
    private var topic = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        taskId = intent.getStringExtra("task_id") ?: "test"
        subject = intent.getStringExtra("subject") ?: "Test alarm"
        chapter = intent.getStringExtra("chapter") ?: "Alarm check"
        topic = intent.getStringExtra("topic") ?: "This is a test of Exam HQ Alarms"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(28,40,28,40); setBackgroundColor(0xff0a1020.toInt()) }
        fun line(text: String, size: Float, color: Int, bold: Boolean = false) { root.addView(TextView(this).apply { this.text=text; textSize=size; setTextColor(color); gravity=Gravity.CENTER; if (bold) setTypeface(null, 1); setPadding(0,10,0,10) }) }
        line("EXAM HQ ALARM", 14f, 0xff9cbcff.toInt(), true)
        line(subject, 30f, -1, true)
        line(chapter, 21f, 0xffcbd8f0.toInt(), true)
        line(topic, 18f, -1)
        line("Scheduled study time", 14f, 0xff9eacc5.toInt())
        val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
        listOf(5,10,15).forEach { mins -> row.addView(Button(this).apply { text="Snooze ${mins}m"; setOnClickListener { snooze(mins) } }) }
        root.addView(row)
        root.addView(Button(this).apply { text="Dismiss"; textSize=18f; setOnClickListener { dismissAlarm() } })
        setContentView(root)
    }
    private fun stopSound() { startService(Intent(this, RingingService::class.java).setAction(RingingService.ACTION_STOP)) }
    private fun snooze(minutes: Int) { stopSound(); AlarmScheduler.scheduleSnooze(this, taskId, subject, chapter, topic, minutes); finish() }
    private fun dismissAlarm() { stopSound(); finish() }
    override fun onBackPressed() { /* alarm screen stays until snoozed or dismissed */ }
}
