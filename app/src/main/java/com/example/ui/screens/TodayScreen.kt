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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GapJournalEntity
import com.example.domain.ClassLiveStatus
import com.example.domain.EffectiveClass
import com.example.domain.ScheduleLogic
import com.example.ui.GapTargetInfo
import com.example.ui.MainTab
import com.example.ui.RoutineViewModel
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val activeDate by viewModel.activeDate.collectAsState()
    val activeTime by viewModel.activeTime.collectAsState()
    val isLiveClockRunning by viewModel.isLiveClockRunning.collectAsState()
    val liveClock by viewModel.liveClockText.collectAsState()
    val timelineFilter by viewModel.todayTimelineFilter.collectAsState()
    val demoViewMode by viewModel.demoViewMode.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val slots by viewModel.classSlots.collectAsState()
    val overrides by viewModel.overrides.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val gapJournals by viewModel.gapJournals.collectAsState()
    val attendanceFeedback by viewModel.attendanceFeedback.collectAsState()

    val activeDateStr = ScheduleLogic.formatDate(activeDate)

    // Dynamic reactive Day Schedule based on current activeDate and activeTime
    val daySchedule = remember(activeDate, activeTime, courses, slots, overrides, exams, holidays, attendances, semester) {
        ScheduleLogic.getDaySchedule(
            dateStr = activeDateStr,
            semester = semester,
            courses = courses,
            slots = slots,
            overrides = overrides,
            exams = exams,
            holidays = holidays,
            attendances = attendances,
            currentTime = activeTime,
            isTodayDate = true
        )
    }

    val todayGapMap = remember(gapJournals, activeDateStr) {
        gapJournals.filter { it.date == activeDateStr }.associateBy { it.startTime }
    }

    // Counts for filter chips
    val allCount = daySchedule.classes.size
    val ongoingCount = daySchedule.classes.count { it.liveStatus == ClassLiveStatus.LIVE }
    val upcomingCount = daySchedule.classes.count { it.liveStatus == ClassLiveStatus.UP_NEXT || it.liveStatus == ClassLiveStatus.SCHEDULED }
    val completedCount = daySchedule.classes.count { it.liveStatus == ClassLiveStatus.ENDED }

    val filteredClasses = when (timelineFilter) {
        "ongoing" -> daySchedule.classes.filter { it.liveStatus == ClassLiveStatus.LIVE }
        "upcoming" -> daySchedule.classes.filter { it.liveStatus == ClassLiveStatus.UP_NEXT || it.liveStatus == ClassLiveStatus.SCHEDULED }
        "completed" -> daySchedule.classes.filter { it.liveStatus == ClassLiveStatus.ENDED }
        else -> daySchedule.classes
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        when (demoViewMode) {
            "free" -> {
                item {
                    FreeDayEmptyView(onBackToLive = { viewModel.setDemoViewMode("normal") })
                }
            }
            "exam" -> {
                item {
                    ExamDayView(onOpenExams = { viewModel.selectTab(MainTab.EXAMS) })
                }
            }
            else -> {
                // 1. Header Row (Dynamic Date & Weekday Name)
                item {
                    TodayTopHeaderRow(
                        activeDate = activeDate,
                        classCount = daySchedule.classes.size,
                        isFreeDay = daySchedule.isFreeDay,
                        onOpenJournal = { viewModel.openGapJournalScreen() }
                    )
                }

                // 2. CLASS CLOCK Hero Card (Dynamic real-time progress)
                item {
                    ClassClockHeroCard(
                        liveClock = liveClock,
                        classes = daySchedule.classes,
                        currentTime = activeTime,
                        isFreeDay = daySchedule.isFreeDay
                    )
                }

                // 3. Section Title & Subtitle
                item {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Today's Class Timeline",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriText
                        )
                        Text(
                            text = "Live track with real-time class clock & progress (${daySchedule.classes.size} classes scheduled)",
                            fontSize = 12.sp,
                            color = AgriTextMuted
                        )
                    }
                }

                // 4. Filter Chips (All, Upcoming, Ongoing, Completed)
                item {
                    TimelineFilterChipsRow(
                        selectedFilter = timelineFilter,
                        allCount = allCount,
                        upcomingCount = upcomingCount,
                        ongoingCount = ongoingCount,
                        completedCount = completedCount,
                        onSelectFilter = { viewModel.setTodayTimelineFilter(it) }
                    )
                }

                // 5. Timeline with Live Class Hero & Interleaved Gap Cards
                val ongoingClass = daySchedule.classes.find { it.liveStatus == ClassLiveStatus.LIVE }
                if (ongoingClass != null && (timelineFilter == "all" || timelineFilter == "ongoing")) {
                    item {
                        LiveClassHeroCard(
                            cls = ongoingClass,
                            attendanceFeedback = attendanceFeedback,
                            onMarkAttendance = { status ->
                                viewModel.markAttendance(activeDateStr, ongoingClass.slotId, status)
                            }
                        )
                    }
                }

                // If no classes match filter or free day
                if (slots.isEmpty()) {
                    item {
                        EmptyRoutineCard(
                            onImport = { viewModel.openImportScreen() },
                            onRestoreDefault = { viewModel.resetRoutineToDefault() }
                        )
                    }
                } else if (daySchedule.isFreeDay || daySchedule.classes.isEmpty()) {
                    item {
                        FreeDayNoticeCard(activeDate = activeDate)
                    }
                } else if (filteredClasses.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AgriSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No classes in '$timelineFilter' state.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AgriTextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.setTodayTimelineFilter("all") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Show All Classes")
                                }
                            }
                        }
                    }
                }

                // Timeline list with gaps
                val classList = filteredClasses
                items(classList.size) { index ->
                    val currentClass = classList[index]

                    ClassTimelineCard(
                        item = currentClass,
                        onEdit = { viewModel.editClass(currentClass) },
                        onMarkAttendance = { status ->
                            viewModel.markAttendance(activeDateStr, currentClass.slotId, status)
                        }
                    )

                    // Check if there is a gap between this class and next class
                    if (index < classList.size - 1) {
                        val nextClass = classList[index + 1]
                        val endCurrent = try { LocalTime.parse(currentClass.end) } catch (_: Exception) { null }
                        val startNext = try { LocalTime.parse(nextClass.start) } catch (_: Exception) { null }

                        if (endCurrent != null && startNext != null && startNext.isAfter(endCurrent)) {
                            val gapMins = ChronoUnit.MINUTES.between(endCurrent, startNext).toInt()
                            if (gapMins >= 15) {
                                val savedEntry = todayGapMap[currentClass.end]
                                Spacer(modifier = Modifier.height(2.dp))
                                GapTimeJournalCard(
                                    date = activeDateStr,
                                    startTime = currentClass.end,
                                    endTime = nextClass.start,
                                    durationMinutes = gapMins,
                                    savedEntry = savedEntry,
                                    onOpenSheet = {
                                        viewModel.openEditGap(
                                            GapTargetInfo(
                                                date = activeDateStr,
                                                startTime = currentClass.end,
                                                endTime = nextClass.start,
                                                durationMinutes = gapMins,
                                                existingEntry = savedEntry
                                            )
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    } else if (index == classList.size - 1) {
                        // After last class gap option
                        val savedAfter = todayGapMap[currentClass.end]
                        Spacer(modifier = Modifier.height(2.dp))
                        GapTimeJournalCard(
                            date = activeDateStr,
                            startTime = currentClass.end,
                            endTime = "18:00",
                            durationMinutes = 95,
                            label = "After Classes Free Time",
                            savedEntry = savedAfter,
                            onOpenSheet = {
                                viewModel.openEditGap(
                                    GapTargetInfo(
                                        date = activeDateStr,
                                        startTime = currentClass.end,
                                        endTime = "18:00",
                                        durationMinutes = 95,
                                        existingEntry = savedAfter
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        // View Mode Toggle Segmented Bar
        item {
            Spacer(modifier = Modifier.height(6.dp))
            TodayViewModeSegmentedToggle(
                currentMode = demoViewMode,
                onSelectMode = { viewModel.setDemoViewMode(it) }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun TodayTopHeaderRow(
    activeDate: LocalDate,
    classCount: Int,
    isFreeDay: Boolean,
    onOpenJournal: () -> Unit
) {
    val weekdayName = activeDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
    val isRealToday = (activeDate == LocalDate.now())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = weekdayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriText,
                    letterSpacing = (-0.4).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AgriPrimarySoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isRealToday) "TODAY" else "SCHEDULE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriPrimaryDark
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenJournal,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = "Gap Journal",
                        tint = AgriPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = activeDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AgriPrimaryDark
            )
        }

        // Rounded grey-green chip showing number of classes
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AgriSurface2)
                .border(1.dp, AgriBorder, RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            val countText = if (isFreeDay || classCount == 0) "No classes"
            else if (classCount == 1) "1 class"
            else "$classCount classes"

            Text(
                text = countText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AgriText
            )
        }
    }
}

@Composable
private fun DateSwitcherRow(
    activeDate: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
    onSelectWeekday: (DayOfWeek) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevDay, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = AgriText)
                    }
                    Text(
                        text = activeDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriText
                    )
                    IconButton(onClick = onNextDay, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day", tint = AgriText)
                    }
                }

                Button(
                    onClick = onJumpToday,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimarySoft),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = AgriPrimaryDark, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Today (Live)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgriPrimaryDark)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Weekday Jump Chips: SAT to FRI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val days = listOf(
                    DayOfWeek.SATURDAY to "SAT",
                    DayOfWeek.SUNDAY to "SUN",
                    DayOfWeek.MONDAY to "MON",
                    DayOfWeek.TUESDAY to "TUE",
                    DayOfWeek.WEDNESDAY to "WED",
                    DayOfWeek.THURSDAY to "THU",
                    DayOfWeek.FRIDAY to "FRI"
                )

                days.forEach { (dow, label) ->
                    val isSelected = activeDate.dayOfWeek == dow
                    val isFree = dow == DayOfWeek.THURSDAY || dow == DayOfWeek.FRIDAY

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) AgriPrimary
                                else if (isFree) AgriSurface2
                                else AgriSurface
                            )
                            .border(
                                1.dp,
                                if (isSelected) AgriPrimary else AgriBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectWeekday(dow) }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isFree) "$label (Free)" else label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else AgriText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassClockHeroCard(
    liveClock: String,
    classes: List<EffectiveClass>,
    currentTime: LocalTime,
    isFreeDay: Boolean
) {
    val activeClass = classes.find { it.liveStatus == ClassLiveStatus.LIVE }
    val nextClass = classes.find { it.liveStatus == ClassLiveStatus.UP_NEXT || it.liveStatus == ClassLiveStatus.SCHEDULED }
    val allCompleted = classes.isNotEmpty() && classes.all { it.liveStatus == ClassLiveStatus.ENDED }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ClassClockGradientStart, ClassClockGradientEnd)
                )
            )
            .border(1.dp, Color(0xFF1E523A), RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Green dot + CLASS CLOCK on left, Live Time on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLASS CLOCK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF86EFAC)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF134530))
                        .border(1.dp, Color(0xFF1E5A40), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = liveClock,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFF0FDF4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body States: Dynamically evaluated based on currentTime
            when {
                isFreeDay || classes.isEmpty() -> {
                    // State e: Free Day
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NaturePeople, contentDescription = null, tint = Color(0xFF86EFAC), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "No Classes Today", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Official campus free day. Time for revision or field records!", fontSize = 12.sp, color = Color(0xFFD1FAE5))
                        }
                    }
                }

                activeClass != null -> {
                    // State a: Class Running with DYNAMIC elapsed / remaining calculations
                    val startT = try { LocalTime.parse(activeClass.start) } catch (_: Exception) { currentTime }
                    val endT = try { LocalTime.parse(activeClass.end) } catch (_: Exception) { currentTime.plusHours(1) }
                    val totalMins = ChronoUnit.MINUTES.between(startT, endT).coerceAtLeast(1)
                    val elapsedMins = ChronoUnit.MINUTES.between(startT, currentTime).coerceIn(0, totalMins)
                    val remainingMins = ChronoUnit.MINUTES.between(currentTime, endT).coerceAtLeast(0)
                    val progressFloat = (elapsedMins.toFloat() / totalMins.toFloat()).coerceIn(0f, 1f)
                    val percentDone = (progressFloat * 100).toInt()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeClass.courseCode}: ${activeClass.courseTitle}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Venue: ${activeClass.room} • ${if (activeClass.isDoubleBlock) "Double Block Period" else "Single Period"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD1FAE5)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFF22C55E).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "Running", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF86EFAC))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Progress Bar & Percentage
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$elapsedMins mins elapsed • $remainingMins mins left",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA7F3D0)
                                )
                                Text(
                                    text = "$percentDone% Done",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC)
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF22C55E),
                                trackColor = Color(0xFF134530)
                            )
                        }
                    }
                }

                nextClass != null -> {
                    // State b & c: Gap or Before first class
                    val startNext = try { LocalTime.parse(nextClass.start) } catch (_: Exception) { currentTime }
                    val minsUntilNext = ChronoUnit.MINUTES.between(currentTime, startNext).coerceAtLeast(0)
                    val hoursUntil = minsUntilNext / 60
                    val remMinsUntil = minsUntilNext % 60
                    val countdownStr = if (hoursUntil > 0) "${hoursUntil}h ${remMinsUntil}m" else "${remMinsUntil}m"

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFDE047), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Break Time • Next at ${ScheduleLogic.format12Hour(nextClass.start)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Up next: ${nextClass.courseCode} (${nextClass.room}) • Starts in $countdownStr",
                            fontSize = 12.sp,
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }

                allCompleted -> {
                    // State d: All done
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF86EFAC), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "No more classes today", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "All scheduled periods completed. Enjoy your time!", fontSize = 12.sp, color = Color(0xFFD1FAE5))
                        }
                    }
                }

                else -> {
                    Text(text = "No active classes scheduled.", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FreeDayNoticeCard(activeDate: LocalDate) {
    val dayName = activeDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.NaturePeople,
                contentDescription = null,
                tint = ForestGreenSecondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Classes Scheduled Today ($dayName)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AgriText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enjoy your free day! You can explore the calendar or prepare for upcoming lectures.",
                fontSize = 12.sp,
                color = AgriTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyRoutineCard(
    onImport: () -> Unit,
    onRestoreDefault: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AgriPrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AgriPrimaryDark,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Routine Loaded",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AgriText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "All database routines have been reset. You can import your timetable with AI or load the standard BSc Agriculture routine.",
                fontSize = 12.sp,
                color = AgriTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onImport,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Routine", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRestoreDefault,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = AgriPrimaryDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load Default", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AgriText)
                }
            }
        }
    }
}

