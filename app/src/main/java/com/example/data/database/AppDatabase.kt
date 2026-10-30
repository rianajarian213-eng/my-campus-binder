package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AiSummaryDao
import com.example.data.dao.AudioRecordingDao
import com.example.data.dao.BinderPageDao
import com.example.data.dao.CourseDao
import com.example.data.dao.SemesterDao
import com.example.data.dao.StudentProfileDao
import com.example.data.model.AiSummary
import com.example.data.model.AudioRecording
import com.example.data.model.BinderPage
import com.example.data.model.Course
import com.example.data.model.PaperType
import com.example.data.model.Semester
import com.example.data.model.StudentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentProfile::class,
        Semester::class,
        Course::class,
        BinderPage::class,
        AudioRecording::class,
        AiSummary::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun semesterDao(): SemesterDao
    abstract fun courseDao(): CourseDao
    abstract fun binderPageDao(): BinderPageDao
    abstract fun audioRecordingDao(): AudioRecordingDao
    abstract fun aiSummaryDao(): AiSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_binder_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial data asynchronously on IO thread
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val profileDao = database.studentProfileDao()
            val semesterDao = database.semesterDao()
            val courseDao = database.courseDao()
            val pageDao = database.binderPageDao()

            // 1. Initial Profile
            val profile = StudentProfile(
                name = "Ahmad Fadhil",
                nim = "12540311761",
                university = "UIN Sultan Syarif Kasim Riau",
                faculty = "Fakultas Dakwah dan Komunikasi",
                studyProgram = "Ilmu Komunikasi",
                activeSemester = 3,
                academicYear = "2024/2025",
                className = "IKOM-3A",
                status = "Mahasiswa Aktif"
            )
            profileDao.insertOrUpdateProfile(profile)

            // 2. Semesters
            val sem1 = semesterDao.insertSemester(
                Semester(semesterNumber = 1, academicYear = "2023/2024", isActive = false, isArchived = true)
            )
            val sem2 = semesterDao.insertSemester(
                Semester(semesterNumber = 2, academicYear = "2023/2024", isActive = false, isArchived = true)
            )
            val sem3 = semesterDao.insertSemester(
                Semester(semesterNumber = 3, academicYear = "2024/2025", isActive = true, isArchived = false)
            )

            // 3. Courses for Semester 3
            val c1 = courseDao.insertCourse(
                Course(
                    semesterId = sem3,
                    name = "Komunikasi Bisnis",
                    code = "KOM301",
                    lecturerName = "Dr. H. Rusdianto, M.Si",
                    credits = 3,
                    scheduleDay = "Senin",
                    scheduleTime = "08:00 - 10:30",
                    room = "Gedung FDK Lt. 3 R-302",
                    coverColorHex = "#4A3525", // Espresso Leather
                    orderIndex = 0
                )
            )

            val c2 = courseDao.insertCourse(
                Course(
                    semesterId = sem3,
                    name = "Teori Komunikasi",
                    code = "KOM302",
                    lecturerName = "Prof. Nurul Huda, Ph.D",
                    credits = 3,
                    scheduleDay = "Selasa",
                    scheduleTime = "10:45 - 13:15",
                    room = "Auditorium Prof. Harun",
                    coverColorHex = "#6E8B74", // Sage Green
                    orderIndex = 1
                )
            )

            val c3 = courseDao.insertCourse(
                Course(
                    semesterId = sem3,
                    name = "Studi Al-Qur'an",
                    code = "UIN104",
                    lecturerName = "Dr. M. Ihsan, M.Ag",
                    credits = 2,
                    scheduleDay = "Rabu",
                    scheduleTime = "13:30 - 15:10",
                    room = "Lab Bahasa & Studi Islam",
                    coverColorHex = "#D4AF37", // Warm Gold
                    orderIndex = 2
                )
            )

            val c4 = courseDao.insertCourse(
                Course(
                    semesterId = sem3,
                    name = "Fotografi Komunikasi",
                    code = "KOM305",
                    lecturerName = "Rian Pratama, S.Sn., M.Ds",
                    credits = 3,
                    scheduleDay = "Kamis",
                    scheduleTime = "08:00 - 11:20",
                    room = "Studio Foto FDK",
                    coverColorHex = "#C0705E", // Terracotta
                    orderIndex = 3
                )
            )

            val c5 = courseDao.insertCourse(
                Course(
                    semesterId = sem3,
                    name = "Pengantar Ilmu Komunikasi",
                    code = "KOM101",
                    lecturerName = "Dra. Hj. Fitriani, M.Si",
                    credits = 3,
                    scheduleDay = "Jumat",
                    scheduleTime = "08:00 - 10:30",
                    room = "Gedung B R-101",
                    coverColorHex = "#4A6572", // Slate Navy
                    orderIndex = 4
                )
            )

            // 4. Sample Binder Pages
            pageDao.insertPage(
                BinderPage(
                    courseId = c1,
                    semesterId = sem3,
                    title = "Pengantar Negosiasi & Komunikasi Antar Budaya",
                    meetingNumber = 3,
                    pageNumber = 1,
                    paperType = PaperType.LINED.name,
                    isImportant = true,
                    content = """
                    # Pertemuan 3: Negosiasi Bisnis Modern
                    
                    Dosen menekankan pentingnya persiapan pra-negosiasi (Pre-negotiation planning).
                    
                    **Poin Utama:**
                    • BATNA (Best Alternative to a Negotiated Agreement): Alternatif terbaik jika negosiasi mencapai jalan buntu.
                    • ZOPA (Zone of Possible Agreement): Rentang di mana kesepakatan saling menguntungkan dapat dicapai.
                    • High-Context vs Low-Context Communication dalam bisnis internasional.
                    
                    *Catatan Dosen:*
                    "Jangan pernah masuk ke ruang perundingan tanpa mengetahui nilai toleransi minimal tim Anda."
                    
                    Tugas Kelompok: Analisis studi kasus merger perusahaan teknologi Indonesia.
                    """.trimIndent()
                )
            )

            pageDao.insertPage(
                BinderPage(
                    courseId = c2,
                    semesterId = sem3,
                    title = "Model Komunikasi Shannon-Weaver & Agenda Setting",
                    meetingNumber = 2,
                    pageNumber = 1,
                    paperType = PaperType.CORNELL.name,
                    isImportant = false,
                    content = """
                    # Model Transmisi & Teori Efek Media
                    
                    Teori Agenda Setting (McCombs & Shaw, 1972):
                    Media massa tidak memberi tahu masyarakat *apa yang harus dipikirkan*, melainkan *apa yang harus dipikirkan tentang*.
                    
                    **Tiga Tingkatan Agenda:**
                    1. Agenda Media (prioritas isu yang disiarkan redaksi)
                    2. Agenda Publik (isu yang dipandang urgen oleh masyarakat)
                    3. Agenda Kebijakan (agenda tindakan legislator/pemerintah)
                    """.trimIndent()
                )
            )
        }
    }
}
