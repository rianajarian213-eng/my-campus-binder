package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AiSummary
import com.example.data.model.AudioRecording
import com.example.data.model.BinderPage
import com.example.data.model.Course
import com.example.data.model.Semester
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfile)
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY semesterNumber ASC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semesters WHERE isActive = 1 LIMIT 1")
    fun getActiveSemester(): Flow<Semester?>

    @Query("SELECT * FROM semesters WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSemesterSync(): Semester?

    @Query("SELECT * FROM semesters WHERE id = :id LIMIT 1")
    suspend fun getSemesterById(id: Long): Semester?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester): Long

    @Update
    suspend fun updateSemester(semester: Semester)

    @Query("UPDATE semesters SET isActive = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveSemester(activeId: Long)

    @Delete
    suspend fun deleteSemester(semester: Semester)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId AND isArchived = 0 ORDER BY orderIndex ASC, name ASC")
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE isArchived = 0 ORDER BY orderIndex ASC, name ASC")
    fun getAllActiveCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY semesterId ASC, name ASC")
    fun getAllCoursesLifetime(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    fun getCourseById(id: Long): Flow<Course?>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseByIdSync(id: Long): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("SELECT COUNT(*) FROM courses WHERE isArchived = 0")
    fun countActiveCourses(): Flow<Int>
}

@Dao
interface BinderPageDao {
    @Query("SELECT * FROM binder_pages WHERE courseId = :courseId ORDER BY meetingNumber ASC, pageNumber ASC, id ASC")
    fun getPagesByCourse(courseId: Long): Flow<List<BinderPage>>

    @Query("SELECT * FROM binder_pages WHERE id = :id LIMIT 1")
    fun getPageById(id: Long): Flow<BinderPage?>

    @Query("SELECT * FROM binder_pages WHERE id = :id LIMIT 1")
    suspend fun getPageByIdSync(id: Long): BinderPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: BinderPage): Long

    @Update
    suspend fun updatePage(page: BinderPage)

    @Delete
    suspend fun deletePage(page: BinderPage)

    @Query("SELECT COUNT(*) FROM binder_pages")
    fun countTotalPages(): Flow<Int>

    @Query("SELECT * FROM binder_pages ORDER BY lastModified DESC LIMIT :limit")
    fun getRecentPages(limit: Int = 5): Flow<List<BinderPage>>

    // Search inside notes text and title
    @Query("SELECT * FROM binder_pages WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    fun searchPages(query: String): Flow<List<BinderPage>>
}

@Dao
interface AudioRecordingDao {
    @Query("SELECT * FROM audio_recordings WHERE courseId = :courseId ORDER BY dateRecorded DESC")
    fun getRecordingsByCourse(courseId: Long): Flow<List<AudioRecording>>

    @Query("SELECT * FROM audio_recordings ORDER BY dateRecorded DESC")
    fun getAllRecordings(): Flow<List<AudioRecording>>

    @Query("SELECT * FROM audio_recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Long): AudioRecording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: AudioRecording): Long

    @Update
    suspend fun updateRecording(recording: AudioRecording)

    @Delete
    suspend fun deleteRecording(recording: AudioRecording)

    @Query("SELECT COUNT(*) FROM audio_recordings")
    fun countRecordings(): Flow<Int>

    // Search transcripts and titles
    @Query("SELECT * FROM audio_recordings WHERE title LIKE '%' || :query || '%' OR transcriptionText LIKE '%' || :query || '%' ORDER BY dateRecorded DESC")
    fun searchRecordings(query: String): Flow<List<AudioRecording>>
}

@Dao
interface AiSummaryDao {
    @Query("SELECT * FROM ai_summaries WHERE courseId = :courseId ORDER BY dateCreated DESC")
    fun getSummariesByCourse(courseId: Long): Flow<List<AiSummary>>

    @Query("SELECT * FROM ai_summaries ORDER BY dateCreated DESC")
    fun getAllSummaries(): Flow<List<AiSummary>>

    @Query("SELECT * FROM ai_summaries WHERE id = :id LIMIT 1")
    suspend fun getSummaryById(id: Long): AiSummary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: AiSummary): Long

    @Update
    suspend fun updateSummary(summary: AiSummary)

    @Delete
    suspend fun deleteSummary(summary: AiSummary)

    @Query("SELECT * FROM ai_summaries WHERE title LIKE '%' || :query || '%' OR summaryText LIKE '%' || :query || '%' OR fullTranscript LIKE '%' || :query || '%' OR keyPoints LIKE '%' || :query || '%' ORDER BY dateCreated DESC")
    fun searchSummaries(query: String): Flow<List<AiSummary>>
}
