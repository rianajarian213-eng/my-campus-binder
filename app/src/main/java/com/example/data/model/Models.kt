package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaperType(val displayName: String) {
    LINED("Bergaris"),
    BLANK("Polos"),
    GRID("Kotak-kotak"),
    CORNELL("Jurnal Cornell")
}

enum class EducationStatus(val label: String) {
    ACTIVE("Mahasiswa Aktif"),
    LEAVE("Cuti Akademik"),
    GRADUATED("Lulus"),
    POSTGRAD("Melanjutkan Pendidikan")
}

enum class SummaryLength(val label: String, val promptInstruction: String) {
    RINGKAS("Ringkas", "Buat ringkasan padat dan to-the-point maksimal 3-4 paragraf"),
    SEDANG("Sedang", "Buat resume seimbang dengan penjelasan cukup jelas per sub-topik"),
    LENGKAP("Lengkap", "Buat resume komprehensif, mencakup seluruh detail akademik, dalil/teori, dan elaborasi dosen")
}

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Mahasiswa Kampus",
    val nim: String = "12540311761",
    val university: String = "UIN Sultan Syarif Kasim Riau",
    val faculty: String = "Fakultas Dakwah dan Komunikasi",
    val studyProgram: String = "Ilmu Komunikasi",
    val activeSemester: Int = 3,
    val academicYear: String = "2024/2025",
    val className: String = "IKOM-A",
    val status: String = EducationStatus.ACTIVE.label,
    val photoUri: String? = null
)

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterNumber: Int,
    val academicYear: String,
    val isActive: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val name: String,
    val code: String = "",
    val lecturerName: String = "",
    val credits: Int = 3,
    val scheduleDay: String = "Senin",
    val scheduleTime: String = "08:00 - 09:40",
    val room: String = "Ruang R-302",
    val coverColorHex: String = "#4A3525", // Hex color
    val coverTexture: String = "LEATHER",
    val orderIndex: Int = 0,
    val isArchived: Boolean = false
)

@Entity(tableName = "binder_pages")
data class BinderPage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val semesterId: Long,
    val title: String,
    val content: String,
    val meetingNumber: Int = 1, // Pertemuan ke-X
    val pageNumber: Int = 1,
    val paperType: String = PaperType.LINED.name,
    val isImportant: Boolean = false,
    val photoUri: String? = null,
    val dateCreated: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "audio_recordings")
data class AudioRecording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val meetingNumber: Int = 1,
    val title: String,
    val filePath: String,
    val durationSeconds: Int = 0,
    val dateRecorded: Long = System.currentTimeMillis(),
    val transcriptionText: String? = null,
    val isProcessedByAi: Boolean = false
)

@Entity(tableName = "ai_summaries")
data class AiSummary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val recordingId: Long? = null,
    val pageId: Long? = null,
    val title: String,
    val sourceText: String,
    val fullTranscript: String = "",
    val structuredNotes: String = "",
    val summaryText: String = "",
    val definitions: String = "",
    val keyPoints: String = "",
    val lecturerExamples: String = "",
    val conclusion: String = "",
    val studyRecommendations: String = "",
    val summaryLength: String = SummaryLength.SEDANG.name,
    val dateCreated: Long = System.currentTimeMillis()
)
