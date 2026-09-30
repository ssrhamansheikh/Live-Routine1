package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RoutineViewModel
import com.example.ui.theme.AgriAmber
import com.example.ui.theme.AgriAmberText
import com.example.ui.theme.AgriBorder
import com.example.ui.theme.AgriDanger
import com.example.ui.theme.AgriDangerSoft
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryDark
import com.example.ui.theme.AgriPrimarySoft
import com.example.ui.theme.AgriPurple
import com.example.ui.theme.AgriPurpleContainer
import com.example.ui.theme.AgriPurpleText
import com.example.ui.theme.AgriSurface
import com.example.ui.theme.AgriSurface2
import com.example.ui.theme.AgriText
import com.example.ui.theme.AgriTextMuted

@Composable
fun SettingsScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val gapJournals by viewModel.gapJournals.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearJournalConfirm by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var alert10Mins by remember { mutableStateOf(true) }
    var alert1Hour by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        // UPDATE 2: "HOME" DETAILS SECTION / BUTTON AT TOP
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openHomeDetails() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AgriPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = AgriPrimaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Home Details",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(AgriPrimarySoft)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Overview",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriPrimaryDark
                                    )
                                }
                            }
                            Text(
                                text = "Sync status, semester progress, attendance & venue alerts",
                                fontSize = 11.sp,
                                color = AgriTextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = AgriPrimary
                    )
                }
            }
        }

        // Section 1b: Student Profile Button
        item {
            val studentProfile by viewModel.studentProfile.collectAsState()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openProfileDialog() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AgriPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = AgriPrimaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Student Profile",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                            Text(
                                text = "${studentProfile.name} • ${studentProfile.section} (${studentProfile.studentId})",
                                fontSize = 11.sp,
                                color = AgriTextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = AgriPrimary
                    )
                }
            }
        }

        // Section 2: Routine Management Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AgriPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = AgriPrimaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Academic Routine Management",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                            Text(
                                text = "Import AI-formatted routine or restore preinstalled template",
                                fontSize = 11.sp,
                                color = AgriTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openImportScreen() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Import Routine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AgriDanger),
                            modifier = Modifier.weight(1.1f).height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Reset to Default", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant Test Button: Load Sample BSAg 262 Routine
                    Button(
                        onClick = { viewModel.loadSampleNewRoutine() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Quick Test: Load BSAg 262 Sample Routine", fontSize = 12.sp, color = AgriText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Section 3: Gap Time Journal Entry Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openGapJournalScreen() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AgriPrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = AgriPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gap Time Journal",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriText
                                )
                                Text(
                                    text = "${gapJournals.size} gap sessions documented",
                                    fontSize = 11.sp,
                                    color = AgriTextMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = AgriTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportGapJournal { text ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Live Routine Gap Journal", text))
                                    Toast.makeText(context, "Journal copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export Journal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgriText)
                        }

                        Button(
                            onClick = { showClearJournalConfirm = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriDangerSoft),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = AgriDanger, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear Journal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgriDanger)
                        }
                    }
                }
            }
        }

        // Section 5: Holidays Manager Link
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openHolidaysManager() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AgriPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = AgriPurpleText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "University Holidays & Breaks",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                            Text(
                                text = "${holidays.size} holidays configured",
                                fontSize = 11.sp,
                                color = AgriTextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = AgriTextMuted
                    )
                }
            }
        }

        // Section 6: Theme Mode Selector
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance & Theme",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriText
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriSurface2)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                            val selected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) AgriPrimary else Color.Transparent)
                                    .clickable { viewModel.setTheme(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else AgriText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Class Alerts Toggles
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session Reminders & Banners",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Alert 10 minutes before class", fontSize = 13.sp, color = AgriText)
                        Switch(
                            checked = alert10Mins,
                            onCheckedChange = { alert10Mins = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AgriPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Alert 1 hour before scheduled lab/exam", fontSize = 13.sp, color = AgriText)
                        Switch(
                            checked = alert1Hour,
                            onCheckedChange = { alert1Hour = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AgriPrimary)
                        )
                    }
                }
            }
        }

        // Section 8: JSON Backup & Restore
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Data Backup & Portability",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriText
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Export all courses, schedule overrides, exam preparations, holidays, and attendance records as a portable JSON file.",
                        fontSize = 11.sp,
                        color = AgriTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackup { json ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Live Routine Backup", json))
                                    Toast.makeText(context, "Full Backup copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Backup JSON", fontSize = 12.sp, color = AgriText)
                        }

                        Button(
                            onClick = { showRestoreDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Restore JSON", fontSize = 12.sp, color = AgriText)
                        }
                    }
                }
            }
        }

        // Section 9: 100% Offline Note
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "100% Offline (SQLite Engine)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgriText)
                        Text(text = "All routines, records, and attendance stay strictly on this device.", fontSize = 11.sp, color = AgriTextMuted)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(text = "Reset Routine & Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose how you would like to restore your schedule:")
                    Text("• Reset Routine: Restores standard courses & timetable (keeps attendance & profile).", fontSize = 12.sp, color = AgriTextMuted)
                    Text("• Reset Attendance Only: Clears all attendance records back to 0.", fontSize = 12.sp, color = AgriAmberText)
                    Text("• Full Factory Reset: Resets all routine, exams, attendance and term settings.", fontSize = 12.sp, color = AgriDanger)
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.resetRoutineToDefault()
                                showResetConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Routine", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.resetAllAttendance()
                                showResetConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriAmber),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text("Reset Attendance", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.fullResetAllData()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Full Factory Reset", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Journal Dialog
    if (showClearJournalConfirm) {
        AlertDialog(
            onDismissRequest = { showClearJournalConfirm = false },
            title = { Text(text = "Clear Gap Time Journal?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently clear all documented gap activities?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllGapJournals()
                        showClearJournalConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriDanger)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearJournalConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore JSON Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(text = "Restore from JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Paste the JSON backup content below:", fontSize = 12.sp, color = AgriTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("{\"app\": \"live-routine\"...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            viewModel.restoreBackup(restoreJsonText)
                            showRestoreDialog = false
                            restoreJsonText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary)
                ) {
                    Text("Restore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
