package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EducationStatus
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.BinderViewModel

@Composable
fun ProfileScreen(
    viewModel: BinderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val profile by viewModel.studentProfile.collectAsState()
    val activeSem by viewModel.activeSemester.collectAsState()
    val allSemesters by viewModel.allSemesters.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showNewSemesterDialog by remember { mutableStateOf(false) }
    var showAcademicLibraryDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Text(
                text = "Identitas Mahasiswa",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = BinderTextPrimary
            )
        }

        // 2. Aesthetic KTM Digital (Kartu Tanda Mahasiswa)
        item {
            val prof = profile ?: StudentProfile()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderEspresso),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // KTM Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = BinderGoldRing, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = prof.university,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = prof.faculty,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profil", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar & Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BinderGoldRing),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prof.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BinderEspresso
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = prof.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "NIM: ${prof.nim}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = BinderGoldRing
                            )
                            Text(
                                text = "${prof.studyProgram} • Kelas ${prof.className}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status pill & Semester
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = prof.status,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Semester ${prof.activeSemester} (${prof.academicYear})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // 3. Menu PERPUSTAKAAN AKADEMIK (Lifetime Academic Library)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAcademicLibraryDialog = true }
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BinderPaperSand),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = BinderEspresso)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Perpustakaan Akademik Seumur Hidup",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderTextPrimary
                            )
                            Text(
                                text = "Akses seluruh arsip catatan dari semester 1 hingga setelah lulus.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinderTextSecondary
                            )
                        }
                    }
                    Text("❯", color = BinderWarmCaramel, fontSize = 16.sp)
                }
            }
        }

        // 4. Semester Management Card (Arsip Perkuliahan Seumur Kuliah)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Semester & Arsip",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BinderTextPrimary
                        )

                        Button(
                            onClick = { showNewSemesterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Semester Baru", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    allSemesters.forEach { sem ->
                        val isCurrent = sem.id == activeSem?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.switchSemester(sem.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) BinderPaperSand else BinderPaperCard
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = BinderWarmCaramel)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Semester ${sem.semesterNumber}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BinderTextPrimary
                                        )
                                        Text(
                                            text = "Tahun Akademik: ${sem.academicYear} ${if (sem.isArchived) "• Terarsip" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BinderTextSecondary
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = BinderEspresso) {
                                        Text(
                                            text = "Aktif",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Aktifkan",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = BinderWarmCaramel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Data Backup & Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Keamanan Data",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BinderTextPrimary
                    )
                    Text(
                        text = "Data binder tersimpan secara permanen dalam database lokal dan siap disinkronkan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val prof = profile ?: StudentProfile()
                            val backupText = """
                            MY CAMPUS BINDER - CADANGAN AKADEMIK
                            Mahasiswa: ${prof.name} (${prof.nim})
                            Kampus: ${prof.university} - ${prof.faculty}
                            Program Studi: ${prof.studyProgram}
                            Semester Aktif: ${prof.activeSemester} (${prof.academicYear})
                            Status: ${prof.status}
                            Cadangan data dibuat pada: ${java.util.Date()}
                            """.trimIndent()

                            clipboardManager.setText(AnnotatedString(backupText))
                            Toast.makeText(context, "Ringkasan cadangan data disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor / Backup Ringkasan Data")
                    }
                }
            }
        }
    }

    // Modal: Edit Profil Mahasiswa
    if (showEditProfileDialog) {
        val curr = profile ?: StudentProfile()
        var editName by remember { mutableStateOf(curr.name) }
        var editNim by remember { mutableStateOf(curr.nim) }
        var editUni by remember { mutableStateOf(curr.university) }
        var editFaculty by remember { mutableStateOf(curr.faculty) }
        var editStudyProg by remember { mutableStateOf(curr.studyProgram) }
        var editClass by remember { mutableStateOf(curr.className) }
        var editStatus by remember { mutableStateOf(curr.status) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profil Mahasiswa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editNim,
                            onValueChange = { editNim = it },
                            label = { Text("NIM") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editUni,
                            onValueChange = { editUni = it },
                            label = { Text("Universitas / Perguruan Tinggi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editFaculty,
                            onValueChange = { editFaculty = it },
                            label = { Text("Fakultas") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editStudyProg,
                            onValueChange = { editStudyProg = it },
                            label = { Text("Program Studi / Jurusan") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editClass,
                            onValueChange = { editClass = it },
                            label = { Text("Kelas") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            curr.copy(
                                name = editName,
                                nim = editNim,
                                university = editUni,
                                faculty = editFaculty,
                                studyProgram = editStudyProg,
                                className = editClass,
                                status = editStatus
                            )
                        )
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profil mahasiswa diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Tambah Semester Baru (Requirement 16)
    if (showNewSemesterDialog) {
        var semNo by remember { mutableIntStateOf((allSemesters.maxOfOrNull { it.semesterNumber } ?: 3) + 1) }
        var acadYear by remember { mutableStateOf("2025/2026") }

        AlertDialog(
            onDismissRequest = { showNewSemesterDialog = false },
            title = {
                Text(
                    text = "Buat Semester Baru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ketika semester baru dibuat, seluruh catatan semester lama otomatis menjadi arsip permanen dan tidak hilang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )
                    OutlinedTextField(
                        value = "Semester $semNo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nomor Semester") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = acadYear,
                        onValueChange = { acadYear = it },
                        label = { Text("Tahun Akademik (contoh: 2025/2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewSemester(semNo, acadYear, setAsActive = true)
                        showNewSemesterDialog = false
                        Toast.makeText(context, "Semester baru dibuat & diaktifkan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso)
                ) {
                    Text("Buat Semester")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewSemesterDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: PERPUSTAKAAN AKADEMIK (Requirement 18)
    if (showAcademicLibraryDialog) {
        val prof = profile ?: StudentProfile()
        AlertDialog(
            onDismissRequest = { showAcademicLibraryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = BinderEspresso)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perpustakaan Akademik Seumur Hidup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Surface(shape = RoundedCornerShape(8.dp), color = BinderPaperSand) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${prof.university} • ${prof.faculty}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BinderEspresso
                                )
                                Text(
                                    text = "Jurusan: ${prof.studyProgram} • Status: ${prof.status}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinderTextSecondary
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = "Riwayat Semester Terdaftar:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = BinderTextPrimary
                        )
                    }
                    allSemesters.forEach { sem ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BinderPaperCard,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Semester ${sem.semesterNumber} (${sem.academicYear})",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = BinderTextPrimary
                                        )
                                        Text(
                                            text = if (sem.isActive) "Sedang Aktif" else "Tersimpan Permanen di Arsip",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sem.isActive) PastelSageGreen else BinderTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "Buka ❯",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BinderWarmCaramel,
                                        modifier = Modifier.clickable {
                                            viewModel.switchSemester(sem.id)
                                            showAcademicLibraryDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAcademicLibraryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}
