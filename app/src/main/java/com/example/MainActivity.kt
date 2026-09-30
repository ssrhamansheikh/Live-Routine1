package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainTab
import com.example.ui.RoutineViewModel
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomNavBar
import com.example.ui.dialogs.AddEditExamDialog
import com.example.ui.dialogs.AddEditHolidayDialog
import com.example.ui.dialogs.EditClassSheet
import com.example.ui.dialogs.GapJournalSheet
import com.example.ui.dialogs.NotificationsDialog
import com.example.ui.dialogs.StudentProfileDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ExamsScreen
import com.example.ui.screens.GapJournalScreen
import com.example.ui.screens.HolidaysScreen
import com.example.ui.screens.HomeDetailsScreen
import com.example.ui.screens.ImportRoutineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.LiveRoutineTheme
import com.example.domain.ClassLiveStatus
import com.example.domain.ScheduleLogic

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveRoutineApp()
        }
    }
}

@Composable
fun LiveRoutineApp(
    viewModel: RoutineViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val showingHomeDetails by viewModel.showingHomeDetails.collectAsState()
    val showingGapJournal by viewModel.showingGapJournalScreen.collectAsState()
    val editingGapTarget by viewModel.editingGapTarget.collectAsState()
    val showingHolidays by viewModel.showingHolidaysManager.collectAsState()
    val showingImport by viewModel.showingImportScreen.collectAsState()
    val showingProfile by viewModel.showingProfileDialog.collectAsState()
    val showingNotifications by viewModel.showingNotificationsDialog.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val liveClock by viewModel.liveClockText.collectAsState()
    val alert10 by viewModel.alert10Mins.collectAsState()
    val alert1h by viewModel.alert1Hour.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()
    val activeTime by viewModel.activeTime.collectAsState()
    val slots by viewModel.classSlots.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val editingClass by viewModel.editingClassTarget.collectAsState()
    val showingAddExam by viewModel.showingAddExamSheet.collectAsState()
    val editingExamTarget by viewModel.editingExamTarget.collectAsState()
    val showingAddHoliday by viewModel.showingAddHolidaySheet.collectAsState()
    val editingHolidayTarget by viewModel.editingHolidayTarget.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val overrides by viewModel.overrides.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.sendTestClassNotification(context)
    }

    val todaySchedule = remember(activeDate, activeTime, courses, slots, overrides, exams, holidays, semester) {
        ScheduleLogic.getDaySchedule(
            dateStr = ScheduleLogic.formatDate(activeDate),
            semester = semester,
            courses = courses,
            slots = slots,
            overrides = overrides,
            exams = exams,
            holidays = holidays,
            attendances = emptyList(),
            currentTime = activeTime,
            isTodayDate = true
        )
    }

    val notificationCount = remember(todaySchedule, exams, activeDate) {
        val todayStr = ScheduleLogic.formatDate(activeDate)
        val classAlerts = todaySchedule.classes.count {
            it.liveStatus == ClassLiveStatus.LIVE ||
            it.liveStatus == ClassLiveStatus.UP_NEXT ||
            it.changeType == "room"
        }
        val examAlerts = exams.count { it.date == todayStr }
        classAlerts + examAlerts
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Back handling for tabs and sub-screens
    if (showingHomeDetails) {
        BackHandler { viewModel.closeHomeDetails() }
    } else if (showingGapJournal) {
        BackHandler { viewModel.closeGapJournalScreen() }
    } else if (showingHolidays) {
        BackHandler { viewModel.closeHolidaysManager() }
    } else if (showingImport) {
        BackHandler { viewModel.closeImportScreen() }
    } else if (currentTab != MainTab.TODAY) {
        BackHandler { viewModel.selectTab(MainTab.TODAY) }
    }

    val isSubScreen = showingHomeDetails || showingGapJournal || showingImport || showingHolidays

    val screenTitle = when {
        showingHomeDetails -> "Home Details"
        showingGapJournal -> "Gap Journal"
        showingImport -> "Import"
        showingHolidays -> "Holidays"
        currentTab == MainTab.TODAY -> "Today"
        currentTab == MainTab.CALENDAR -> "Calendar"
        currentTab == MainTab.EXAMS -> "Exams"
        currentTab == MainTab.ATTENDANCE -> "Attendance"
        currentTab == MainTab.SETTINGS -> "Settings"
        else -> "Live Routine"
    }

    LiveRoutineTheme(themeMode = themeMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!isSubScreen) {
                    AppTopBar(
                        screenTitle = screenTitle,
                        liveTime = liveClock,
                        notificationCount = notificationCount,
                        profile = studentProfile,
                        onOpenNotifications = { viewModel.openNotificationsDialog() },
                        onOpenProfile = { viewModel.openProfileDialog() },
                        onOpenSettings = { viewModel.selectTab(MainTab.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                if (!isSubScreen) {
                    BottomNavBar(
                        selectedTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    showingHomeDetails -> {
                        HomeDetailsScreen(viewModel = viewModel)
                    }
                    showingGapJournal -> {
                        GapJournalScreen(viewModel = viewModel)
                    }
                    showingImport -> {
                        ImportRoutineScreen(viewModel = viewModel)
                    }
                    showingHolidays -> {
                        HolidaysScreen(viewModel = viewModel)
                    }
                    else -> {
                        when (currentTab) {
                            MainTab.TODAY -> TodayScreen(viewModel = viewModel)
                            MainTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                            MainTab.EXAMS -> ExamsScreen(viewModel = viewModel)
                            MainTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                            MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Sheets & Dialogs
            editingGapTarget?.let { gapTarget ->
                GapJournalSheet(
                    target = gapTarget,
                    onDismiss = { viewModel.closeEditGap() },
                    onSave = { viewModel.saveGapJournal(it) },
                    onDelete = { viewModel.deleteGapJournal(it) }
                )
            }

            editingClass?.let { targetClass ->
                EditClassSheet(
                    targetClass = targetClass,
                    courses = courses,
                    onDismiss = { viewModel.closeEditClassSheet() },
                    onSaveOverride = { viewModel.saveClassOverride(it) },
                    onRevert = { viewModel.revertOverrides(it) }
                )
            }

            if (showingAddExam) {
                AddEditExamDialog(
                    initialExam = editingExamTarget,
                    courses = courses,
                    onDismiss = { viewModel.closeExamSheet() },
                    onSaveExam = { viewModel.saveExam(it) }
                )
            }

            if (showingAddHoliday) {
                AddEditHolidayDialog(
                    initialHoliday = editingHolidayTarget,
                    onDismiss = { viewModel.closeHolidaySheet() },
                    onSaveHoliday = { viewModel.saveHoliday(it) }
                )
            }

            if (showingProfile) {
                StudentProfileDialog(
                    profile = studentProfile,
                    onDismiss = { viewModel.closeProfileDialog() },
                    onSave = { name, id, sec, fac, sess, prog ->
                        viewModel.saveProfile(name, id, sec, fac, sess, prog)
                    }
                )
            }

            if (showingNotifications) {
                NotificationsDialog(
                    activeClasses = todaySchedule.classes,
                    upcomingExams = exams,
                    alert10Mins = alert10,
                    alert1Hour = alert1h,
                    onToggleSettings = { a10, a1h ->
                        viewModel.setNotificationToggles(a10, a1h)
                    },
                    onSendTestNotification = { ctx ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                viewModel.sendTestClassNotification(ctx)
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.sendTestClassNotification(ctx)
                        }
                    },
                    onDismiss = { viewModel.closeNotificationsDialog() }
                )
            }
        }
    }
}
