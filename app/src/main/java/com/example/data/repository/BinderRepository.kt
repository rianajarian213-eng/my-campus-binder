package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.AiSummary
import com.example.data.model.AudioRecording
import com.example.data.model.BinderPage
import com.example.data.model.Course
import com.example.data.model.Semester
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SearchResultItem(
    val id: Long,
    val type: String, // "PAGE", "AUDIO", "SUMMARY", "COURSE"
    val title: String,
    val snippet: String,
    val courseName: String,
    val dateOrMeeting: String,
    val courseId: Long,
    val semesterId: Long?
)

class BinderRepository(private val database: AppDatabase) {
    private val profileDao = database.studentProfileDao()
    private val semesterDao = database.semesterDao()
    private val courseDao = database.courseDao()
    private val binderPageDao = database.binderPageDao()
    private val audioRecordingDao = database.audioRecordingDao()
    private val aiSummaryDao = database.aiSummaryDao()

    // Profile
    val studentProfile: Flow<StudentProfile?> = profileDao.getProfile()
    suspend fun getProfileSync(): StudentProfile? = profileDao.getProfileSync()
    suspend fun saveProfile(profile: StudentProfile) = profileDao.insertOrUpdateProfile(profile)

    // Semesters
    val allSemesters: Flow<List<Semester>> = semesterDao.getAllSemesters()
    val activeSemester: Flow<Semester?> = semesterDao.getActiveSemester()

    suspend fun createSemester(semesterNumber: Int, academicYear: String, setAsActive: Boolean): Long {
        val id = semesterDao.insertSemester(
            Semester(
                semesterNumber = semesterNumber,
                academicYear = academicYear,
                isActive = setAsActive,
                isArchived = !setAsActive
            )
        )
        if (setAsActive) {
            semesterDao.setActiveSemester(id)
        }
        return id
    }

    suspend fun switchActiveSemester(semesterId: Long) {
        semesterDao.setActiveSemester(semesterId)
    }

    suspend fun deleteSemester(semester: Semester) = semesterDao.deleteSemester(semester)

