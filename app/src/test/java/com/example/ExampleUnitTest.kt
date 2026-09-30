package com.example

import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.OverrideEntity
import com.example.data.model.Semester
import com.example.domain.ScheduleLogic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun testScheduleLogic_semesterWeekCalculation() {
        val semester = Semester(name = "Fall 2026", startDate = "2026-09-05", endDate = "2026-12-31")
        val week = ScheduleLogic.getSemesterWeek(LocalDate.of(2026, 9, 29), semester)
        assertEquals(4, week)
    }

    @Test
    fun testScheduleLogic_october3SemesterStart() {
        val semester = Semester(name = "Fall 2026", startDate = "2026-10-03", endDate = "2027-02-28")
        // September 30 is before semester start
        val preWeek = ScheduleLogic.getSemesterWeek(LocalDate.of(2026, 9, 30), semester)
        assertEquals(0, preWeek)

        // October 3 is day 1 of week 1
        val week1 = ScheduleLogic.getSemesterWeek(LocalDate.of(2026, 10, 3), semester)
        assertEquals(1, week1)

        // October 10 is day 1 of week 2
        val week2 = ScheduleLogic.getSemesterWeek(LocalDate.of(2026, 10, 10), semester)
        assertEquals(2, week2)
    }

    @Test
    fun testScheduleLogic_daySchedule_roomOverride() {
        val semester = Semester(name = "Fall 2026", startDate = "2026-09-05", endDate = "2026-12-31")
        val courses = listOf(
            CourseEntity("BOT 108", "Crop Botany Lab", "C", 1.0, "lab", "Dr. Syada Nizer Sultana", "BSAg_DSNS")
        )
        val slots = listOf(
            ClassSlotEntity("tue_1", "TUE", "10:40", "11:40", "BOT 108", "Lab 2")
        )
        val overrides = listOf(
            OverrideEntity(
                id = "ov1",
                slotId = "tue_1",
                courseCode = "BOT 108",
                changeType = "room",
                scope = "day",
                date = "2026-09-29",
                newRoom = "AGRIlab4",
                note = "Relocated"
            )
        )

        val result = ScheduleLogic.getDaySchedule(
            dateStr = "2026-09-29",
            semester = semester,
            courses = courses,
            slots = slots,
            overrides = overrides,
            exams = emptyList(),
            holidays = emptyList(),
            attendances = emptyList()
        )

        assertEquals(1, result.classes.size)
        val cls = result.classes[0]
        assertEquals("AGRIlab4", cls.room)
        assertEquals("Lab 2", cls.originalRoom)
        assertEquals("room", cls.changeType)
    }
}
