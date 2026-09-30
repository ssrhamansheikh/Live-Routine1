package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ScheduleLogic
import com.example.ui.RoutineViewModel
import com.example.ui.theme.AgriAmber
import com.example.ui.theme.AgriAmberContainer
import com.example.ui.theme.AgriAmberText
import com.example.ui.theme.AgriError
import com.example.ui.theme.AgriErrorContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenSecondary
import java.time.LocalDate

@Composable
fun AttendanceScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val slots by viewModel.classSlots.collectAsState()
    val overrides by viewModel.overrides.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()

    // Dynamic Reference date from activeDate
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Circular Aggregate Gauge Card
        item {
            AggregateRingCard(
                stat = overallStat,
                onResetAttendance = { viewModel.resetAllAttendance() }
            )
        }

        // Quick Daily Attendance Logger
        item {
            val activeDateStr = ScheduleLogic.formatDate(activeDate)
            val todaySchedule = remember(activeDate, courses, slots, overrides, holidays, attendances) {
                ScheduleLogic.getDaySchedule(
                    dateStr = activeDateStr,
                    semester = semester,
                    courses = courses,
                    slots = slots,
                    overrides = overrides,
                    exams = emptyList(),
                    holidays = holidays,
                    attendances = attendances,
                    isTodayDate = true
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenSecondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Attendance Logger",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = activeDate.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (todaySchedule.classes.isEmpty()) {
                        Text(
                            text = "No classes scheduled on this day.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todaySchedule.classes.forEach { cls ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${cls.courseCode} (${cls.courseTitle})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${ScheduleLogic.format12Hour(cls.start)} - ${ScheduleLogic.format12Hour(cls.end)} • Room ${cls.room}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.markAttendance(activeDateStr, cls.slotId, "present") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (cls.attendanceStatus == "present") ForestGreenSecondary else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (cls.attendanceStatus == "present") Color.White else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.markAttendance(activeDateStr, cls.slotId, "absent") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (cls.attendanceStatus == "absent") AgriErrorContainer else MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (cls.attendanceStatus == "absent") AgriError else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Per-Course Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled Modules Attendance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Min 75% Required",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Per-Course Cards
        items(courseStats, key = { it.courseCode }) { courseStat ->
            CourseAttendanceCard(
                stat = courseStat
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun AggregateRingCard(
    stat: ScheduleLogic.OverallAttendanceStat,
    onResetAttendance: () -> Unit
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Fall 2026 Active Term Record",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (stat.totalHeld == 0) MaterialTheme.colorScheme.secondaryContainer
                            else if (stat.isEligible) MaterialTheme.colorScheme.secondaryContainer
                            else AgriErrorContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (stat.totalHeld == 0) "Ready • 0 Sessions"
                        else if (stat.isEligible) "Eligible for Final Exam"
                        else "Attendance Shortage",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.totalHeld == 0 || stat.isEligible) MaterialTheme.colorScheme.onSecondaryContainer else AgriError
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ring + Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Circular Ring Gauge
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val ringProgress = (stat.percentage / 100f).coerceIn(0f, 1f)
                    val activeColor = if (stat.totalHeld == 0 || stat.isEligible) ForestGreenSecondary else AgriError
                    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (stat.totalHeld > 0) {
                            drawArc(
                                color = activeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * ringProgress,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (stat.totalHeld == 0) "0.0%" else String.format(java.util.Locale.US, "%.1f%%", stat.percentage),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (stat.totalHeld == 0) "Not Started" else "Cumulative",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats breakdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ForestGreenSecondary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Attended: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${stat.totalAttended}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreenSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AgriError))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Missed: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${stat.totalAbsent}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AgriError)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Total Held: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${stat.totalHeld}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 75% target threshold bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "75% Minimum Bar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (stat.isEligible) "+${String.format(java.util.Locale.US, "%.1f", stat.percentage - 75f)}% buffer"
                        else "-${String.format(java.util.Locale.US, "%.1f", 75f - stat.percentage)}% needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.isEligible) ForestGreenSecondary else AgriError
                    )
                }
                LinearProgressIndicator(
                    progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (stat.isEligible) ForestGreenSecondary else AgriError,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stat.totalHeld} total sessions logged",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Attendance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Attendance Records?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all logged present and absent class sessions back to 0. Are you sure you want to reset?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetAttendance()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset to 0", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CourseAttendanceCard(
    stat: ScheduleLogic.CourseAttendanceStat
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Course Code, Type pill, Safe / At Risk badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stat.courseCode,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (stat.type == "lab") "LAB" else "THEORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (stat.isSafe) MaterialTheme.colorScheme.secondaryContainer
                            else AgriErrorContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (stat.isSafe) "SAFE" else "AT RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.isSafe) MaterialTheme.colorScheme.onSecondaryContainer else AgriError
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stat.courseTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Percentage & Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f%%", stat.percentage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stat.isSafe) ForestGreenSecondary else AgriError
                )

                Text(
                    text = "${stat.attendedCount} / ${stat.heldCount} sessions attended",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (stat.isSafe) ForestGreenSecondary else AgriError,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            if (!stat.isSafe) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AgriError
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Must attend next ${stat.neededConsecutiveClasses} consecutive class(es) to recover 75%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AgriError
                    )
                }
            }
        }
    }
}
