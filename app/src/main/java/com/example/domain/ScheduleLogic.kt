package com.example.domain

import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.ExamEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.OverrideEntity
import com.example.data.model.Semester
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class EffectiveClass(
    val slotId: String,
    val courseCode: String,
    val courseTitle: String,
    val section: String,
    val credits: Double,
    val type: String, // "theory" | "lab"
    val instructorName: String,
    val instructorShortCode: String,
    val day: String,
    val start: String, // "HH:mm"
    val end: String,   // "HH:mm"
    val originalStart: String? = null,
    val originalEnd: String? = null,
    val room: String,
    val originalRoom: String? = null,
    val changeType: String? = null, // "room" | "time" | "cancelled" | "extra" | "swap"
    val overrideNote: String? = null,
    val overrideId: String? = null,
    val attendanceStatus: String? = null, // "present" | "absent" | null
    val liveStatus: ClassLiveStatus = ClassLiveStatus.SCHEDULED,
    val progressPercent: Float = 0f,
    val minutesLeft: Long = 0,
    val isDoubleBlock: Boolean = false
)

enum class ClassLiveStatus {
    LIVE,
    UP_NEXT,
    SCHEDULED,
    ENDED
}

data class DayScheduleResult(
    val date: String,
    val localDate: LocalDate,
    val weekday: String, // "SAT", "SUN", etc.
    val holiday: HolidayEntity? = null,
    val exams: List<ExamEntity> = emptyList(),
    val classes: List<EffectiveClass> = emptyList(),
    val isFreeDay: Boolean = false,
    val studyGap: StudyGapInfo? = null,
    val weekNumber: Int = 1,
    val dayOfWeekNumber: Int = 1 // 1=SAT .. 7=FRI
)

data class StudyGapInfo(
    val title: String,
    val subtitle: String,
    val durationText: String
)

