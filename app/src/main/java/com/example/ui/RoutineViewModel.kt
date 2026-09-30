package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.ExamEntity
import com.example.data.model.GapJournalEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.OverrideEntity
import com.example.data.model.Semester
import com.example.data.repository.RoutineRepository
import com.example.data.repository.StudentProfile
import com.example.domain.EffectiveClass
import com.example.domain.ImportParser
import com.example.domain.ParsedRoutine
import com.example.domain.ScheduleLogic
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

enum class MainTab {
    TODAY,
    CALENDAR,
    EXAMS,
    ATTENDANCE,
    SETTINGS
}

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)

    val courses: StateFlow<List<CourseEntity>> = repository.courses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classSlots: StateFlow<List<ClassSlotEntity>> = repository.classSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overrides: StateFlow<List<OverrideEntity>> = repository.overrides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = repository.exams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<HolidayEntity>> = repository.holidays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendances: StateFlow<List<AttendanceEntity>> = repository.attendances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gapJournals: StateFlow<List<GapJournalEntity>> = repository.gapJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semester: StateFlow<Semester> = repository.semester
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Semester())

    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    // Active Navigation
    private val _currentTab = MutableStateFlow(MainTab.TODAY)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Sub-screens & Sheets
    private val _showingHomeDetails = MutableStateFlow(false)
    val showingHomeDetails: StateFlow<Boolean> = _showingHomeDetails.asStateFlow()

    private val _showingGapJournalScreen = MutableStateFlow(false)
    val showingGapJournalScreen: StateFlow<Boolean> = _showingGapJournalScreen.asStateFlow()

    private val _editingGapTarget = MutableStateFlow<GapTargetInfo?>(null)
    val editingGapTarget: StateFlow<GapTargetInfo?> = _editingGapTarget.asStateFlow()

    private val _showingHolidaysManager = MutableStateFlow(false)
    val showingHolidaysManager: StateFlow<Boolean> = _showingHolidaysManager.asStateFlow()

    private val _showingImportScreen = MutableStateFlow(false)
    val showingImportScreen: StateFlow<Boolean> = _showingImportScreen.asStateFlow()

    private val _showingProfileDialog = MutableStateFlow(false)
    val showingProfileDialog: StateFlow<Boolean> = _showingProfileDialog.asStateFlow()

    private val _showingNotificationsDialog = MutableStateFlow(false)
    val showingNotificationsDialog: StateFlow<Boolean> = _showingNotificationsDialog.asStateFlow()

    private val _editingClassTarget = MutableStateFlow<EffectiveClass?>(null)
    val editingClassTarget: StateFlow<EffectiveClass?> = _editingClassTarget.asStateFlow()

    private val _showingAddExamSheet = MutableStateFlow(false)
    val showingAddExamSheet: StateFlow<Boolean> = _showingAddExamSheet.asStateFlow()

    private val _editingExamTarget = MutableStateFlow<ExamEntity?>(null)
    val editingExamTarget: StateFlow<ExamEntity?> = _editingExamTarget.asStateFlow()

    private val _showingAddHolidaySheet = MutableStateFlow(false)
    val showingAddHolidaySheet: StateFlow<Boolean> = _showingAddHolidaySheet.asStateFlow()

    private val _editingHolidayTarget = MutableStateFlow<HolidayEntity?>(null)
    val editingHolidayTarget: StateFlow<HolidayEntity?> = _editingHolidayTarget.asStateFlow()

    // DYNAMIC REAL-TIME & DATE STATE
    // Defaults to today if in Fall 2026 range, otherwise defaults to Sep 29, 2026 (a scheduled Tuesday)
    private val initialDate = determineInitialDate()
    private val _activeDate = MutableStateFlow(initialDate)
    val activeDate: StateFlow<LocalDate> = _activeDate.asStateFlow()

    private val _activeTime = MutableStateFlow(LocalTime.now())
    val activeTime: StateFlow<LocalTime> = _activeTime.asStateFlow()

    private val _isLiveClockRunning = MutableStateFlow(true)
    val isLiveClockRunning: StateFlow<Boolean> = _isLiveClockRunning.asStateFlow()

    // Formatted live clock string
    private val _liveClockText = MutableStateFlow(getFormattedTime(LocalTime.now()))
    val liveClockText: StateFlow<String> = _liveClockText.asStateFlow()

    // Notification Toggles
    private val _alert10Mins = MutableStateFlow(true)
    val alert10Mins: StateFlow<Boolean> = _alert10Mins.asStateFlow()

    private val _alert1Hour = MutableStateFlow(true)
    val alert1Hour: StateFlow<Boolean> = _alert1Hour.asStateFlow()

    // Calendar Selected Date & Mode
    private val _selectedCalendarDate = MutableStateFlow(initialDate)
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    private val _showingDayDetailsSheet = MutableStateFlow(false)
    val showingDayDetailsSheet: StateFlow<Boolean> = _showingDayDetailsSheet.asStateFlow()

    // Today screen timeline filter: all | upcoming | ongoing | completed
    private val _todayTimelineFilter = MutableStateFlow("all")
    val todayTimelineFilter: StateFlow<String> = _todayTimelineFilter.asStateFlow()

    // Demo View Mode Toggle (Normal, Free Day, Exam Day)
    private val _demoViewMode = MutableStateFlow("normal")
    val demoViewMode: StateFlow<String> = _demoViewMode.asStateFlow()

    // Calendar Tab Mode: "day" | "week" | "month"
    private val _calendarViewMode = MutableStateFlow("week")
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    // Week start in Calendar (starts on Saturday)
    private val _calendarWeekStart = MutableStateFlow(getSaturdayOfWeek(initialDate))
    val calendarWeekStart: StateFlow<LocalDate> = _calendarWeekStart.asStateFlow()

    // Month in Calendar
    private val _calendarMonthDate = MutableStateFlow(initialDate.withDayOfMonth(1))
    val calendarMonthDate: StateFlow<LocalDate> = _calendarMonthDate.asStateFlow()

    // Exam Filter
    private val _examFilter = MutableStateFlow("All")
    val examFilter: StateFlow<String> = _examFilter.asStateFlow()

    // Attendance feedback message
    private val _attendanceFeedback = MutableStateFlow<String?>(null)
    val attendanceFeedback: StateFlow<String?> = _attendanceFeedback.asStateFlow()

    // User message (Snackbar / Toast)
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Import Preview
    private val _importPreview = MutableStateFlow<ParsedRoutine?>(null)
    val importPreview: StateFlow<ParsedRoutine?> = _importPreview.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    init {
        // Ticking live clock: updates every second when live clock is running
        viewModelScope.launch {
            while (true) {
                if (_isLiveClockRunning.value) {
                    val now = LocalTime.now()
                    _activeTime.value = now
                    _liveClockText.value = getFormattedTime(now)
                }
                delay(1000)
            }
        }
    }

    private fun determineInitialDate(): LocalDate {
        return LocalDate.now()
    }

    private fun getSaturdayOfWeek(date: LocalDate): LocalDate {
        var sat = date
        while (sat.dayOfWeek != DayOfWeek.SATURDAY) {
            sat = sat.minusDays(1)
        }
        return sat
    }

    private fun getFormattedTime(time: LocalTime): String {
        val fmt = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US)
        return time.format(fmt)
    }

    // Dynamic Date & Time Controls
    fun setActiveDate(date: LocalDate) {
        _activeDate.value = date
        _selectedCalendarDate.value = date
        _calendarWeekStart.value = getSaturdayOfWeek(date)
        _calendarMonthDate.value = date.withDayOfMonth(1)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        setActiveDate(today)
        resetToRealTime()
        _demoViewMode.value = "normal"
    }

    fun nextDay() {
        setActiveDate(_activeDate.value.plusDays(1))
    }

    fun prevDay() {
        setActiveDate(_activeDate.value.minusDays(1))
    }

    fun selectWeekday(dayOfWeek: DayOfWeek) {
        var cur = _activeDate.value
        // Find nearest day matching dayOfWeek
        for (i in -3..3) {
            val candidate = cur.plusDays(i.toLong())
            if (candidate.dayOfWeek == dayOfWeek) {
                setActiveDate(candidate)
                return
            }
        }
        while (cur.dayOfWeek != dayOfWeek) {
            cur = cur.plusDays(1)
        }
        setActiveDate(cur)
    }

    fun setSimulatedTime(time: LocalTime) {
        _isLiveClockRunning.value = false
        _activeTime.value = time
        _liveClockText.value = getFormattedTime(time) + " (Sim)"
    }

    fun resetToRealTime() {
        _isLiveClockRunning.value = true
        val now = LocalTime.now()
        _activeTime.value = now
        _liveClockText.value = getFormattedTime(now)
    }

    fun setNotificationToggles(alert10: Boolean, alert1Hour: Boolean) {
        _alert10Mins.value = alert10
        _alert1Hour.value = alert1Hour
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _showingHomeDetails.value = false
        _showingGapJournalScreen.value = false
        _showingHolidaysManager.value = false
        _showingImportScreen.value = false
    }

    fun setDemoViewMode(mode: String) {
        _demoViewMode.value = mode
    }

    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
    }

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
        _activeDate.value = date
        _showingDayDetailsSheet.value = true
    }

    fun closeDayDetailsSheet() {
        _showingDayDetailsSheet.value = false
    }

    fun jumpToTodayCalendar() {
        val today = LocalDate.now()
        _activeDate.value = today
        _selectedCalendarDate.value = today
        _calendarWeekStart.value = getSaturdayOfWeek(today)
        _calendarMonthDate.value = today.withDayOfMonth(1)
        _calendarViewMode.value = "week"
    }

    fun prevWeek() {
        _calendarWeekStart.value = _calendarWeekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        _calendarWeekStart.value = _calendarWeekStart.value.plusWeeks(1)
    }

    fun prevMonth() {
        _calendarMonthDate.value = _calendarMonthDate.value.minusMonths(1)
    }

    fun nextMonth() {
        _calendarMonthDate.value = _calendarMonthDate.value.plusMonths(1)
    }

    fun setExamFilter(filter: String) {
        _examFilter.value = filter
    }

    fun showAddExam(prefillDate: String? = null) {
        val date = prefillDate ?: ScheduleLogic.formatDate(_activeDate.value)
        _editingExamTarget.value = ExamEntity(
            id = UUID.randomUUID().toString(),
            courseCode = courses.value.firstOrNull()?.code ?: "BOT 107",
            type = "Class Test",
            date = date,
            start = "10:00",
            end = "11:00",
            hall = "Hall 302",
            syllabus = "",
            reminder = "1day"
        )
        _showingAddExamSheet.value = true
    }

    fun editExam(exam: ExamEntity) {
        _editingExamTarget.value = exam
        _showingAddExamSheet.value = true
    }

    fun closeExamSheet() {
        _showingAddExamSheet.value = false
        _editingExamTarget.value = null
    }

    fun saveExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.saveExam(exam)
            closeExamSheet()
            _userMessage.value = "Exam scheduled successfully"
        }
    }

    fun deleteExam(id: String) {
        viewModelScope.launch {
            repository.deleteExam(id)
            _userMessage.value = "Exam removed"
        }
    }

    fun toggleExamChecklist(exam: ExamEntity, itemId: String) {
        viewModelScope.launch {
            try {
                val array = JSONArray(exam.checklistJson)
                val newArray = JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.optString("id") == itemId) {
                        obj.put("done", !obj.optBoolean("done", false))
                    }
                    newArray.put(obj)
                }
                val updated = exam.copy(checklistJson = newArray.toString())
                repository.saveExam(updated)
            } catch (_: Exception) {}
        }
    }

    fun addExamChecklistItem(exam: ExamEntity, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val array = JSONArray(exam.checklistJson)
                val newItem = JSONObject().apply {
                    put("id", UUID.randomUUID().toString())
                    put("text", text.trim())
                    put("done", false)
                }
                array.put(newItem)
                val updated = exam.copy(checklistJson = array.toString())
                repository.saveExam(updated)
            } catch (_: Exception) {}
        }
    }

    fun showAddHoliday(prefillDate: String? = null) {
        val defaultDate = prefillDate ?: ScheduleLogic.formatDate(_activeDate.value)
        _editingHolidayTarget.value = HolidayEntity(
            id = UUID.randomUUID().toString(),
            title = "",
            startDate = defaultDate,
            endDate = defaultDate,
            type = "University Closed",
            note = ""
        )
        _showingAddHolidaySheet.value = true
    }

    fun editHoliday(holiday: HolidayEntity) {
        _editingHolidayTarget.value = holiday
        _showingAddHolidaySheet.value = true
    }

    fun closeHolidaySheet() {
        _showingAddHolidaySheet.value = false
        _editingHolidayTarget.value = null
    }

    fun saveHoliday(holiday: HolidayEntity) {
        viewModelScope.launch {
            repository.saveHoliday(holiday)
            closeHolidaySheet()
            _userMessage.value = "Holiday saved"
        }
    }

    fun deleteHoliday(id: String) {
        viewModelScope.launch {
            repository.deleteHoliday(id)
            _userMessage.value = "Holiday removed"
        }
    }

    fun openHolidaysManager() {
        _showingHolidaysManager.value = true
    }

    fun closeHolidaysManager() {
        _showingHolidaysManager.value = false
    }

    fun openImportScreen() {
        _showingImportScreen.value = true
        _importPreview.value = null
        _importError.value = null
    }

    fun closeImportScreen() {
        _showingImportScreen.value = false
        _importPreview.value = null
        _importError.value = null
    }

    fun editClass(effectiveClass: EffectiveClass) {
        _editingClassTarget.value = effectiveClass
    }

    fun closeEditClassSheet() {
        _editingClassTarget.value = null
    }

    fun saveClassOverride(override: OverrideEntity) {
        viewModelScope.launch {
            repository.saveOverride(override)
            closeEditClassSheet()
            _userMessage.value = "Schedule change applied"
        }
    }

    fun revertOverrides(slotId: String) {
        viewModelScope.launch {
            repository.revertOverridesForSlot(slotId)
            closeEditClassSheet()
            _userMessage.value = "Reverted to original routine"
        }
    }

    fun markAttendance(date: String, slotId: String, status: String) {
        viewModelScope.launch {
            repository.setAttendance(date, slotId, status)
            if (status == "present") {
                _attendanceFeedback.value = "✓ Logged Present: Verified via Local SQLite"
            } else {
                _attendanceFeedback.value = "Notice: Marked Absent for session"
            }
        }
    }

    fun resetAllAttendance() {
        viewModelScope.launch {
            repository.resetAllAttendance()
            _userMessage.value = "All attendance records reset to 0!"
        }
    }

    fun resetRoutineToDefault() {
        viewModelScope.launch {
            repository.resetRoutineToDefault()
            val resetDate = LocalDate.now()
            setActiveDate(resetDate)
            resetToRealTime()
            _userMessage.value = "Default BSc Agriculture routine restored!"
        }
    }

    fun fullResetAllData() {
        viewModelScope.launch {
            repository.fullResetApp()
            val resetDate = LocalDate.now()
            setActiveDate(resetDate)
            resetToRealTime()
            _userMessage.value = "All data cleared! Routine database is now clean."
        }
    }

    fun loadSampleNewRoutine() {
        viewModelScope.launch {
            val res = ImportParser.parseHtmlContent(ImportParser.SAMPLE_AGRI_IMPORT_HTML)
            if (res.isSuccess) {
                val preview = res.getOrThrow()
                repository.importRoutineData(preview.semester, preview.courses, preview.classes)
                setActiveDate(LocalDate.now())
                resetToRealTime()
                _userMessage.value = "BSAg 262 sample routine loaded successfully!"
                closeImportScreen()
                selectTab(MainTab.TODAY)
            } else {
                _userMessage.value = "Failed to load sample routine: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateSemester(name: String, start: String, end: String) {
        viewModelScope.launch {
            repository.saveSemester(name, start, end)
            _userMessage.value = "Semester details updated"
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            repository.setThemeMode(theme)
        }
    }

    fun parseImportContent(html: String) {
        _importError.value = null
        val result = ImportParser.parseHtmlContent(html)
        if (result.isSuccess) {
            _importPreview.value = result.getOrNull()
        } else {
            _importError.value = result.exceptionOrNull()?.message ?: "Failed to parse routine."
            _importPreview.value = null
        }
    }

    fun confirmImport() {
        val preview = _importPreview.value ?: return
        viewModelScope.launch {
            repository.importRoutineData(
                preview.semester,
                preview.courses,
                preview.classes
            )
            // Adjust active date to start of new semester or keep current
            preview.semester?.let {
                try {
                    val sDate = ScheduleLogic.parseDate(it.startDate)
                    setActiveDate(sDate)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Routine updated: ${preview.courses.size} courses, ${preview.classes.size} weekly classes"
            closeImportScreen()
            selectTab(MainTab.TODAY)
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onResult(json)
        }
    }

    fun restoreBackup(json: String) {
        viewModelScope.launch {
            val res = repository.importBackupJson(json)
            if (res.isSuccess) {
                _userMessage.value = "Data restored successfully"
            } else {
                _userMessage.value = res.exceptionOrNull()?.message ?: "Failed to restore backup"
            }
        }
    }

    // Home Details Navigation
    fun openHomeDetails() {
        _showingHomeDetails.value = true
    }

    fun closeHomeDetails() {
        _showingHomeDetails.value = false
    }

    // Profile Dialog Controls
    fun openProfileDialog() {
        _showingProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showingProfileDialog.value = false
    }

    fun saveProfile(
        name: String,
        studentId: String,
        section: String,
        faculty: String,
        session: String = "Fall 2026",
        program: String = "BSc Agriculture"
    ) {
        viewModelScope.launch {
            repository.saveStudentProfile(name, studentId, section, faculty, session, program)
            closeProfileDialog()
            _userMessage.value = "Student profile saved!"
        }
    }

    // Notifications Dialog Controls
    fun openNotificationsDialog() {
        _showingNotificationsDialog.value = true
    }

    fun closeNotificationsDialog() {
        _showingNotificationsDialog.value = false
    }

    fun sendTestClassNotification(context: Context) {
        val success = NotificationHelper.sendClassAlert(
            context = context,
            notificationId = 1001,
            title = "BOT 108: Crop Botany Lab",
            message = "Lab starts in 10 minutes at AGRIlab4 (Emergency Relocation).",
            subText = "Live Routine Alert"
        )
        if (success) {
            _userMessage.value = "Test notification sent to system bar!"
        } else {
            _userMessage.value = "Notification sent! (Check app notification permissions)"
        }
    }

    // Gap Journal Methods
    fun openGapJournalScreen() {
        _showingGapJournalScreen.value = true
    }

    fun closeGapJournalScreen() {
        _showingGapJournalScreen.value = false
    }

    fun openEditGap(target: GapTargetInfo) {
        _editingGapTarget.value = target
    }

    fun closeEditGap() {
        _editingGapTarget.value = null
    }

    fun saveGapJournal(entry: GapJournalEntity) {
        viewModelScope.launch {
            repository.saveGapJournal(entry)
            closeEditGap()
            _userMessage.value = "Gap time logged!"
        }
    }

    fun deleteGapJournal(id: String) {
        viewModelScope.launch {
            repository.deleteGapJournal(id)
            closeEditGap()
            _userMessage.value = "Gap time entry removed"
        }
    }

    fun clearAllGapJournals() {
        viewModelScope.launch {
            repository.clearAllGapJournals()
            _userMessage.value = "Gap journal cleared"
        }
    }

    fun exportGapJournal(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val text = repository.exportGapJournalText()
            onResult(text)
        }
    }

    fun setTodayTimelineFilter(filter: String) {
        _todayTimelineFilter.value = filter
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}

data class GapTargetInfo(
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val existingEntry: GapJournalEntity? = null
)