    // Courses
    fun getCoursesBySemester(semesterId: Long): Flow<List<Course>> = courseDao.getCoursesBySemester(semesterId)
    val allActiveCourses: Flow<List<Course>> = courseDao.getAllActiveCourses()
    val allCoursesLifetime: Flow<List<Course>> = courseDao.getAllCoursesLifetime()
    fun getCourseById(id: Long): Flow<Course?> = courseDao.getCourseById(id)
    suspend fun getCourseByIdSync(id: Long): Course? = courseDao.getCourseByIdSync(id)
    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)
    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)
    val totalActiveCoursesCount: Flow<Int> = courseDao.countActiveCourses()

    // Binder Pages
    fun getPagesByCourse(courseId: Long): Flow<List<BinderPage>> = binderPageDao.getPagesByCourse(courseId)
    fun getPageById(id: Long): Flow<BinderPage?> = binderPageDao.getPageById(id)
    suspend fun getPageByIdSync(id: Long): BinderPage? = binderPageDao.getPageByIdSync(id)
    suspend fun insertPage(page: BinderPage): Long = binderPageDao.insertPage(page)
    suspend fun updatePage(page: BinderPage) = binderPageDao.updatePage(page)
    suspend fun deletePage(page: BinderPage) = binderPageDao.deletePage(page)
    val totalPagesCount: Flow<Int> = binderPageDao.countTotalPages()
    val recentPages: Flow<List<BinderPage>> = binderPageDao.getRecentPages(6)

    // Audio Recordings
    fun getRecordingsByCourse(courseId: Long): Flow<List<AudioRecording>> = audioRecordingDao.getRecordingsByCourse(courseId)
    val allRecordings: Flow<List<AudioRecording>> = audioRecordingDao.getAllRecordings()
    suspend fun getRecordingById(id: Long): AudioRecording? = audioRecordingDao.getRecordingById(id)
    suspend fun insertRecording(recording: AudioRecording): Long = audioRecordingDao.insertRecording(recording)
    suspend fun updateRecording(recording: AudioRecording) = audioRecordingDao.updateRecording(recording)
    suspend fun deleteRecording(recording: AudioRecording) = audioRecordingDao.deleteRecording(recording)
    val totalRecordingsCount: Flow<Int> = audioRecordingDao.countRecordings()

    // AI Summaries
    fun getSummariesByCourse(courseId: Long): Flow<List<AiSummary>> = aiSummaryDao.getSummariesByCourse(courseId)
    val allSummaries: Flow<List<AiSummary>> = aiSummaryDao.getAllSummaries()
    suspend fun getSummaryById(id: Long): AiSummary? = aiSummaryDao.getSummaryById(id)
    suspend fun insertSummary(summary: AiSummary): Long = aiSummaryDao.insertSummary(summary)
    suspend fun updateSummary(summary: AiSummary) = aiSummaryDao.updateSummary(summary)
    suspend fun deleteSummary(summary: AiSummary) = aiSummaryDao.deleteSummary(summary)

    // Global Search across Pages, Recordings, and Summaries
    fun globalSearch(query: String): Flow<List<SearchResultItem>> {
        val cleanQuery = query.trim()
        val pagesFlow = binderPageDao.searchPages(cleanQuery)
        val recordingsFlow = audioRecordingDao.searchRecordings(cleanQuery)
        val summariesFlow = aiSummaryDao.searchSummaries(cleanQuery)
        val coursesFlow = courseDao.getAllCoursesLifetime()

        return combine(pagesFlow, recordingsFlow, summariesFlow, coursesFlow) { pages, recordings, summaries, courses ->
            val courseMap = courses.associateBy { it.id }
            val results = mutableListOf<SearchResultItem>()

            pages.forEach { page ->
                val cName = courseMap[page.courseId]?.name ?: "Mata Kuliah"
                results.add(
                    SearchResultItem(
                        id = page.id,
                        type = "PAGE",
                        title = page.title,
                        snippet = extractSnippet(page.content, cleanQuery),
                        courseName = cName,
                        dateOrMeeting = "Pertemuan ${page.meetingNumber} • Hal ${page.pageNumber}",
                        courseId = page.courseId,
                        semesterId = page.semesterId
                    )
                )
            }

            summaries.forEach { summary ->
                val cName = courseMap[summary.courseId]?.name ?: "Mata Kuliah"
                results.add(
                    SearchResultItem(
                        id = summary.id,
                        type = "SUMMARY",
                        title = "AI Resume: ${summary.title}",
                        snippet = extractSnippet("${summary.summaryText}\n${summary.keyPoints}\n${summary.definitions}", cleanQuery),
                        courseName = cName,
                        dateOrMeeting = "Resume AI",
                        courseId = summary.courseId,
                        semesterId = courseMap[summary.courseId]?.semesterId
                    )
                )
            }

            recordings.forEach { rec ->
                val cName = courseMap[rec.courseId]?.name ?: "Mata Kuliah"
                results.add(
                    SearchResultItem(
                        id = rec.id,
                        type = "AUDIO",
                        title = rec.title,
                        snippet = extractSnippet(rec.transcriptionText ?: "Rekaman audio perkuliahan", cleanQuery),
                        courseName = cName,
                        dateOrMeeting = "Rekaman Suara • ${rec.durationSeconds}s",
                        courseId = rec.courseId,
                        semesterId = courseMap[rec.courseId]?.semesterId
                    )
                )
            }

            results
        }
    }

    private fun extractSnippet(fullText: String, query: String): String {
        if (query.isBlank()) return fullText.take(120) + if (fullText.length > 120) "..." else ""
        val index = fullText.indexOf(query, ignoreCase = true)
        return if (index >= 0) {
            val start = (index - 35).coerceAtLeast(0)
            val end = (index + query.length + 65).coerceAtMost(fullText.length)
            (if (start > 0) "..." else "") + fullText.substring(start, end).replace("\n", " ") + (if (end < fullText.length) "..." else "")
        } else {
            fullText.take(120).replace("\n", " ") + if (fullText.length > 120) "..." else ""
        }
    }
}