@Composable
private fun TimelineFilterChipsRow(
    selectedFilter: String,
    allCount: Int,
    upcomingCount: Int,
    ongoingCount: Int,
    completedCount: Int,
    onSelectFilter: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            "all" to ("All ($allCount)" to true),
            "ongoing" to ("Ongoing ($ongoingCount)" to (ongoingCount > 0)),
            "upcoming" to ("Upcoming ($upcomingCount)" to (upcomingCount > 0)),
            "completed" to ("Completed ($completedCount)" to (completedCount > 0))
        )

        filters.forEach { (key, pair) ->
            val (label, shouldShow) = pair
            if (shouldShow) {
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) AgriPrimary
                            else AgriSurface
                        )
                        .border(
                            1.dp,
                            if (isSelected) AgriPrimary else AgriBorder,
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { onSelectFilter(key) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else AgriText
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveClassHeroCard(
    cls: EffectiveClass,
    attendanceFeedback: String?,
    onMarkAttendance: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(AgriPrimarySoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AgriPrimary))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "CURRENT SESSION IN PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                    }
                }

                Text(
                    text = "${ScheduleLogic.format12Hour(cls.start)} – ${ScheduleLogic.format12Hour(cls.end)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = AgriPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${cls.courseCode}: ${cls.courseTitle}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AgriText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Venue: ${cls.room} • Instructor: ${cls.instructorName} [${cls.instructorShortCode}]",
                fontSize = 12.sp,
                color = AgriTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onMarkAttendance("present") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cls.attendanceStatus == "present") ForestGreenSecondary else AgriSurface2
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (cls.attendanceStatus == "present") "✓ Marked Present" else "Mark Present",
                        color = if (cls.attendanceStatus == "present") Color.White else AgriText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { onMarkAttendance("absent") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cls.attendanceStatus == "absent") AgriDangerSoft else AgriSurface2
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (cls.attendanceStatus == "absent") "Marked Absent" else "Mark Absent",
                        color = if (cls.attendanceStatus == "absent") AgriDanger else AgriText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            attendanceFeedback?.let { fb ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = fb, fontSize = 11.sp, color = AgriPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ClassTimelineCard(
    item: EffectiveClass,
    onEdit: () -> Unit,
    onMarkAttendance: (String) -> Unit
) {
    val isLive = item.liveStatus == ClassLiveStatus.LIVE
    val isEnded = item.liveStatus == ClassLiveStatus.ENDED
    val isCancelled = item.changeType == "cancelled"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) AgriPrimarySoft.copy(alpha = 0.35f) else AgriSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLive) AgriPrimary else AgriBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Column
                Column(
                    modifier = Modifier.width(68.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = ScheduleLogic.format12Hour(item.start),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) AgriPrimaryDark else AgriText
                    )
                    Text(
                        text = ScheduleLogic.format12Hour(item.end),
                        fontSize = 11.sp,
                        color = AgriTextMuted
                    )
                }

                // Divider Line
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isLive) AgriPrimary
                            else if (isEnded) AgriBorder
                            else ForestGreenSecondary
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.courseCode,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) AgriTextMuted else AgriText,
                            textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (item.type == "lab") AgriAmberContainer else AgriSurface2)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.type == "lab") "LAB" else "THEORY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.type == "lab") AgriAmberText else AgriText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.courseTitle,
                        fontSize = 12.sp,
                        color = AgriTextMuted,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (item.changeType == "room") AgriDanger else AgriTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Room ${item.room}",
                            fontSize = 11.sp,
                            fontWeight = if (item.changeType == "room") FontWeight.Bold else FontWeight.Normal,
                            color = if (item.changeType == "room") AgriDanger else AgriTextMuted
                        )
                        if (item.changeType == "room") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "(Relocated)", fontSize = 10.sp, color = AgriDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit class",
                        tint = AgriTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Attendance Logging Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance: ${if (item.attendanceStatus == "present") "Present ✓" else if (item.attendanceStatus == "absent") "Absent ✗" else "Unrecorded"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (item.attendanceStatus) {
                        "present" -> ForestGreenSecondary
                        "absent" -> AgriDanger
                        else -> AgriTextMuted
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onMarkAttendance("present") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.attendanceStatus == "present") ForestGreenSecondary else AgriSurface2,
                            contentColor = if (item.attendanceStatus == "present") Color.White else AgriText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Present", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onMarkAttendance("absent") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.attendanceStatus == "absent") AgriDangerSoft else AgriSurface2,
                            contentColor = if (item.attendanceStatus == "absent") AgriDanger else AgriText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Absent", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GapTimeJournalCard(
    date: String,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    label: String = "Study Break",
    savedEntry: GapJournalEntity?,
    onOpenSheet: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface2),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSheet)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AgriPrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AgriPrimaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$durationMinutes mins $label (${ScheduleLogic.format12Hour(startTime)} – ${ScheduleLogic.format12Hour(endTime)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriText
                    )
                    Text(
                        text = if (savedEntry != null) "Logged: [${savedEntry.tag}] ${savedEntry.note}" else "Tap to log gap time activity / library study",
                        fontSize = 11.sp,
                        color = if (savedEntry != null) AgriPrimaryDark else AgriTextMuted,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = AgriPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TodayViewModeSegmentedToggle(
    currentMode: String,
    onSelectMode: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "SCHEDULE VIEW MODES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AgriTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AgriSurface2)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("normal" to "Normal (Live)", "free" to "Free Day", "exam" to "Exam Day").forEach { (mode, label) ->
                    val selected = currentMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) AgriPrimary else Color.Transparent)
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else AgriText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeDayEmptyView(onBackToLive: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.NaturePeople, contentDescription = null, tint = ForestGreenSecondary, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Campus Free Day", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgriText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Thursday and Friday are official free study days for BSc Agriculture students. Enjoy your time!",
                fontSize = 13.sp,
                color = AgriTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBackToLive,
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Return to Live Routine")
            }
        }
    }
}

@Composable
private fun ExamDayView(onOpenExams: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = AgriAmber, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Exam Day Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgriText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Regular classes are suspended during term assessment days. Track exam halls and syllabi in the Exams tab.",
                fontSize = 13.sp,
                color = AgriTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenExams,
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Exams Manager")
            }
        }
    }
}
