package com.examhq.alarms

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max

data class StudyTask(
    val id: String, val date: LocalDate, val dayLabel: String,
    val subject: String, val chapter: String, val topic: String,
    val slotName: String, val slotLabel: String, val alarmAt: LocalDateTime
)

object Planner {
    fun load(context: Context): List<StudyTask> {
        val json = context.assets.open("planner.json").bufferedReader().use { it.readText() }
        val days = JSONArray(json)
        val out = mutableListOf<StudyTask>()
        for (di in 0 until days.length()) {
            val day = days.getJSONObject(di)
            val date = LocalDate.parse(day.getString("date"))
            val label = day.getString("label")
            val items = day.getJSONArray("items")
            data class Slot(val label: String, val name: String, val start: Int, val end: Int, val tasks: MutableList<Int> = mutableListOf())
            val dow = date.dayOfWeek.value % 7
            val slots = mutableListOf<Slot>()
            val isExam = !day.isNull("exam")
            if (isExam) {
                slots += Slot("6:00–6:30 AM", "Exam-day quick revision", 6*60, 6*60+30)
                val evening = if (dow == 1 || dow == 2 || dow == 5) Slot("6:00–6:40 PM", "Next examination preparation", 18*60, 18*60+40)
                    else Slot("6:00–7:15 PM", "Next examination preparation", 18*60, 19*60+15)
                slots += evening
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    if (item.optString("kind") == "exam") slots[0].tasks += i else slots[1].tasks += i
                }
            } else {
                when (dow) {
                    1, 2, 5 -> { slots += Slot("6:00–6:40 PM", "Main board-study block", 18*60, 18*60+40); slots += Slot("10:00–10:15 PM", "Light revision (only if needed)", 22*60, 22*60+15) }
                    3, 4 -> { slots += Slot("6:00–7:15 PM", "Main board-study block", 18*60, 19*60+15); slots += Slot("7:30–8:10 PM", "NCERT questions / second subject / revision", 19*60+30, 20*60+10); slots += Slot("10:00–10:15 PM", "Light revision (only if needed)", 22*60, 22*60+15) }
                    else -> {
                        slots += Slot("6:00–7:30 AM", "Deep board study", 6*60, 7*60+30)
                        slots += Slot("9:45–11:15 AM", "Board preparation", 9*60+45, 11*60+15)
                        slots += Slot("11:15 AM–12:00 PM", "English / language practice", 11*60+15, 12*60)
                        slots += Slot("1:00–2:30 PM", "Second board-study block", 13*60, 14*60+30)
                        slots += Slot("3:00–4:00 PM", "Mistake correction and weak topics", 15*60, 16*60)
                    }
                }
                val remaining = (0 until items.length()).toMutableList()
                if (dow == 0 || dow == 6) {
                    val english = remaining.filter { items.getJSONObject(it).optString("subject") == "English" }
                    slots[2].tasks.addAll(english); remaining.removeAll(english.toSet())
                }
                val priority = when (dow) {
                    1,2,5 -> listOf("Chemistry","Mathematics","Physics","Computer Science")
                    3 -> listOf("Mathematics","Chemistry","Physics","Computer Science")
                    4 -> listOf("Chemistry","Physics","Mathematics","Computer Science")
                    6 -> listOf("Chemistry","Mathematics","Physics","Computer Science")
                    else -> listOf("Mathematics","Chemistry","Physics","Computer Science")
                }
                remaining.sortBy { priority.indexOf(items.getJSONObject(it).optString("subject")).let { rank -> if (rank < 0) 99 else rank } }
                val usable = if (dow in listOf(1,2,3,4,5)) listOf(0,1) else listOf(0,1,3,4)
                remaining.forEachIndexed { index, itemIndex -> slots[usable[index % usable.size]].tasks += itemIndex }
            }
            slots.forEach { slot ->
                val count = slot.tasks.size
                slot.tasks.forEachIndexed { position, itemIndex ->
                    val item = items.getJSONObject(itemIndex)
                    val minute = slot.start + ((slot.end - slot.start) * position / max(1, count))
                    val time = LocalTime.of(minute / 60, minute % 60)
                    val taskId = "${date}_${itemIndex}"
                    out += StudyTask(taskId, date, label, item.getString("subject"), item.getString("chapter"), item.getString("topic"), slot.name, slot.label, LocalDateTime.of(date, time))
                }
            }
        }
        return out.sortedBy { it.alarmAt }
    }
}
