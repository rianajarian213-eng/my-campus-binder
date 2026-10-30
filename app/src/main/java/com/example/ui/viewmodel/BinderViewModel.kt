package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiResult
import com.example.ai.AiService
import com.example.ai.ProcessedLecturePackage
import com.example.data.database.AppDatabase
import com.example.data.model.AiSummary
import com.example.data.model.AudioRecording
import com.example.data.model.BinderPage
import com.example.data.model.Course
import com.example.data.model.PaperType
import com.example.data.model.Semester
import com.example.data.model.StudentProfile
import com.example.data.model.SummaryLength
import com.example.data.repository.BinderRepository
import com.example.data.repository.SearchResultItem
import com.example.util.AudioPlayerManager
import com.example.util.AudioRecorderManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ScreenTab {
    HOME,
    BINDERS,
    RECORD,
    AI_ASSISTANT,
    PROFILE
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data class Processing(val message: String) : AiUiState
    data class PackageResult(val pkg: ProcessedLecturePackage, val targetCourseId: Long, val meetingNo: Int) : AiUiState
    data class AssistantResponse(val answer: String) : AiUiState
    data class Error(val message: String) : AiUiState
    data class NotConfigured(val message: String) : AiUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class BinderViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = BinderRepository(database)
    val recorderManager = AudioRecorderManager(application)
    val playerManager = AudioPlayerManager()
    val aiService = AiService()

    // Screen navigation
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    val selectedCourseId: StateFlow<Long?> = _selectedCourseId.asStateFlow()

    private val _selectedPageId = MutableStateFlow<Long?>(null)
    val selectedPageId: StateFlow<Long?> = _selectedPageId.asStateFlow()

    private val _isCreatingPage = MutableStateFlow(false)
    val isCreatingPage: StateFlow<Boolean> = _isCreatingPage.asStateFlow()

    private val _isGlobalSearching = MutableStateFlow(false)
    val isGlobalSearching: StateFlow<Boolean> = _isGlobalSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Flows from DB
    val studentProfile: StateFlow<StudentProfile?> = repository.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSemester: StateFlow<Semester?> = repository.activeSemester
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSemesters: StateFlow<List<Semester>> = repository.allSemesters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCourses: StateFlow<List<Course>> = activeSemester.flatMapLatest { sem ->
        if (sem != null) repository.getCoursesBySemester(sem.id)
        else repository.allActiveCourses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalActiveCoursesCount: StateFlow<Int> = repository.totalActiveCoursesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPagesCount: StateFlow<Int> = repository.totalPagesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRecordingsCount: StateFlow<Int> = repository.totalRecordingsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentPages: StateFlow<List<BinderPage>> = repository.recentPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordings: StateFlow<List<AudioRecording>> = repository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCourse: StateFlow<Course?> = _selectedCourseId.flatMapLatest { id ->
        if (id != null) repository.getCourseById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentCoursePages: StateFlow<List<BinderPage>> = _selectedCourseId.flatMapLatest { id ->
        if (id != null) repository.getPagesByCourse(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCourseRecordings: StateFlow<List<AudioRecording>> = _selectedCourseId.flatMapLatest { id ->
        if (id != null) repository.getRecordingsByCourse(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCourseSummaries: StateFlow<List<AiSummary>> = _selectedCourseId.flatMapLatest { id ->
        if (id != null) repository.getSummariesByCourse(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPage: StateFlow<BinderPage?> = _selectedPageId.flatMapLatest { id ->
        if (id != null) repository.getPageById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Search results
    val searchResults: StateFlow<List<SearchResultItem>> = _searchQuery.flatMapLatest { query ->
        if (query.isNotBlank()) repository.globalSearch(query) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI state
    private val _aiUiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiUiState: StateFlow<AiUiState> = _aiUiState.asStateFlow()

    // Navigation actions
    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
        _selectedCourseId.value = null
        _selectedPageId.value = null
        _isCreatingPage.value = false
        _isGlobalSearching.value = false
    }

    fun openCourseBinder(courseId: Long) {
        _selectedCourseId.value = courseId
        _selectedPageId.value = null
        _isCreatingPage.value = false
    }

    fun closeCourseBinder() {
        _selectedCourseId.value = null
        _selectedPageId.value = null
        _isCreatingPage.value = false
    }

    fun openPageEditor(pageId: Long?) {
        _selectedPageId.value = pageId
        _isCreatingPage.value = (pageId == null)
    }

    fun closePageEditor() {
        _selectedPageId.value = null
        _isCreatingPage.value = false
    }

    fun setGlobalSearching(enabled: Boolean) {
        _isGlobalSearching.value = enabled
        if (!enabled) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Profile Actions
    fun updateProfile(profile: StudentProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    // Semester Actions
    fun createNewSemester(semesterNumber: Int, academicYear: String, setAsActive: Boolean) {
        viewModelScope.launch {
            repository.createSemester(semesterNumber, academicYear, setAsActive)
        }
    }

    fun switchSemester(semesterId: Long) {
        viewModelScope.launch {
            repository.switchActiveSemester(semesterId)
        }
    }

    // Course Actions
    fun addCourse(
        name: String,
        code: String,
        lecturer: String,
        credits: Int,
        day: String,
        time: String,
        room: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val semId = activeSemester.value?.id ?: 1L
            repository.insertCourse(
                Course(
                    semesterId = semId,
                    name = name,
                    code = code,
                    lecturerName = lecturer,
                    credits = credits,
                    scheduleDay = day,
                    scheduleTime = time,
                    room = room,
                    coverColorHex = colorHex
                )
            )
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            if (_selectedCourseId.value == course.id) {
                closeCourseBinder()
            }
        }
    }

    // Page Actions
    fun savePage(
        pageId: Long?,
        courseId: Long,
        title: String,
        content: String,
        meetingNo: Int,
        paperType: PaperType,
        isImportant: Boolean,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val semId = activeSemester.value?.id ?: 1L
            val existing = if (pageId != null && pageId > 0) repository.getPageByIdSync(pageId) else null
            if (existing != null) {
                repository.updatePage(
                    existing.copy(
                        title = title,
                        content = content,
                        meetingNumber = meetingNo,
                        paperType = paperType.name,
                        isImportant = isImportant,
                        photoUri = photoUri ?: existing.photoUri,
                        lastModified = System.currentTimeMillis()
                    )
                )
            } else {
                val pagesCount = currentCoursePages.value.size
                repository.insertPage(
                    BinderPage(
                        courseId = courseId,
                        semesterId = semId,
                        title = title,
                        content = content,
                        meetingNumber = meetingNo,
                        pageNumber = pagesCount + 1,
                        paperType = paperType.name,
                        isImportant = isImportant,
                        photoUri = photoUri
                    )
                )
            }
            closePageEditor()
        }
    }

    fun deletePage(page: BinderPage) {
        viewModelScope.launch {
            repository.deletePage(page)
            if (_selectedPageId.value == page.id) {
                closePageEditor()
            }
        }
    }

    // Audio Actions
    fun saveRecording(
        courseId: Long,
        meetingNo: Int,
        title: String,
        file: File,
        durationSeconds: Int
    ) {
        viewModelScope.launch {
            repository.insertRecording(
                AudioRecording(
                    courseId = courseId,
                    meetingNumber = meetingNo,
                    title = title,
                    filePath = file.absolutePath,
                    durationSeconds = durationSeconds
                )
            )
        }
    }

    fun deleteRecording(recording: AudioRecording) {
        viewModelScope.launch {
            val file = File(recording.filePath)
            if (file.exists()) file.delete()
            repository.deleteRecording(recording)
        }
    }

    // AI Processing: Satu Rekaman -> Satu Catatan Lengkap
    fun processRecordingWithAi(
        recording: AudioRecording,
        course: Course
    ) {
        viewModelScope.launch {
            _aiUiState.value = AiUiState.Processing("Sedang mentranskripsikan audio & mengekstrak struktur materi...")
            val audioFile = File(recording.filePath)
            val result = aiService.processOneRecordingToFullPackage(
                audioFile = audioFile,
                courseName = course.name,
                lecturerName = course.lecturerName,
                meetingNo = recording.meetingNumber,
                topicHint = recording.title
            )

            when (result) {
                is AiResult.Success -> {
                    // Update recording with transcript
                    repository.updateRecording(
                        recording.copy(
                            transcriptionText = result.data.fullTranscript,
                            isProcessedByAi = true
                        )
                    )
                    _aiUiState.value = AiUiState.PackageResult(
                        pkg = result.data,
                        targetCourseId = course.id,
                        meetingNo = recording.meetingNumber
                    )
                }
                is AiResult.Error -> {
                    _aiUiState.value = AiUiState.Error(result.message)
                }
                is AiResult.NotConfigured -> {
                    _aiUiState.value = AiUiState.NotConfigured(result.message)
                }
            }
        }
    }

    // AI Resume dari Teks / Catatan
    fun generateSummaryFromNotes(
        notesText: String,
        courseName: String,
        courseId: Long,
        length: SummaryLength
    ) {
        viewModelScope.launch {
            _aiUiState.value = AiUiState.Processing("Merangkum materi perkuliahan dengan AI...")
            val result = aiService.generateStructuredSummary(
                sourceText = notesText,
                courseName = courseName,
                length = length
            )
            when (result) {
                is AiResult.Success -> {
                    _aiUiState.value = AiUiState.PackageResult(
                        pkg = result.data,
                        targetCourseId = courseId,
                        meetingNo = 1
                    )
                }
                is AiResult.Error -> {
                    _aiUiState.value = AiUiState.Error(result.message)
                }
                is AiResult.NotConfigured -> {
                    _aiUiState.value = AiUiState.NotConfigured(result.message)
                }
            }
        }
    }

    // Simpan Paket AI ke Binder
    fun saveAiPackageToBinder(pkg: ProcessedLecturePackage, courseId: Long, meetingNo: Int) {
        viewModelScope.launch {
            val semId = activeSemester.value?.id ?: 1L
            // 1. Simpan sebagai Halaman Baru di Binder
            val fullContent = buildString {
                appendLine("# ${pkg.title}")
                appendLine("*Pertemuan ke-$meetingNo • Catatan & Resume Berbasis AI*")
                appendLine()
                if (pkg.summaryText.isNotBlank()) {
                    appendLine("## Ringkasan Materi")
                    appendLine(pkg.summaryText)
                    appendLine()
                }
                if (pkg.structuredNotes.isNotBlank()) {
                    appendLine("## Catatan Kuliah Terstruktur")
                    appendLine(pkg.structuredNotes)
                    appendLine()
                }
                if (pkg.keyPoints.isNotBlank()) {
                    appendLine("## Poin-Poin Penting")
                    appendLine(pkg.keyPoints)
                    appendLine()
                }
                if (pkg.definitions.isNotBlank()) {
                    appendLine("## Istilah & Definisi Akademik")
                    appendLine(pkg.definitions)
                    appendLine()
                }
                if (pkg.lecturerExamples.isNotBlank()) {
                    appendLine("## Contoh yang Diberikan Dosen")
                    appendLine(pkg.lecturerExamples)
                    appendLine()
                }
                if (pkg.conclusion.isNotBlank()) {
                    appendLine("## Kesimpulan")
                    appendLine(pkg.conclusion)
                    appendLine()
                }
                if (pkg.studyRecommendations.isNotBlank()) {
                    appendLine("## Hal Penting untuk Dipelajari")
                    appendLine(pkg.studyRecommendations)
                }
            }

            repository.insertPage(
                BinderPage(
                    courseId = courseId,
                    semesterId = semId,
                    title = pkg.title,
                    content = fullContent,
                    meetingNumber = meetingNo,
                    pageNumber = currentCoursePages.value.size + 1,
                    paperType = PaperType.CORNELL.name,
                    isImportant = true
                )
            )

            // 2. Simpan juga ke AiSummary table
            repository.insertSummary(
                AiSummary(
                    courseId = courseId,
                    title = pkg.title,
                    sourceText = pkg.fullTranscript,
                    fullTranscript = pkg.fullTranscript,
                    structuredNotes = pkg.structuredNotes,
                    summaryText = pkg.summaryText,
                    definitions = pkg.definitions,
                    keyPoints = pkg.keyPoints,
                    lecturerExamples = pkg.lecturerExamples,
                    conclusion = pkg.conclusion,
                    studyRecommendations = pkg.studyRecommendations
                )
            )

            _aiUiState.value = AiUiState.Idle
            openCourseBinder(courseId)
        }
    }

    fun dismissAiState() {
        _aiUiState.value = AiUiState.Idle
    }

    fun askStudyAssistant(question: String, contextNotes: String) {
        viewModelScope.launch {
            _aiUiState.value = AiUiState.Processing("Asisten AI sedang menyusun jawaban...")
            val result = aiService.askStudyAssistant(question, contextNotes)
            when (result) {
                is AiResult.Success -> {
                    _aiUiState.value = AiUiState.AssistantResponse(result.data)
                }
                is AiResult.Error -> {
                    _aiUiState.value = AiUiState.Error(result.message)
                }
                is AiResult.NotConfigured -> {
                    _aiUiState.value = AiUiState.NotConfigured(result.message)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorderManager.release()
        playerManager.release()
    }
}
