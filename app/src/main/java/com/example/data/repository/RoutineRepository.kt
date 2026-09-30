package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.ExamEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.OverrideEntity
import com.example.data.model.Semester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RoutineRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "live_routine_db"
    ).fallbackToDestructiveMigration().build()

    private val courseDao = db.courseDao()
    private val classSlotDao = db.classSlotDao()
    private val overrideDao = db.overrideDao()
    private val examDao = db.examDao()
    private val holidayDao = db.holidayDao()
    private val attendanceDao = db.attendanceDao()
    private val settingDao = db.settingDao()
    private val gapJournalDao = db.gapJournalDao()

    val courses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val classSlots: Flow<List<ClassSlotEntity>> = classSlotDao.getAllSlots()
    val overrides: Flow<List<OverrideEntity>> = overrideDao.getAllOverrides()
    val exams: Flow<List<ExamEntity>> = examDao.getAllExams()
    val holidays: Flow<List<HolidayEntity>> = holidayDao.getAllHolidays()
    val attendances: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()
    val gapJournals: Flow<List<com.example.data.model.GapJournalEntity>> = gapJournalDao.getAllGapJournals()

    val semester: Flow<Semester> = settingDao.getAllSettings().map { settings ->
        val map = settings.associate { it.key to it.value }
        Semester(
            name = map["semester_name"] ?: "Fall 2026",
            startDate = map["semester_start"] ?: "2026-10-03",
            endDate = map["semester_end"] ?: "2027-02-28"
        )
    }

    val themeMode: Flow<String> = settingDao.getSetting("theme_mode").map {
        it?.value ?: "system"
    }

    val studentProfile: Flow<StudentProfile> = settingDao.getAllSettings().map { settings ->
        val map = settings.associate { it.key to it.value }
        StudentProfile(
            name = map["student_name"] ?: "Fatema Tuz Zohra",
            studentId = map["student_id"] ?: "AGR-2026-0428",
            section = map["student_section"] ?: "Section C",
            faculty = map["student_faculty"] ?: "Faculty of Agriculture",
            session = map["student_session"] ?: map["semester_name"] ?: "Fall 2026",
            program = map["student_program"] ?: "BSc Agriculture"
        )
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedDefaults()
        }
    }

    suspend fun checkAndSeedDefaults() {
        val userInitialized = settingDao.getSettingSync("user_initialized")?.value == "true"
        if (!userInitialized) {
            val existing = courseDao.getAllCoursesList()
            if (existing.isEmpty()) {
                seedDefaultRoutine(initialLaunch = true)
            }
            saveSemester("Fall 2026", "2026-10-03", "2027-02-28")
            settingDao.setSetting(AppSettingEntity("user_initialized", "true"))
        } else {
            val currentStart = settingDao.getSettingSync("semester_start")?.value
            if (currentStart == null || currentStart == "2026-09-05") {
                saveSemester("Fall 2026", "2026-10-03", "2027-02-28")
            }
        }
    }

    suspend fun seedDefaultRoutine(initialLaunch: Boolean = false) {
        val defaultCourses = listOf(
            CourseEntity("MAT 107", "Mathematics", "O", 4.0, "theory", "Arnab Mukherjee", "CAAS_AM"),
            CourseEntity("ENG 102", "English Comprehension and Speaking", "Z", 3.0, "theory", "Ms. Fatema Tasnim", "DEML_FT"),
            CourseEntity("BOT 107", "Crop Botany", "C", 3.0, "theory", "Dr. Syada Nizer Sultana", "BSAg_DSNS"),
            CourseEntity("BOT 108", "Crop Botany Lab", "C", 1.0, "lab", "Dr. Syada Nizer Sultana", "BSAg_DSNS"),
            CourseEntity("AGR 101", "Agronomy", "E", 3.0, "theory", "Dr. Mohammad Rezaul Karim", "BSAg_DMRK"),
            CourseEntity("AGR 102", "Agronomy Lab", "E", 1.0, "lab", "Dr. Mohammad Rezaul Karim", "BSAg_DMRK")
        )

        val defaultSlots = listOf(
            // SAT: 10:40-11:40 BOT 107 Room 601; 13:10-14:10 AGR 101 Room 521; 15:20-16:20 MAT 107 Room 805
            ClassSlotEntity("sat_1", "SAT", "10:40", "11:40", "BOT 107", "601"),
            ClassSlotEntity("sat_2", "SAT", "13:10", "14:10", "AGR 101", "521"),
            ClassSlotEntity("sat_3", "SAT", "15:20", "16:20", "MAT 107", "805"),

            // SUN: 10:40-11:40 BOT 107 Room 520; 13:10-14:10 AGR 101 Room 521; 15:20-16:20 MAT 107 Room 805; 16:25-17:25 ENG 102 Room 307
            ClassSlotEntity("sun_1", "SUN", "10:40", "11:40", "BOT 107", "520"),
            ClassSlotEntity("sun_2", "SUN", "13:10", "14:10", "AGR 101", "521"),
            ClassSlotEntity("sun_3", "SUN", "15:20", "16:20", "MAT 107", "805"),
            ClassSlotEntity("sun_4", "SUN", "16:25", "17:25", "ENG 102", "307"),

            // MON: 10:40-11:40 BOT 107 Room 1003; 13:10-14:10 AGR 101 Room 310
            ClassSlotEntity("mon_1", "MON", "10:40", "11:40", "BOT 107", "1003"),
            ClassSlotEntity("mon_2", "MON", "13:10", "14:10", "AGR 101", "310"),

            // TUE: 10:40-11:40 BOT 108 Room AGRIlab4; 11:45-12:45 BOT 108 Room AGRIlab4; 15:20-16:20 MAT 107 Room 805; 16:25-17:25 ENG 102 Room 307
            ClassSlotEntity("tue_1", "TUE", "10:40", "11:40", "BOT 108", "AGRIlab4"),
            ClassSlotEntity("tue_2", "TUE", "11:45", "12:45", "BOT 108", "AGRIlab4"),
            ClassSlotEntity("tue_3", "TUE", "15:20", "16:20", "MAT 107", "805"),
            ClassSlotEntity("tue_4", "TUE", "16:25", "17:25", "ENG 102", "307"),

            // WED: 13:10-14:10 AGR 102 Room AGRIlab1; 14:15-15:15 AGR 102 Room AGRIlab1; 15:20-16:20 MAT 107 Room 805; 16:25-17:25 ENG 102 Room 307
            ClassSlotEntity("wed_1", "WED", "13:10", "14:10", "AGR 102", "AGRIlab1"),
            ClassSlotEntity("wed_2", "WED", "14:15", "15:15", "AGR 102", "AGRIlab1"),
            ClassSlotEntity("wed_3", "WED", "15:20", "16:20", "MAT 107", "805"),
            ClassSlotEntity("wed_4", "WED", "16:25", "17:25", "ENG 102", "307")
        )

        courseDao.clearAll()
        courseDao.insertCourses(defaultCourses)

        classSlotDao.clearAll()
        classSlotDao.insertSlots(defaultSlots)

        overrideDao.clearAll()
        examDao.clearAll()
        attendanceDao.clearAll()

        // Seed default holidays if empty
        if (holidayDao.getAllHolidaysList().isEmpty()) {
            val defaultHolidays = listOf(
                HolidayEntity(
                    id = "holiday_durga_puja",
                    title = "Durga Puja & Autumn Recess",
                    startDate = "2026-10-18",
                    endDate = "2026-10-21",
                    type = "University Closed",
                    note = "Campus and academic departments closed."
                ),
                HolidayEntity(
                    id = "holiday_victory_day",
                    title = "National Victory Day",
                    startDate = "2026-12-16",
                    endDate = "2026-12-16",
                    type = "National Holiday",
                    note = "Official National Holiday celebration."
                ),
                HolidayEntity(
                    id = "holiday_winter_break",
                    title = "Winter & Semester End Recess",
                    startDate = "2026-12-25",
                    endDate = "2026-12-31",
                    type = "University Closed",
                    note = "Term break before winter semester."
                )
            )
            defaultHolidays.forEach { holidayDao.insertHoliday(it) }
        }

        // Exams and attendance start completely fresh and empty
        examDao.clearAll()
        attendanceDao.clearAll()
    }

    suspend fun saveGapJournal(entry: com.example.data.model.GapJournalEntity) {
        gapJournalDao.insertGapJournal(entry)
    }

    suspend fun deleteGapJournal(id: String) {
        gapJournalDao.deleteGapJournal(id)
    }

    suspend fun clearAllGapJournals() {
        gapJournalDao.clearAll()
    }

    suspend fun exportGapJournalText(): String {
        val list = gapJournalDao.getAllGapJournalsList()
        val sb = StringBuilder()
        sb.append("LIVE ROUTINE — GAP TIME JOURNAL EXPORT\n")
        sb.append("=====================================\n\n")
        val byDate = list.groupBy { it.date }
        for ((d, entries) in byDate) {
            sb.append("Date: ").append(d).append("\n")
            for (e in entries) {
                sb.append(" • [").append(e.startTime).append(" - ").append(e.endTime)
                    .append(" (").append(e.durationMinutes).append("m)] [")
                    .append(e.tag).append("]: ").append(e.note).append("\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    suspend fun resetRoutineToDefault() {
        saveSemester("Fall 2026", "2026-10-03", "2027-02-28")
        seedDefaultRoutine(initialLaunch = true)
    }

    suspend fun fullResetApp() {
        courseDao.clearAll()
        classSlotDao.clearAll()
        overrideDao.clearAll()
        examDao.clearAll()
        holidayDao.clearAll()
        attendanceDao.clearAll()
        gapJournalDao.clearAll()
        settingDao.clearAll()
        saveSemester("Fall 2026", "2026-10-03", "2027-02-28")
        saveStudentProfile("Fatema Tuz Zohra", "AGR-2026-0428", "Section C", "Faculty of Agriculture", "Fall 2026", "BSc Agriculture")
        settingDao.setSetting(AppSettingEntity("user_initialized", "true"))
    }

    suspend fun setAttendance(date: String, slotId: String, status: String) {
        val key = "$date|$slotId"
        attendanceDao.setAttendance(AttendanceEntity(key, date, slotId, status))
    }

    suspend fun removeAttendance(date: String, slotId: String) {
        attendanceDao.deleteAttendance("$date|$slotId")
    }

    suspend fun resetAllAttendance() {
        attendanceDao.clearAll()
    }

    suspend fun saveOverride(override: OverrideEntity) {
        overrideDao.insertOverride(override)
    }

    suspend fun deleteOverride(id: String) {
        overrideDao.deleteOverride(id)
    }

    suspend fun revertOverridesForSlot(slotId: String) {
        overrideDao.deleteOverridesForSlot(slotId)
    }

    suspend fun saveExam(exam: ExamEntity) {
        examDao.insertExam(exam)
    }

    suspend fun deleteExam(id: String) {
        examDao.deleteExam(id)
    }

    suspend fun saveHoliday(holiday: HolidayEntity) {
        holidayDao.insertHoliday(holiday)
    }

    suspend fun deleteHoliday(id: String) {
        holidayDao.deleteHoliday(id)
    }

    suspend fun saveSemester(name: String, startDate: String, endDate: String) {
        settingDao.setSetting(AppSettingEntity("semester_name", name))
        settingDao.setSetting(AppSettingEntity("semester_start", startDate))
        settingDao.setSetting(AppSettingEntity("semester_end", endDate))
    }

    suspend fun setThemeMode(mode: String) {
        settingDao.setSetting(AppSettingEntity("theme_mode", mode))
    }

    suspend fun exportBackupJson(): String {
        val coursesList = courseDao.getAllCoursesList()
        val slotsList = classSlotDao.getAllSlotsList()
        val overridesList = overrideDao.getAllOverridesList()
        val examsList = examDao.getAllExamsList()
        val holidaysList = holidayDao.getAllHolidaysList()
        val attendanceList = attendanceDao.getAllAttendanceList()

        val root = JSONObject()
        root.put("app", "live-routine")
        root.put("version", 1)

        val cArray = JSONArray()
        coursesList.forEach { c ->
            val obj = JSONObject()
            obj.put("code", c.code)
            obj.put("title", c.title)
            obj.put("section", c.section)
            obj.put("credits", c.credits)
            obj.put("type", c.type)
            val inst = JSONObject()
            inst.put("name", c.instructorName)
            inst.put("shortCode", c.instructorShortCode)
            obj.put("instructor", inst)
            cArray.put(obj)
        }
        root.put("courses", cArray)

        val sArray = JSONArray()
        slotsList.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("day", s.day)
            obj.put("start", s.start)
            obj.put("end", s.end)
            obj.put("courseCode", s.courseCode)
            obj.put("room", s.room)
            sArray.put(obj)
        }
        root.put("classes", sArray)

        val oArray = JSONArray()
        overridesList.forEach { o ->
            val obj = JSONObject()
            obj.put("id", o.id)
            obj.put("slotId", o.slotId)
            obj.put("courseCode", o.courseCode)
            obj.put("changeType", o.changeType)
            obj.put("scope", o.scope)
            obj.put("date", o.date)
            obj.put("fromDate", o.fromDate)
            obj.put("newRoom", o.newRoom)
            obj.put("newStart", o.newStart)
            obj.put("newEnd", o.newEnd)
            obj.put("note", o.note)
            oArray.put(obj)
        }
        root.put("overrides", oArray)

        val eArray = JSONArray()
        examsList.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("courseCode", e.courseCode)
            obj.put("type", e.type)
            obj.put("date", e.date)
            obj.put("start", e.start)
            obj.put("end", e.end)
            obj.put("hall", e.hall)
            obj.put("syllabus", e.syllabus)
            obj.put("reminder", e.reminder)
            obj.put("checklist", JSONArray(e.checklistJson))
            eArray.put(obj)
        }
        root.put("exams", eArray)

        val hArray = JSONArray()
        holidaysList.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("title", h.title)
            obj.put("startDate", h.startDate)
            obj.put("endDate", h.endDate)
            obj.put("type", h.type)
            obj.put("note", h.note)
            hArray.put(obj)
        }
        root.put("holidays", hArray)

        val aArray = JSONArray()
        attendanceList.forEach { a ->
            val obj = JSONObject()
            obj.put("key", a.key)
            obj.put("date", a.date)
            obj.put("slotId", a.slotId)
            obj.put("status", a.status)
            aArray.put(obj)
        }
        root.put("attendance", aArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            if (root.optString("app") != "live-routine") {
                return Result.failure(Exception("Not a valid Live Routine backup file"))
            }

            if (root.has("courses")) {
                val cArray = root.getJSONArray("courses")
                val list = mutableListOf<CourseEntity>()
                for (i in 0 until cArray.length()) {
                    val obj = cArray.getJSONObject(i)
                    val inst = obj.optJSONObject("instructor")
                    list.add(
                        CourseEntity(
                            code = obj.getString("code"),
                            title = obj.getString("title"),
                            section = obj.optString("section", "A"),
                            credits = obj.optDouble("credits", 3.0),
                            type = obj.optString("type", "theory"),
                            instructorName = inst?.optString("name") ?: obj.optString("instructorName", ""),
                            instructorShortCode = inst?.optString("shortCode") ?: obj.optString("instructorShortCode", "")
                        )
                    )
                }
                courseDao.clearAll()
                courseDao.insertCourses(list)
            }

            if (root.has("classes")) {
                val sArray = root.getJSONArray("classes")
                val list = mutableListOf<ClassSlotEntity>()
                for (i in 0 until sArray.length()) {
                    val obj = sArray.getJSONObject(i)
                    list.add(
                        ClassSlotEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            day = obj.getString("day"),
                            start = obj.getString("start"),
                            end = obj.getString("end"),
                            courseCode = obj.getString("courseCode"),
                            room = obj.getString("room")
                        )
                    )
                }
                classSlotDao.clearAll()
                classSlotDao.insertSlots(list)
            }

            if (root.has("overrides")) {
                val oArray = root.getJSONArray("overrides")
                overrideDao.clearAll()
                for (i in 0 until oArray.length()) {
                    val obj = oArray.getJSONObject(i)
                    overrideDao.insertOverride(
                        OverrideEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            slotId = if (obj.isNull("slotId")) null else obj.optString("slotId"),
                            courseCode = obj.getString("courseCode"),
                            changeType = obj.getString("changeType"),
                            scope = obj.getString("scope"),
                            date = obj.getString("date"),
                            fromDate = if (obj.isNull("fromDate")) null else obj.optString("fromDate"),
                            newRoom = if (obj.isNull("newRoom")) null else obj.optString("newRoom"),
                            newStart = if (obj.isNull("newStart")) null else obj.optString("newStart"),
                            newEnd = if (obj.isNull("newEnd")) null else obj.optString("newEnd"),
                            note = if (obj.isNull("note")) null else obj.optString("note")
                        )
                    )
                }
            }

            if (root.has("exams")) {
                val eArray = root.getJSONArray("exams")
                examDao.clearAll()
                for (i in 0 until eArray.length()) {
                    val obj = eArray.getJSONObject(i)
                    val cl = obj.optJSONArray("checklist")?.toString() ?: "[]"
                    examDao.insertExam(
                        ExamEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            courseCode = obj.getString("courseCode"),
                            type = obj.getString("type"),
                            date = obj.getString("date"),
                            start = obj.getString("start"),
                            end = obj.getString("end"),
                            hall = obj.optString("hall", ""),
                            syllabus = obj.optString("syllabus", ""),
                            reminder = obj.optString("reminder", "1day"),
                            checklistJson = cl
                        )
                    )
                }
            }

            if (root.has("holidays")) {
                val hArray = root.getJSONArray("holidays")
                holidayDao.clearAll()
                for (i in 0 until hArray.length()) {
                    val obj = hArray.getJSONObject(i)
                    holidayDao.insertHoliday(
                        HolidayEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.getString("title"),
                            startDate = obj.getString("startDate"),
                            endDate = obj.getString("endDate"),
                            type = obj.optString("type", "Public Holiday"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            if (root.has("attendance")) {
                val aArray = root.getJSONArray("attendance")
                attendanceDao.clearAll()
                for (i in 0 until aArray.length()) {
                    val obj = aArray.getJSONObject(i)
                    attendanceDao.setAttendance(
                        AttendanceEntity(
                            key = obj.getString("key"),
                            date = obj.getString("date"),
                            slotId = obj.getString("slotId"),
                            status = obj.getString("status")
                        )
                    )
                }
            }

            Result.success("Restored successfully from backup")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importRoutineData(
        newSemester: Semester?,
        newCourses: List<CourseEntity>,
        newSlots: List<ClassSlotEntity>
    ) {
        if (newSemester != null) {
            saveSemester(newSemester.name, newSemester.startDate, newSemester.endDate)
        }
        courseDao.clearAll()
        courseDao.insertCourses(newCourses)
        classSlotDao.clearAll()
        classSlotDao.insertSlots(newSlots)
        overrideDao.clearAll() // Overrides are cleared as requested
        // Keeps exams, holidays and attendance!
    }

    suspend fun saveStudentProfile(
        name: String,
        studentId: String,
        section: String,
        faculty: String,
        session: String = "Fall 2026",
        program: String = "BSc Agriculture"
    ) {
        settingDao.setSetting(AppSettingEntity("student_name", name))
        settingDao.setSetting(AppSettingEntity("student_id", studentId))
        settingDao.setSetting(AppSettingEntity("student_section", section))
        settingDao.setSetting(AppSettingEntity("student_faculty", faculty))
        settingDao.setSetting(AppSettingEntity("student_session", session))
        settingDao.setSetting(AppSettingEntity("student_program", program))
    }
}

data class StudentProfile(
    val name: String = "Fatema Tuz Zohra",
    val studentId: String = "AGR-2026-0428",
    val section: String = "Section C",
    val faculty: String = "Faculty of Agriculture",
    val session: String = "Fall 2026",
    val program: String = "BSc Agriculture"
)

