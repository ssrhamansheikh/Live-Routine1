package com.example.ui.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamEntity
import com.example.domain.ClassLiveStatus
import com.example.domain.EffectiveClass
import com.example.ui.theme.AgriAmber
import com.example.ui.theme.AgriAmberContainer
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
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun NotificationsDialog(
    activeClasses: List<EffectiveClass>,
    upcomingExams: List<ExamEntity>,
    alert10Mins: Boolean,
    alert1Hour: Boolean,
    onToggleSettings: (alert10: Boolean, alert1Hour: Boolean) -> Unit,
    onSendTestNotification: (Context) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var cur10 by remember { mutableStateOf(alert10Mins) }
    var cur1h by remember { mutableStateOf(alert1Hour) }

    val runningClass = activeClasses.find { it.liveStatus == ClassLiveStatus.LIVE }
    val upNextClass = activeClasses.find { it.liveStatus == ClassLiveStatus.UP_NEXT || it.liveStatus == ClassLiveStatus.SCHEDULED }
    val relocatedClasses = activeClasses.filter { it.changeType == "room" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notifications & Alerts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                        Text(
                            text = "Real-time class, lab & exam alerts",
                            fontSize = 11.sp,
                            color = AgriTextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AgriTextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 1: Active Live Alerts
                Text(
                    text = "ACTIVE ALERTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriPrimaryDark,
                    letterSpacing = 0.5.sp
                )

                if (runningClass != null) {
                    AlertBanner(
                        title = "In Session: ${runningClass.courseCode}",
                        description = "Room ${runningClass.room} • ${runningClass.minutesLeft}m remaining",
                        color = ForestGreenSecondary,
                        bgColor = AgriPrimarySoft,
                        icon = Icons.Default.School
                    )
                }

                if (upNextClass != null) {
                    AlertBanner(
                        title = "Up Next: ${upNextClass.courseCode} (${upNextClass.start})",
                        description = "Room: ${upNextClass.room} • Starts in ${upNextClass.minutesLeft} mins",
                        color = AgriAmberText,
                        bgColor = AgriAmberContainer,
                        icon = Icons.Default.Alarm
                    )
                }

                relocatedClasses.forEach { rc ->
                    AlertBanner(
                        title = "Venue Shift: ${rc.courseCode}",
                        description = "Shifted to Room ${rc.room} (${rc.overrideNote ?: "Maintenance"})",
                        color = AgriDanger,
                        bgColor = AgriDangerSoft,
                        icon = Icons.Default.AltRoute
                    )
                }

                if (upcomingExams.isNotEmpty()) {
                    val nextExam = upcomingExams.first()
                    AlertBanner(
                        title = "Assessment Reminder: ${nextExam.courseCode} ${nextExam.type}",
                        description = "Scheduled on ${nextExam.date} at ${nextExam.start} (${nextExam.hall})",
                        color = AgriAmberText,
                        bgColor = AgriAmberContainer.copy(alpha = 0.6f),
                        icon = Icons.Default.Warning
                    )
                }

                if (runningClass == null && upNextClass == null && relocatedClasses.isEmpty() && upcomingExams.isEmpty()) {
                    Text(
                        text = "No pending alerts for current schedule window.",
                        fontSize = 12.sp,
                        color = AgriTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 2: Alert Preferences
                Text(
                    text = "REMINDER PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriPrimaryDark,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("10 mins before class start", fontSize = 12.sp, color = AgriText)
                    Switch(
                        checked = cur10,
                        onCheckedChange = {
                            cur10 = it
                            onToggleSettings(cur10, cur1h)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = AgriPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1 hour before lab / exam", fontSize = 12.sp, color = AgriText)
                    Switch(
                        checked = cur1h,
                        onCheckedChange = {
                            cur1h = it
                            onToggleSettings(cur10, cur1h)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = AgriPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Test notification action button
                Button(
                    onClick = { onSendTestNotification(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriSurface2),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Test Notification to System Bar", fontSize = 12.sp, color = AgriText, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun AlertBanner(
    title: String,
    description: String,
    color: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                Text(text = description, fontSize = 11.sp, color = color.copy(alpha = 0.85f))
            }
        }
    }
}
