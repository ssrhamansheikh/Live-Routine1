package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ScheduleLogic
import com.example.ui.MainTab
import com.example.ui.RoutineViewModel
import com.example.ui.theme.AgriAccentGold
import com.example.ui.theme.AgriAccentGoldSoft
import com.example.ui.theme.AgriAmberText
import com.example.ui.theme.AgriBorder
import com.example.ui.theme.AgriDanger
import com.example.ui.theme.AgriDangerSoft
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryDark
import com.example.ui.theme.AgriPrimarySoft
import com.example.ui.theme.AgriSurface
import com.example.ui.theme.AgriSurface2
import com.example.ui.theme.AgriText
import com.example.ui.theme.AgriTextMuted
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeDetailsScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeHomeDetails()
    }

    var showSemesterDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val liveClock by viewModel.liveClockText.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val slots by viewModel.classSlots.collectAsState()
    val overrides by viewModel.overrides.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    val refDate = activeDate
    val (overallStat, courseStats) = remember(courses, slots, overrides, holidays, attendances, semester, refDate) {
        ScheduleLogic.calculateAttendanceStats(
            semester = semester,
            courses = courses,
            slots = slots,
            overrides = overrides,
            holidays = holidays,
            attendances = attendances,
            referenceDate = refDate
        )
    }

    val semStart = try { ScheduleLogic.parseDate(semester.startDate) } catch (_: Exception) { refDate }
    val semEnd = try { ScheduleLogic.parseDate(semester.endDate) } catch (_: Exception) { refDate.plusDays(100) }
    val isUpcomingSemester = refDate.isBefore(semStart)
    val daysUntilStart = if (isUpcomingSemester) ChronoUnit.DAYS.between(refDate, semStart) else 0L
    val totalDays = ChronoUnit.DAYS.between(semStart, semEnd).coerceAtLeast(1)
    val elapsedDays = ChronoUnit.DAYS.between(semStart, refDate).coerceIn(0, totalDays)
    val progressFloat = if (isUpcomingSemester) 0f else (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progressFloat * 100).toInt()
    val totalWeeks = ((totalDays + 6) / 7).toInt().coerceAtLeast(1)
    val currentWeekNum = if (isUpcomingSemester) 0 else ScheduleLogic.getSemesterWeek(refDate, semester)
    val dayOfWeekNum = ScheduleLogic.getSatBasedDayNumber(refDate)

    val activeRelocations = overrides.filter { it.changeType == "room" }
    val upcomingExams = exams.filter { exam ->
        val d = try { ScheduleLogic.parseDate(exam.date) } catch (_: Exception) { refDate }
        !d.isBefore(refDate)
    }.sortedBy { it.date }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.closeHomeDetails() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AgriText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Home Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriPrimaryDark
                )
                Text(
                    text = "System status, attendance metrics & academic overview",
                    fontSize = 11.sp,
                    color = AgriTextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Sync & Clock Status Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
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
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = AgriPrimaryDark
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Direct SQLite Synchronized",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriPrimaryDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(AgriSurface2)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = AgriTextMuted
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "100% Offline (SQLite)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AgriTextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "System Real-Time Clock",
                                    fontSize = 11.sp,
                                    color = AgriTextMuted
                                )
                                Text(
                                    text = liveClock,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AgriPrimaryDark
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isUpcomingSemester) AgriAccentGoldSoft else AgriPrimarySoft)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isUpcomingSemester) AgriAccentGold else AgriPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUpcomingSemester) "Starts in $daysUntilStart ${if (daysUntilStart == 1L) "day" else "days"}" else "On Schedule",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUpcomingSemester) AgriAmberText else AgriPrimaryDark
                                )
                            }
                        }
                    }
                }
            }

            // 2. Date & Term Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = AgriPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = refDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", java.util.Locale.US)),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriText
                                )
                            }

                            IconButton(
                                onClick = { showSemesterDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Semester Schedule",
                                    tint = AgriPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUpcomingSemester) {
                                "${semester.name} • Starts on ${semStart.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", java.util.Locale.US))} (in $daysUntilStart ${if (daysUntilStart == 1L) "day" else "days"})"
                            } else {
                                "${semester.name} • Week $currentWeekNum of Semester (Day $dayOfWeekNum/7)"
                            },
                            fontSize = 12.sp,
                            color = if (isUpcomingSemester) AgriPrimaryDark else AgriTextMuted,
                            fontWeight = if (isUpcomingSemester) FontWeight.SemiBold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Term progress bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isUpcomingSemester) "Semester Countdown ($daysUntilStart days remaining)" else "Semester Progress (Week $currentWeekNum of $totalWeeks)",
                                    fontSize = 11.sp,
                                    color = AgriTextMuted
                                )
                                Text(
                                    text = if (isUpcomingSemester) "Starts Oct 3" else "$progressPercent%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriPrimary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AgriPrimary,
                                trackColor = AgriSurface2
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Term Start / End Date interactive row with Edit hint
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AgriSurface2)
                                .clickable { showSemesterDialog = true }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Semester Start", fontSize = 10.sp, color = AgriTextMuted)
                                Text(text = semester.startDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = AgriText)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Duration", fontSize = 10.sp, color = AgriTextMuted)
                                Text(text = "$totalWeeks Weeks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgriPrimaryDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Semester End", fontSize = 10.sp, color = AgriTextMuted)
                                Text(text = semester.endDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = AgriText)
                            }
                        }
                    }
                }
            }

            // 3. Academic Program Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AgriPrimarySoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = AgriPrimaryDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${studentProfile.program} (${studentProfile.section})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriText
                                    )
                                    Text(
                                        text = "${studentProfile.faculty} • ${courses.size} Registered Courses",
                                        fontSize = 12.sp,
                                        color = AgriTextMuted
                                    )
                                    Text(
                                        text = "Student: ${studentProfile.name} • ${studentProfile.studentId}",
                                        fontSize = 11.sp,
                                        color = AgriTextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showProfileDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Program Details",
                                    tint = AgriPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AgriSurface2)
                                .clickable { showProfileDialog = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Batch/Session: ${studentProfile.session} • Tap to edit profile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AgriPrimaryDark
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = AgriPrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSemesterDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriPrimarySoft),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AgriPrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Semester",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                    }

                    Button(
                        onClick = { showProfileDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AgriText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriText
                        )
                    }
                }
            }

            // 4. Attendance Summary Card with Mini Progress Bars
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = AgriPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Attendance Summary",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriText
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(AgriPrimarySoft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f%%", overallStat.percentage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriPrimaryDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Per-course list with mini progress bars
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            courseStats.forEach { cs ->
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${cs.courseCode} (${cs.courseTitle})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AgriText,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.0f%%", cs.percentage)} (${cs.attendedCount}/${cs.heldCount})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cs.isSafe) AgriPrimary else AgriDanger
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (cs.percentage / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (cs.isSafe) AgriPrimary else AgriDanger,
                                        trackColor = AgriSurface2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Active Venue Relocations Card(s)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = AgriDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Venue Relocations",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeRelocations.isEmpty()) {
                            Text(
                                text = "No active room relocations. All classes in regular venues.",
                                fontSize = 12.sp,
                                color = AgriTextMuted
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeRelocations.forEach { r ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AgriDangerSoft.copy(alpha = 0.5f))
                                            .border(1.dp, AgriDanger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${r.courseCode} Lab Session",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AgriText
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(AgriDanger)
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "TODAY ONLY",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Moved to ${r.newRoom ?: "AGRIlab4"}. ${r.note ?: ""}",
                                                fontSize = 12.sp,
                                                color = AgriText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Upcoming Exams Countdown Card(s)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = AgriAccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upcoming Exams & Countdowns",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (upcomingExams.isEmpty()) {
                            Text(
                                text = "No assessments scheduled for the upcoming weeks.",
                                fontSize = 12.sp,
                                color = AgriTextMuted
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                upcomingExams.forEach { ex ->
                                    val exDate = try { ScheduleLogic.parseDate(ex.date) } catch (_: Exception) { refDate }
                                    val daysDiff = ChronoUnit.DAYS.between(refDate, exDate)
                                    val countdown = if (daysDiff == 0L) "Today" else if (daysDiff == 1L) "Tomorrow" else "in ${daysDiff}d"

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AgriAccentGoldSoft)
                                            .border(1.dp, AgriAccentGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${ex.type} $countdown: ${ex.courseCode}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AgriAmberText
                                                )
                                                Text(
                                                    text = "Date: ${ex.date} • ${ScheduleLogic.format12Hour(ex.start)} (${ex.hall})",
                                                    fontSize = 11.sp,
                                                    color = AgriAmberText
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    viewModel.closeHomeDetails()
                                                    viewModel.selectTab(MainTab.EXAMS)
                                                }
                                            ) {
                                                Text(
                                                    text = "Syllabus",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AgriPrimaryDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // 1. Edit Semester Dialog
    if (showSemesterDialog) {
        var nameInput by remember { mutableStateOf(semester.name) }
        var startInput by remember { mutableStateOf(semester.startDate) }
        var endInput by remember { mutableStateOf(semester.endDate) }

        AlertDialog(
            onDismissRequest = { showSemesterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Edit Semester Schedule", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Customize your semester name and academic dates. The app calculates week numbers and attendance based on the start date.",
                        fontSize = 12.sp,
                        color = AgriTextMuted
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Semester Term Name") },
                        placeholder = { Text("e.g. Fall 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = startInput,
                        onValueChange = { startInput = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        placeholder = { Text("2026-10-03") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick presets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { startInput = "2026-10-03" },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimarySoft),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Set Oct 3, 2026", fontSize = 11.sp, color = AgriPrimaryDark, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { startInput = refDate.toString() },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Set to Today", fontSize = 11.sp, color = AgriText, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = endInput,
                        onValueChange = { endInput = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        placeholder = { Text("2027-02-28") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && startInput.isNotBlank() && endInput.isNotBlank()) {
                            viewModel.updateSemester(nameInput.trim(), startInput.trim(), endInput.trim())
                            showSemesterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Semester", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSemesterDialog = false }) {
                    Text("Cancel", color = AgriTextMuted)
                }
            }
        )
    }

    // 2. Edit Academic & Student Profile Dialog
    if (showProfileDialog) {
        var progInput by remember { mutableStateOf(studentProfile.program) }
        var secInput by remember { mutableStateOf(studentProfile.section) }
        var facInput by remember { mutableStateOf(studentProfile.faculty) }
        var sessInput by remember { mutableStateOf(studentProfile.session) }
        var nameInput by remember { mutableStateOf(studentProfile.name) }
        var idInput by remember { mutableStateOf(studentProfile.studentId) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Edit Academic & Student Details", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = progInput,
                        onValueChange = { progInput = it },
                        label = { Text("Program / Degree") },
                        placeholder = { Text("e.g. BSc Agriculture or BSAg 262") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = secInput,
                        onValueChange = { secInput = it },
                        label = { Text("Class Section") },
                        placeholder = { Text("e.g. Section C") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = facInput,
                        onValueChange = { facInput = it },
                        label = { Text("Faculty / Department") },
                        placeholder = { Text("e.g. Faculty of Agriculture") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sessInput,
                        onValueChange = { sessInput = it },
                        label = { Text("Academic Session / Batch") },
                        placeholder = { Text("e.g. Fall 2026 • BSAg 262") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { idInput = it },
                        label = { Text("Student Roll / ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProfile(
                            name = nameInput.trim(),
                            studentId = idInput.trim(),
                            section = secInput.trim(),
                            faculty = facInput.trim(),
                            session = sessInput.trim(),
                            program = progInput.trim()
                        )
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Details", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel", color = AgriTextMuted)
                }
            }
        )
    }
}
