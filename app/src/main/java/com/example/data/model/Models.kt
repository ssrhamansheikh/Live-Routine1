package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val code: String,
    val title: String,
    val section: String,
    val credits: Double,
    val type: String, // "theory" | "lab"
    val instructorName: String,
    val instructorShortCode: String
)

@Entity(tableName = "class_slots")
data class ClassSlotEntity(
    @PrimaryKey val id: String,
    val day: String, // "SAT" | "SUN" | "MON" | "TUE" | "WED" | "THU" | "FRI"
    val start: String, // "HH:mm"
    val end: String,   // "HH:mm"
    val courseCode: String,
    val room: String
)

@Entity(tableName = "overrides")
data class OverrideEntity(
    @PrimaryKey val id: String,
    val slotId: String?, // null if extra class
    val courseCode: String,
    val changeType: String, // "room" | "time" | "cancelled" | "extra" | "swap"
    val scope: String,      // "day" | "future" | "semester"
    val date: String,       // "YYYY-MM-DD"
    val fromDate: String? = null,
    val newRoom: String? = null,
    val newStart: String? = null,
    val newEnd: String? = null,
    val note: String? = null
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val courseCode: String,
    val type: String, // "Class Test" | "Midterm" | "Final" | "Practical" | "Assignment" | "Viva Voce"
    val date: String, // "YYYY-MM-DD"
    val start: String,
    val end: String,
    val hall: String,
    val syllabus: String,
    val reminder: String = "1day", // "none" | "1day" | "1hour"
    val checklistJson: String = "[]" // JSON of List<ExamChecklistItem>
)

data class ExamChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val done: Boolean = false
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startDate: String, // "YYYY-MM-DD"
    val endDate: String,   // "YYYY-MM-DD"
    val type: String,      // "Public Holiday" | "University Closed" | "Exam Break" | "Custom"
    val note: String = ""
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val key: String, // "YYYY-MM-DD|slotId"
    val date: String,
    val slotId: String,
    val status: String // "present" | "absent"
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "gap_journal")
data class GapJournalEntity(
    @PrimaryKey val id: String, // "date_startTime"
    val date: String,          // "YYYY-MM-DD"
    val startTime: String,     // "11:45"
    val endTime: String,       // "15:20"
    val durationMinutes: Int,  // e.g. 215
    val tag: String,           // "Library", "Studied", etc.
    val note: String,          // user note
    val timestamp: Long = System.currentTimeMillis()
)

data class Semester(
    val name: String = "Fall 2026",
    val startDate: String = "2026-10-03",
    val endDate: String = "2027-02-28"
)
