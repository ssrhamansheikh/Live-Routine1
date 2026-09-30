package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.ExamEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.OverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY code ASC")
    suspend fun getAllCoursesList(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE code = :code LIMIT 1")
    fun getCourseByCode(code: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE code = :code LIMIT 1")
    suspend fun getCourseByCodeSync(code: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE code = :code")
    suspend fun deleteCourseByCode(code: String)

    @Query("DELETE FROM courses")
    suspend fun clearAll()
}

@Dao
interface ClassSlotDao {
    @Query("SELECT * FROM class_slots ORDER BY day ASC, start ASC")
    fun getAllSlots(): Flow<List<ClassSlotEntity>>

    @Query("SELECT * FROM class_slots ORDER BY day ASC, start ASC")
    suspend fun getAllSlotsList(): List<ClassSlotEntity>

    @Query("SELECT * FROM class_slots WHERE day = :day ORDER BY start ASC")
    fun getSlotsByDay(day: String): Flow<List<ClassSlotEntity>>

    @Query("SELECT * FROM class_slots WHERE day = :day ORDER BY start ASC")
    suspend fun getSlotsByDaySync(day: String): List<ClassSlotEntity>

    @Query("SELECT * FROM class_slots WHERE courseCode = :courseCode")
    fun getSlotsByCourse(courseCode: String): Flow<List<ClassSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<ClassSlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: ClassSlotEntity)

    @Update
    suspend fun updateSlot(slot: ClassSlotEntity)

    @Query("DELETE FROM class_slots WHERE id = :id")
    suspend fun deleteSlot(id: String)

    @Query("DELETE FROM class_slots WHERE courseCode = :courseCode")
    suspend fun deleteSlotsForCourse(courseCode: String)

    @Query("DELETE FROM class_slots")
    suspend fun clearAll()
}

@Dao
interface OverrideDao {
    @Query("SELECT * FROM overrides")
    fun getAllOverrides(): Flow<List<OverrideEntity>>

    @Query("SELECT * FROM overrides")
    suspend fun getAllOverridesList(): List<OverrideEntity>

    @Query("SELECT * FROM overrides WHERE date = :date")
    fun getOverridesForDate(date: String): Flow<List<OverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: OverrideEntity)

    @Query("DELETE FROM overrides WHERE id = :id")
    suspend fun deleteOverride(id: String)

    @Query("DELETE FROM overrides WHERE slotId = :slotId")
    suspend fun deleteOverridesForSlot(slotId: String)

    @Query("DELETE FROM overrides")
    suspend fun clearAll()
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY date ASC, start ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY date ASC, start ASC")
    suspend fun getAllExamsList(): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    fun getExamById(id: String): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamByIdSync(id: String): ExamEntity?

    @Query("SELECT * FROM exams WHERE date = :date ORDER BY start ASC")
    fun getExamsForDate(date: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE courseCode = :courseCode ORDER BY date ASC")
    fun getExamsByCourse(courseCode: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: String)

    @Query("DELETE FROM exams")
    suspend fun clearAll()
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY startDate ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays ORDER BY startDate ASC")
    suspend fun getAllHolidaysList(): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays WHERE id = :id")
    suspend fun deleteHoliday(id: String)

    @Query("DELETE FROM holidays")
    suspend fun clearAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceList(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceForDateSync(date: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE `key` = :key LIMIT 1")
    suspend fun getAttendanceByKeySync(key: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAttendance(record: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE `key` = :key")
    suspend fun deleteAttendance(key: String)

    @Query("DELETE FROM attendance WHERE date = :date")
    suspend fun deleteAttendanceForDate(date: String)

    @Query("DELETE FROM attendance")
    suspend fun clearAll()
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingSync(key: String): AppSettingEntity?

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)

    @Query("DELETE FROM app_settings")
    suspend fun clearAll()
}

@Dao
interface GapJournalDao {
    @Query("SELECT * FROM gap_journal ORDER BY date DESC, startTime DESC")
    fun getAllGapJournals(): Flow<List<com.example.data.model.GapJournalEntity>>

    @Query("SELECT * FROM gap_journal WHERE date = :date ORDER BY startTime ASC")
    fun getGapJournalsForDate(date: String): Flow<List<com.example.data.model.GapJournalEntity>>

    @Query("SELECT * FROM gap_journal WHERE date = :date ORDER BY startTime ASC")
    suspend fun getGapJournalsForDateList(date: String): List<com.example.data.model.GapJournalEntity>

    @Query("SELECT * FROM gap_journal ORDER BY date DESC, startTime DESC")
    suspend fun getAllGapJournalsList(): List<com.example.data.model.GapJournalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGapJournal(entry: com.example.data.model.GapJournalEntity)

    @Query("DELETE FROM gap_journal WHERE id = :id")
    suspend fun deleteGapJournal(id: String)

    @Query("DELETE FROM gap_journal")
    suspend fun clearAll()
}

@Database(
    entities = [
        CourseEntity::class,
        ClassSlotEntity::class,
        OverrideEntity::class,
        ExamEntity::class,
        HolidayEntity::class,
        AttendanceEntity::class,
        AppSettingEntity::class,
        com.example.data.model.GapJournalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun classSlotDao(): ClassSlotDao
    abstract fun overrideDao(): OverrideDao
    abstract fun examDao(): ExamDao
    abstract fun holidayDao(): HolidayDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun settingDao(): AppSettingDao
    abstract fun gapJournalDao(): GapJournalDao
}