object ScheduleLogic {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, dateFormatter)
    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    // Maps Java DayOfWeek to Saturday-first Weekday String
    fun getAppWeekday(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> "SAT"
            DayOfWeek.SUNDAY -> "SUN"
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
        }
    }

    // Week day number where SAT = 1, SUN = 2, ... FRI = 7
    fun getSatBasedDayNumber(date: LocalDate): Int {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> 1
            DayOfWeek.SUNDAY -> 2
            DayOfWeek.MONDAY -> 3
            DayOfWeek.TUESDAY -> 4
            DayOfWeek.WEDNESDAY -> 5
            DayOfWeek.THURSDAY -> 6
            DayOfWeek.FRIDAY -> 7
        }
    }

    // Calculates semester week starting Saturday
    fun getSemesterWeek(date: LocalDate, semester: Semester): Int {
        return try {
            val start = parseDate(semester.startDate)
            if (date.isBefore(start)) return 0
            val daysBetween = ChronoUnit.DAYS.between(start, date)
            (daysBetween / 7).toInt() + 1
        } catch (_: Exception) {
            1
        }
    }

    // Formats 24h "HH:mm" to 12h "hh:mm AM/PM"
    fun format12Hour(time24: String): String {
        return try {
            val parts = time24.split(":")
            val hour = parts[0].toInt()
            val min = parts[1].toInt()
            val ampm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format(Locale.US, "%02d:%02d %s", displayHour, min, ampm)
        } catch (_: Exception) {
            time24
        }
    }

    // Formats time range e.g. "10:40 AM – 11:40 AM"
    fun formatTimeRange(start: String, end: String): String {
        return "${format12Hour(start)} – ${format12Hour(end)}"
    }

    fun getDaySchedule(
        dateStr: String,
        semester: Semester,
        courses: List<CourseEntity>,
        slots: List<ClassSlotEntity>,
        overrides: List<OverrideEntity>,
        exams: List<ExamEntity>,
        holidays: List<HolidayEntity>,
        attendances: List<AttendanceEntity>,
        currentTime: LocalTime = LocalTime.now(),
        isTodayDate: Boolean = false
    ): DayScheduleResult {
        val targetDate = try { parseDate(dateStr) } catch (_: Exception) { LocalDate.now() }
        val semesterStart = try { parseDate(semester.startDate) } catch (_: Exception) { LocalDate.MIN }
        val semesterEnd = try { parseDate(semester.endDate) } catch (_: Exception) { LocalDate.MAX }

        val weekDay = getAppWeekday(targetDate)
        val dayOfWeekNum = getSatBasedDayNumber(targetDate)
        val weekNumber = getSemesterWeek(targetDate, semester)

        // Holiday check
        val holiday = holidays.find { h ->
            try {
                val s = parseDate(h.startDate)
                val e = parseDate(h.endDate)
                !targetDate.isBefore(s) && !targetDate.isAfter(e)
            } catch (_: Exception) {
                false
            }
        }

        // Exams on this date
        val dayExams = exams.filter { it.date == dateStr }

        // Holidays cancel classes for the day
        if (holiday != null) {
            return DayScheduleResult(
                date = dateStr,
                localDate = targetDate,
                weekday = weekDay,
                holiday = holiday,
                exams = dayExams,
                classes = emptyList(),
                isFreeDay = true,
                studyGap = null,
                weekNumber = weekNumber,
                dayOfWeekNumber = dayOfWeekNum
            )
        }

        val courseMap = courses.associateBy { it.code }
        val attendanceMap = attendances.filter { it.date == dateStr }.associateBy { it.slotId }

        // Start from weekly slots for that weekday
        val daySlots = slots.filter { it.day.equals(weekDay, ignoreCase = true) }

        // Build effective classes
        val effectiveClasses = mutableListOf<EffectiveClass>()

        for (slot in daySlots) {
            // Find applicable overrides
            // Date-specific overrides win over broader ones ("day" > "future" > "semester")
            val slotOverrides = overrides.filter { it.slotId == slot.id }
            val applicableOverride = slotOverrides.find { it.scope == "day" && it.date == dateStr }
                ?: slotOverrides.find { o ->
                    o.scope == "future" && o.fromDate != null && !targetDate.isBefore(parseDate(o.fromDate))
                }
                ?: slotOverrides.find { it.scope == "semester" }

            if (applicableOverride != null && applicableOverride.changeType == "cancelled") {
                // Class is cancelled
                continue
            }

            val effectiveRoom = applicableOverride?.newRoom ?: slot.room
            val effectiveStart = applicableOverride?.newStart ?: slot.start
            val effectiveEnd = applicableOverride?.newEnd ?: slot.end
            val changeType = applicableOverride?.changeType

            val course = courseMap[slot.courseCode] ?: CourseEntity(
                code = slot.courseCode,
                title = slot.courseCode,
                section = "",
                credits = 3.0,
                type = "theory",
                instructorName = "",
                instructorShortCode = ""
            )

            val att = attendanceMap[slot.id]?.status

            // Calculate live status if today
            val (status, progress, minLeft) = if (isTodayDate) {
                calculateLiveStatus(effectiveStart, effectiveEnd, currentTime)
            } else {
                Triple(ClassLiveStatus.SCHEDULED, 0f, 0L)
            }

            effectiveClasses.add(
                EffectiveClass(
                    slotId = slot.id,
                    courseCode = course.code,
                    courseTitle = course.title,
                    section = course.section,
                    credits = course.credits,
                    type = course.type,
                    instructorName = course.instructorName,
                    instructorShortCode = course.instructorShortCode,
                    day = weekDay,
                    start = effectiveStart,
                    end = effectiveEnd,
                    originalStart = if (effectiveStart != slot.start) slot.start else null,
                    originalEnd = if (effectiveEnd != slot.end) slot.end else null,
                    room = effectiveRoom,
                    originalRoom = if (effectiveRoom != slot.room) slot.room else null,
                    changeType = changeType,
                    overrideNote = applicableOverride?.note,
                    overrideId = applicableOverride?.id,
                    attendanceStatus = att,
                    liveStatus = status,
                    progressPercent = progress,
                    minutesLeft = minLeft
                )
            )
        }

        // Add extra class overrides for this date
        val extraOverrides = overrides.filter { o ->
            o.changeType == "extra" && (
                (o.scope == "day" && o.date == dateStr) ||
                (o.scope == "future" && o.fromDate != null && !targetDate.isBefore(parseDate(o.fromDate))) ||
                (o.scope == "semester")
            )
        }

        for (extra in extraOverrides) {
            val course = courseMap[extra.courseCode] ?: CourseEntity(
                code = extra.courseCode,
                title = extra.courseCode,
                section = "",
                credits = 3.0,
                type = "theory",
                instructorName = "",
                instructorShortCode = ""
            )
            val start = extra.newStart ?: "10:00"
            val end = extra.newEnd ?: "11:00"
            val att = attendanceMap[extra.id]?.status

            val (status, progress, minLeft) = if (isTodayDate) {
                calculateLiveStatus(start, end, currentTime)
            } else {
                Triple(ClassLiveStatus.SCHEDULED, 0f, 0L)
            }

            effectiveClasses.add(
                EffectiveClass(
                    slotId = extra.id,
                    courseCode = course.code,
                    courseTitle = course.title,
                    section = course.section,
                    credits = course.credits,
                    type = course.type,
                    instructorName = course.instructorName,
                    instructorShortCode = course.instructorShortCode,
                    day = weekDay,
                    start = start,
                    end = end,
                    room = extra.newRoom ?: "TBD",
                    changeType = "extra",
                    overrideNote = extra.note,
                    overrideId = extra.id,
                    attendanceStatus = att,
                    liveStatus = status,
                    progressPercent = progress,
                    minutesLeft = minLeft
                )
            )
        }

        // Sort by start time
        effectiveClasses.sortBy { it.start }

        // Detect consecutive double blocks of same course (e.g. BOT 108 Part I and Part II)
        val markedClasses = effectiveClasses.mapIndexed { index, cls ->
            val prevSame = index > 0 && effectiveClasses[index - 1].courseCode == cls.courseCode
            val nextSame = index < effectiveClasses.size - 1 && effectiveClasses[index + 1].courseCode == cls.courseCode
            cls.copy(isDoubleBlock = prevSame || nextSame)
        }

        // Calculate study gap between classes
        val studyGap = calculateStudyGap(markedClasses)

        val isFreeDay = (weekDay == "THU" || weekDay == "FRI") && markedClasses.isEmpty() && dayExams.isEmpty()

        return DayScheduleResult(
            date = dateStr,
            localDate = targetDate,
            weekday = weekDay,
            holiday = null,
            exams = dayExams,
            classes = markedClasses,
            isFreeDay = isFreeDay,
            studyGap = studyGap,
            weekNumber = weekNumber,
            dayOfWeekNumber = dayOfWeekNum
        )
    }

    private fun calculateLiveStatus(startStr: String, endStr: String, now: LocalTime): Triple<ClassLiveStatus, Float, Long> {
        return try {
            val start = LocalTime.parse(startStr)
            val end = LocalTime.parse(endStr)

            if (now.isBefore(start)) {
                val minsUntil = ChronoUnit.MINUTES.between(now, start)
                if (minsUntil in 0..15) {
                    Triple(ClassLiveStatus.UP_NEXT, 0f, minsUntil)
                } else {
                    Triple(ClassLiveStatus.SCHEDULED, 0f, minsUntil)
                }
            } else if (!now.isAfter(end)) {
                val totalMins = ChronoUnit.MINUTES.between(start, end).coerceAtLeast(1)
                val elapsedMins = ChronoUnit.MINUTES.between(start, now)
                val remainingMins = ChronoUnit.MINUTES.between(now, end).coerceAtLeast(0)
                val pct = (elapsedMins.toFloat() / totalMins.toFloat()).coerceIn(0f, 1f)
                Triple(ClassLiveStatus.LIVE, pct, remainingMins)
            } else {
                Triple(ClassLiveStatus.ENDED, 1f, 0L)
            }
        } catch (_: Exception) {
            Triple(ClassLiveStatus.SCHEDULED, 0f, 0L)
        }
    }

    private fun calculateStudyGap(classes: List<EffectiveClass>): StudyGapInfo? {
        if (classes.size < 2) return null
        for (i in 0 until classes.size - 1) {
            try {
                val endFirst = LocalTime.parse(classes[i].end)
                val startSecond = LocalTime.parse(classes[i + 1].start)
                val diffMins = ChronoUnit.MINUTES.between(endFirst, startSecond)
                if (diffMins >= 45) {
                    val hrs = diffMins / 60
                    val mins = diffMins % 60
                    val durationText = if (hrs > 0 && mins > 0) "${hrs}h ${mins}m Study Gap" else if (hrs > 0) "${hrs}h Study Gap" else "${mins}m Study Gap"
                    val rangeText = "${format12Hour(classes[i].end)} – ${format12Hour(classes[i + 1].start)} • Central AGRI Library open"
                    return StudyGapInfo(
                        title = durationText,
                        subtitle = rangeText,
                        durationText = if (classes[i].type == "lab") "Post-Lab" else "Break"
                    )
                }
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }
        return null
    }

    data class CourseAttendanceStat(
        val courseCode: String,
        val courseTitle: String,
        val section: String,
        val credits: Double,
        val type: String,
        val instructorName: String,
        val instructorShortCode: String,
        val heldCount: Int,
        val attendedCount: Int,
        val absentCount: Int,
        val percentage: Float,
        val isSafe: Boolean,
        val neededConsecutiveClasses: Int,
        val sessions: List<PastSessionRecord>
    )

    data class PastSessionRecord(
        val date: String,
        val slotId: String,
        val status: String,
        val day: String,
        val timeRange: String,
        val room: String
    )

    fun calculateAttendanceStats(
        semester: Semester,
        courses: List<CourseEntity>,
        slots: List<ClassSlotEntity>,
        overrides: List<OverrideEntity>,
        holidays: List<HolidayEntity>,
        attendances: List<AttendanceEntity>,
        referenceDate: LocalDate = LocalDate.now()
    ): Pair<OverallAttendanceStat, List<CourseAttendanceStat>> {
        val courseStats = mutableMapOf<String, MutableCourseAccumulator>()
        courses.forEach { c ->
            courseStats[c.code] = MutableCourseAccumulator(c)
        }

        val semesterStart = try { parseDate(semester.startDate) } catch (_: Exception) { referenceDate }
        if (referenceDate.isBefore(semesterStart)) {
            val courseList = courses.map { c ->
                CourseAttendanceStat(
                    courseCode = c.code,
                    courseTitle = c.title,
                    section = c.section,
                    credits = c.credits,
                    type = c.type,
                    instructorName = c.instructorName,
                    instructorShortCode = c.instructorShortCode,
                    heldCount = 0,
                    attendedCount = 0,
                    absentCount = 0,
                    percentage = 100f,
                    isSafe = true,
                    neededConsecutiveClasses = 0,
                    sessions = emptyList()
                )
            }
            return Pair(
                OverallAttendanceStat(
                    totalHeld = 0,
                    totalAttended = 0,
                    totalAbsent = 0,
                    percentage = 100f,
                    isEligible = true
                ),
                courseList
            )
        }

        val startDate = semesterStart

        val attMap = attendances.associateBy { "${it.date}|${it.slotId}" }

        // Iterate through all days from semester start up to reference date
        var cur = startDate
        while (!cur.isAfter(referenceDate)) {
            val dateStr = formatDate(cur)
            val weekDay = getAppWeekday(cur)

            // Check if holiday
            val isHoliday = holidays.any { h ->
                try {
                    val s = parseDate(h.startDate)
                    val e = parseDate(h.endDate)
                    !cur.isBefore(s) && !cur.isAfter(e)
                } catch (_: Exception) { false }
            }

            if (!isHoliday) {
                val daySlots = slots.filter { it.day.equals(weekDay, ignoreCase = true) }
                for (slot in daySlots) {
                    val override = overrides.find { it.slotId == slot.id && it.date == dateStr }
                        ?: overrides.find { it.slotId == slot.id && it.scope == "semester" }

                    if (override?.changeType == "cancelled") {
                        continue
                    }

                    val key = "$dateStr|${slot.id}"
                    val att = attMap[key]
                    val acc = courseStats[slot.courseCode]
                    if (acc != null) {
                        val timeRange = "${format12Hour(slot.start)} - ${format12Hour(slot.end)}"
                        if (att != null && att.status == "present") {
                            acc.held++
                            acc.attended++
                            acc.sessions.add(PastSessionRecord(dateStr, slot.id, "present", weekDay, timeRange, slot.room))
                        } else if (att != null && att.status == "absent") {
                            acc.held++
                            acc.absent++
                            acc.sessions.add(PastSessionRecord(dateStr, slot.id, "absent", weekDay, timeRange, slot.room))
                        } else {
                            // Unmarked session
                            acc.sessions.add(PastSessionRecord(dateStr, slot.id, "unmarked", weekDay, timeRange, slot.room))
                        }
                    }
                }
            }

            cur = cur.plusDays(1)
        }

        val courseList = courseStats.values.map { acc ->
            val total = if (acc.held > 0) acc.held else 1
            val pct = if (acc.held > 0) ((acc.attended.toFloat() / total.toFloat()) * 100f) else 100f
            val isSafe = pct >= 75f

            // How many consecutive classes needed to reach 75%
            val needed = if (isSafe) 0 else {
                val numerator = 3 * acc.held - 4 * acc.attended
                if (numerator > 0) kotlin.math.ceil(numerator.toDouble()).toInt() else 1
            }

            CourseAttendanceStat(
                courseCode = acc.course.code,
                courseTitle = acc.course.title,
                section = acc.course.section,
                credits = acc.course.credits,
                type = acc.course.type,
                instructorName = acc.course.instructorName,
                instructorShortCode = acc.course.instructorShortCode,
                heldCount = acc.held,
                attendedCount = acc.attended,
                absentCount = acc.absent,
                percentage = pct,
                isSafe = isSafe,
                neededConsecutiveClasses = needed,
                sessions = acc.sessions.reversed()
            )
        }

        val actualHeld = courseList.sumOf { it.heldCount }
        val totalAttended = courseList.sumOf { it.attendedCount }
        val overallPct = if (actualHeld > 0) (totalAttended.toFloat() / actualHeld.toFloat()) * 100f else 0f
        val overallStat = OverallAttendanceStat(
            totalHeld = actualHeld,
            totalAttended = totalAttended,
            totalAbsent = courseList.sumOf { it.absentCount },
            percentage = overallPct,
            isEligible = actualHeld == 0 || overallPct >= 75f
        )

        return Pair(overallStat, courseList)
    }

    data class OverallAttendanceStat(
        val totalHeld: Int,
        val totalAttended: Int,
        val totalAbsent: Int,
        val percentage: Float,
        val isEligible: Boolean
    )

    private class MutableCourseAccumulator(val course: CourseEntity) {
        var held = 0
        var attended = 0
        var absent = 0
        val sessions = mutableListOf<PastSessionRecord>()
    }
}
