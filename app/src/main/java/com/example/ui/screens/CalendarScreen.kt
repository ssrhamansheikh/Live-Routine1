package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ScheduleLogic
import com.example.ui.RoutineViewModel
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val viewMode by viewModel.calendarViewMode.collectAsState()
    val weekStart by viewModel.calendarWeekStart.collectAsState()
    val monthDate by viewModel.calendarMonthDate.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val slots by viewModel.classSlots.collectAsState()
    val overrides by viewModel.overrides.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val gapJournals by viewModel.gapJournals.collectAsState()

    var fabExpanded by remember { mutableStateOf(false) }

    val selectedDateStr = ScheduleLogic.formatDate(selectedDate)
    val daySchedule = ScheduleLogic.getDaySchedule(
        dateStr = selectedDateStr,
        semester = semester,
        courses = courses,
        slots = slots,
        overrides = overrides,
        exams = exams,
        holidays = holidays,
        attendances = attendances,
        isTodayDate = (selectedDate == LocalDate.now())
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Segmented View Switcher: Day | Week | Month
            item {
                SegmentedViewControl(
                    currentMode = viewMode,
                    onModeChange = { viewModel.setCalendarViewMode(it) }
                )
            }

            // Week Navigator Header
            item {
                WeekNavigatorHeader(
                    weekStart = weekStart,
                    weekNumber = ScheduleLogic.getSemesterWeek(weekStart, semester),
                    semesterName = semester.name,
                    onPrevWeek = { viewModel.prevWeek() },
                    onNextWeek = { viewModel.nextWeek() },
                    onJumpToday = { viewModel.jumpToTodayCalendar() }
                )
            }

            // Weekly Load Matrix
            item {
                WeeklyLoadMatrixBanner(
                    weekStart = weekStart,
                    semester = semester,
                    courses = courses,
                    slots = slots,
                    overrides = overrides,
                    exams = exams,
                    holidays = holidays
                )
            }

            if (slots.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Today, contentDescription = null, tint = ForestGreenSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "No routine timetable in database", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Go to Settings > Import Routine or restore standard routine.", fontSize = 11.sp, color = AgriTextMuted)
                            }
                        }
                    }
                }
            }

            // SECTION 1: WEEK VIEW (Matrix timetable)
            if (viewMode == "week" || viewMode == "day") {
                item {
                    AcademicWeekTimetable(
                        weekStart = weekStart,
                        selectedDate = selectedDate,
                        semester = semester,
                        courses = courses,
                        slots = slots,
                        overrides = overrides,
                        exams = exams,
                        holidays = holidays,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) }
                    )
                }
            }

            // SECTION 2: MONTH VIEW
            if (viewMode == "month" || viewMode == "week") {
                item {
                    MonthViewGrid(
                        monthDate = monthDate,
                        selectedDate = selectedDate,
                        overrides = overrides,
                        exams = exams,
                        holidays = holidays,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                        onPrevMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }
            }

            // SECTION 3: DAY DETAILS (Active Date Schedule preview)
            item {
                val dayGaps = gapJournals.filter { it.date == selectedDateStr }
                DayDetailsPreviewCard(
                    date = selectedDate,
                    daySchedule = daySchedule,
                    dayGaps = dayGaps,
                    onAddExam = { viewModel.showAddExam(selectedDateStr) },
                    onAddHoliday = { viewModel.showAddHoliday(selectedDateStr) },
                    onEditRoutine = {
                        val firstClass = daySchedule.classes.firstOrNull()
                        if (firstClass != null) {
                            viewModel.editClass(firstClass)
                        } else {
                            viewModel.showAddExam(selectedDateStr)
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // FAB + Popover
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(visible = fabExpanded) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FabMenuItem(
                            icon = Icons.Default.Quiz,
                            iconBg = AgriAmberContainer,
                            iconTint = AgriAmberText,
                            title = "Add Exam / Quiz",
                            subtitle = "Midterm, Practical or VIVA",
                            onClick = {
                                fabExpanded = false
                                viewModel.showAddExam(selectedDateStr)
                            }
                        )
                        FabMenuItem(
                            icon = Icons.Default.Celebration,
                            iconBg = AgriPurpleContainer,
                            iconTint = AgriPurpleText,
                            title = "Add Holiday / Break",
                            subtitle = "Campus closure or recess",
                            onClick = {
                                fabExpanded = false
                                viewModel.showAddHoliday(selectedDateStr)
                            }
                        )
                        FabMenuItem(
                            icon = Icons.Default.AltRoute,
                            iconBg = MaterialTheme.colorScheme.secondaryContainer,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            title = "Schedule Override",
                            subtitle = "Room swap or makeup class",
                            onClick = {
                                fabExpanded = false
                                val first = daySchedule.classes.firstOrNull()
                                if (first != null) viewModel.editClass(first)
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                shape = RoundedCornerShape(28.dp),
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("calendar_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Event"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Event",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SegmentedViewControl(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val modes = listOf("day" to "Day", "week" to "Week", "month" to "Month")
            modes.forEach { (mode, label) ->
                val selected = currentMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { onModeChange(mode) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekNavigatorHeader(
    weekStart: LocalDate,
    weekNumber: Int,
    semesterName: String,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit
) {
    val weekEnd = weekStart.plusDays(6)
    val fmt = DateTimeFormatter.ofPattern("MMM dd", Locale.US)
    val title = "${weekStart.format(fmt)} – ${weekEnd.format(fmt)}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPrevWeek,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Week",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (weekNumber <= 0) "Pre-Term" else "W$weekNumber",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "$semesterName Academic Term",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onJumpToday,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Today, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onNextWeek,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Week",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyLoadMatrixBanner(
    weekStart: LocalDate,
    semester: com.example.data.model.Semester,
    courses: List<com.example.data.model.CourseEntity>,
    slots: List<com.example.data.model.ClassSlotEntity>,
    overrides: List<com.example.data.model.OverrideEntity>,
    exams: List<com.example.data.model.ExamEntity>,
    holidays: List<com.example.data.model.HolidayEntity>
) {
    var totalClassesCount = 0
    val totalMinutesThisWeek = (0..6).sumOf { i ->
        val d = weekStart.plusDays(i.toLong())
        val schedule = ScheduleLogic.getDaySchedule(
            dateStr = ScheduleLogic.formatDate(d),
            semester = semester,
            courses = courses,
            slots = slots,
            overrides = overrides,
            exams = exams,
            holidays = holidays,
            attendances = emptyList(),
            isTodayDate = false
        )
        totalClassesCount += schedule.classes.size
        schedule.classes.sumOf { cls ->
            try {
                val st = java.time.LocalTime.parse(cls.start)
                val en = java.time.LocalTime.parse(cls.end)
                java.time.temporal.ChronoUnit.MINUTES.between(st, en).coerceAtLeast(0)
            } catch (_: Exception) { 60L }
        }
    }
    val hours = totalMinutesThisWeek / 60
    val mins = totalMinutesThisWeek % 60
    val loadText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    val modulesCount = courses.size

    val weekEnd = weekStart.plusDays(6)
    val weekOverridesCount = overrides.count { o ->
        try {
            val d = o.date?.let { ScheduleLogic.parseDate(it) }
            d != null && !d.isBefore(weekStart) && !d.isAfter(weekEnd)
        } catch (_: Exception) { false }
    }

    val weekExamsCount = exams.count { e ->
        try {
            val d = ScheduleLogic.parseDate(e.date)
            !d.isBefore(weekStart) && !d.isAfter(weekEnd)
        } catch (_: Exception) { false }
    }

    val weekHolidaysCount = (0..6).count { i ->
        val d = weekStart.plusDays(i.toLong())
        holidays.any { h ->
            try {
                val s = ScheduleLogic.parseDate(h.startDate)
                val e = ScheduleLogic.parseDate(h.endDate)
                !d.isBefore(s) && !d.isAfter(e)
            } catch (_: Exception) { false }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEKLY LOAD MATRIX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${semester.name} (Live)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(value = loadText, label = "$totalClassesCount Classes", modifier = Modifier.weight(1f))
                StatCard(
                    value = if (weekExamsCount > 0) "$weekExamsCount Exams" else "$modulesCount Modules",
                    label = if (weekExamsCount > 0) "Assessments" else "Enrolled Modules",
                    modifier = Modifier.weight(1f)
                )
                OverrideStatCard(
                    value = if (weekHolidaysCount > 0) "$weekHolidaysCount Holidays" else "$weekOverridesCount Shifts",
                    label = if (weekHolidaysCount > 0) "Campus Closed" else "Room Overrides",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun OverrideStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AgriAmberContainer.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AgriAmberText
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AgriAmberText)
            }
            Text(text = label, fontSize = 10.sp, color = AgriAmberText, maxLines = 1)
        }
    }
}

@Composable
private fun AcademicWeekTimetable(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    semester: com.example.data.model.Semester,
    courses: List<com.example.data.model.CourseEntity>,
    slots: List<com.example.data.model.ClassSlotEntity>,
    overrides: List<com.example.data.model.OverrideEntity>,
    exams: List<com.example.data.model.ExamEntity>,
    holidays: List<com.example.data.model.HolidayEntity>,
    onSelectDate: (LocalDate) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Academic Week Schedule",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "(7-Day Layout)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ForestGreenSecondary))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Lecture", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AgriAmber))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Lab Shift", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable 7-column timetable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 7) {
                    val date = weekStart.plusDays(i.toLong())
                    val dayName = ScheduleLogic.getAppWeekday(date)
                    val isToday = (date == LocalDate.now())
                    val isSelected = (date == selectedDate)

                    val daySchedule = remember(date, courses, slots, overrides, exams, holidays) {
                        ScheduleLogic.getDaySchedule(
                            dateStr = ScheduleLogic.formatDate(date),
                            semester = semester,
                            courses = courses,
                            slots = slots,
                            overrides = overrides,
                            exams = exams,
                            holidays = holidays,
                            attendances = emptyList(),
                            isTodayDate = isToday
                        )
                    }

                    DayColumnCard(
                        date = date,
                        dayName = dayName,
                        isToday = isToday,
                        isSelected = isSelected,
                        classes = daySchedule.classes,
                        isFreeDay = daySchedule.isFreeDay,
                        holidayName = daySchedule.holiday?.title,
                        onClick = { onSelectDate(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumnCard(
    date: LocalDate,
    dayName: String,
    isToday: Boolean,
    isSelected: Boolean,
    classes: List<com.example.domain.EffectiveClass>,
    isFreeDay: Boolean,
    holidayName: String?,
    onClick: () -> Unit
) {
    val dayNum = date.dayOfMonth.toString()

    Column(
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Date Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isToday) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ForestGreenSecondary))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = dayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = dayNum, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Text(
                    text = dayNum,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Dynamic Slots
        if (holidayName != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AgriPurpleContainer)
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Holiday", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AgriPurpleText)
            }
        } else if (isFreeDay || classes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Free Day", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            classes.forEach { cls ->
                val isLab = cls.type == "lab"
                val isShift = cls.changeType == "room" || cls.changeType == "time"
                val isCancelled = cls.changeType == "cancelled"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isCancelled) AgriDangerSoft
                            else if (isShift) AgriAmberContainer.copy(alpha = 0.85f)
                            else if (isLab) AgriAmberContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ScheduleLogic.format12Hour(cls.start),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShift) AgriAmberText else MaterialTheme.colorScheme.primary
                        )
                        if (isShift) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(AgriAmber)
                                    .padding(horizontal = 2.dp, vertical = 0.5.dp)
                            ) {
                                Text(text = "SHIFT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Text(
                        text = cls.courseCode,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShift) AgriAmberText else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Rm ${cls.room}",
                        fontSize = 8.5.sp,
                        color = if (isShift) AgriAmberText else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotBlock(time: String, code: String, room: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(4.dp)
    ) {
        Text(text = time, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = code, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(text = room, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun MonthViewGrid(
    monthDate: LocalDate,
    selectedDate: LocalDate,
    overrides: List<com.example.data.model.OverrideEntity>,
    exams: List<com.example.data.model.ExamEntity>,
    holidays: List<com.example.data.model.HolidayEntity>,
    onSelectDate: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val ym = YearMonth.from(monthDate)
    val monthTitle = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = monthTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Agronomy Cycle", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7 Days header: Sat Sun Mon Tue Wed Thu Fri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                val dayHeaders = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                dayHeaders.forEach {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month Grid starting on Saturday
            val firstOfMonth = ym.atDay(1)
            val firstDaySatBased = ScheduleLogic.getSatBasedDayNumber(firstOfMonth) // 1=SAT .. 7=FRI
            val daysInMonth = ym.lengthOfMonth()

            var dayCounter = 1
            var prevMonthDay = firstOfMonth.minusDays((firstDaySatBased - 1).toLong())

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 6) {
                    if (dayCounter > daysInMonth && row >= 5) break

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 1..7) {
                            if (row == 0 && col < firstDaySatBased) {
                                // Previous month days
                                val pDate = prevMonthDay
                                prevMonthDay = prevMonthDay.plusDays(1)
                                Text(
                                    text = "${pDate.dayOfMonth}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            } else if (dayCounter <= daysInMonth) {
                                val date = ym.atDay(dayCounter)
                                val dateStr = ScheduleLogic.formatDate(date)
                                val isToday = (date == LocalDate.now())
                                val isSelected = (date == selectedDate)
                                val hasClasses = col in 1..5

                                val isShift = overrides.any { it.date == dateStr }
                                val isHoliday = holidays.any { h ->
                                    try {
                                        val s = ScheduleLogic.parseDate(h.startDate)
                                        val e = ScheduleLogic.parseDate(h.endDate)
                                        !date.isBefore(s) && !date.isAfter(e)
                                    } catch (_: Exception) { false }
                                }
                                val isExam = exams.any { it.date == dateStr }

                                MonthGridCell(
                                    day = dayCounter,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    hasClasses = hasClasses,
                                    isShift = isShift,
                                    isHoliday = isHoliday,
                                    isExam = isExam,
                                    onClick = { onSelectDate(date) },
                                    modifier = Modifier.weight(1f)
                                )
                                dayCounter++
                            } else {
                                // Next month day fill
                                val nextDayNum = dayCounter - daysInMonth
                                Text(
                                    text = "$nextDayNum",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGridCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasClasses: Boolean,
    isShift: Boolean,
    isHoliday: Boolean,
    isExam: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ForestGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$day", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else {
            Text(
                text = "$day",
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Dots indicator
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (isShift) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AgriAmber))
            } else if (isHoliday) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AgriPurple))
            } else if (hasClasses) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ForestGreenSecondary))
            }
        }
    }
}

@Composable
private fun DayDetailsPreviewCard(
    date: LocalDate,
    daySchedule: com.example.domain.DayScheduleResult,
    dayGaps: List<com.example.data.model.GapJournalEntity> = emptyList(),
    onAddExam: () -> Unit,
    onAddHoliday: () -> Unit,
    onEditRoutine: () -> Unit
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy", Locale.US)
    val isToday = (date == LocalDate.now())

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Drag Handle bar
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date.format(dateFmt),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Fall 2026 • Week ${daySchedule.weekNumber} • BSc Agriculture Core Session",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForestGreenPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "Today", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Override Notice if any active shifts exist on this date
            val activeOverrides = daySchedule.classes.filter { it.changeType != null }
            if (activeOverrides.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriAmberContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = AgriAmberText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${activeOverrides.size} Active Schedule Shift(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriAmberText
                            )
                            activeOverrides.forEach { ov ->
                                Text(
                                    text = "${ov.courseCode}: ${ov.overrideNote ?: if (ov.changeType == "room") "Moved to Room ${ov.room}" else "Schedule shifted"}",
                                    fontSize = 11.sp,
                                    color = AgriAmberText,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Classes list for this date
            if (daySchedule.classes.isEmpty() && daySchedule.exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes or exams on this date",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    daySchedule.classes.forEach { cls ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (cls.changeType == "room") AgriAmberContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${ScheduleLogic.format12Hour(cls.start)} – ${ScheduleLogic.format12Hour(cls.end)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (cls.changeType == "room") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AgriAmber)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(text = "ROOM OVERRIDE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (cls.type == "lab") "Lab Session" else "Lecture",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cls.courseCode}: ${cls.courseTitle}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = cls.room,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cls.instructorName} [${cls.instructorShortCode}]",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Present • 88%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestGreenSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gap Journal notes for this date
            if (dayGaps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Gap Time Activities (${dayGaps.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayGaps.forEach { gap ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${ScheduleLogic.format12Hour(gap.startTime)} – ${ScheduleLogic.format12Hour(gap.endTime)} (${gap.durationMinutes}m)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = gap.tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                                if (gap.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "“${gap.note}”", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons row: + Exam, + Holiday, Edit Routine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddExam,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = AgriAmberText, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+ Exam", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onAddHoliday,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.EventBusy, contentDescription = null, tint = AgriPurpleText, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+ Holiday", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onEditRoutine,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit Routine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
