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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ProcessedLecturePackage
import com.example.data.model.SummaryLength
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.BinderViewModel

@Composable
fun AiAssistantScreen(
    viewModel: BinderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val aiState by viewModel.aiUiState.collectAsState()
    val courses by viewModel.activeCourses.collectAsState()

    var activeResultTab by remember { mutableIntStateOf(0) }
    val resultTabs = listOf("Catatan Terstruktur", "Resume 8 Struktur", "Transkrip Lengkap", "Definisi & Poin", "Kesimpulan")

    var userQuestion by remember { mutableStateOf("") }
    var manualNotesInput by remember { mutableStateOf("") }
    var selectedSummaryLength by remember { mutableStateOf(SummaryLength.SEDANG) }
    var selectedCourseForNotes by remember(courses) { mutableStateOf(courses.firstOrNull()) }

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
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI Study Assistant",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BinderGoldRing
                    ) {
                        Text(
                            text = "Gemini",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Satu Rekaman → Satu Catatan Lengkap, resume 8 struktur materi, dan tanya jawab kuliah.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BinderTextSecondary
                )
            }
        }

        // 2. Active State Handling (Loading, Result, NotConfigured, Error)
        when (val state = aiState) {
            is AiUiState.Processing -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = BinderEspresso)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = BinderTextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sedang menganalisis struktur kuliah & poin akademik...",
                                style = MaterialTheme.typography.labelSmall,
                                color = BinderTextSecondary
                            )
                        }
                    }
                }
            }

            is AiUiState.PackageResult -> {
                // FEATURE 15: Satu Rekaman → Satu Catatan Lengkap Results Multi-tab
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Title & Success Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                                    Text(
                                        text = "✓ Satu Rekaman → Satu Catatan Lengkap",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PastelSageGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.dismissAiState() }) {
                                    Text("✕", color = BinderTextSecondary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.pkg.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = BinderEspresso
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sub-tabs
                            ScrollableTabRow(
                                selectedTabIndex = activeResultTab,
                                containerColor = BinderPaperSand,
                                contentColor = BinderEspresso,
                                edgePadding = 4.dp
                            ) {
                                resultTabs.forEachIndexed { idx, title ->
                                    Tab(
                                        selected = activeResultTab == idx,
                                        onClick = { activeResultTab = idx },
                                        text = {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (activeResultTab == idx) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tab Body Content
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = BinderPaperSand.copy(alpha = 0.5f)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                ) {
                                    item {
                                        when (activeResultTab) {
                                            0 -> {
                                                Text(
                                                    text = state.pkg.structuredNotes.ifBlank { "Catatan terstruktur belum tersedia." },
                                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                                    color = BinderTextPrimary
                                                )
                                            }
                                            1 -> {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = state.pkg.summaryText,
                                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                                        color = BinderTextPrimary
                                                    )
                                                    if (state.pkg.lecturerExamples.isNotBlank()) {
                                                        Text(
                                                            text = "Contoh dari Dosen:\n${state.pkg.lecturerExamples}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = BinderTextSecondary
                                                        )
                                                    }
                                                }
                                            }
                                            2 -> {
                                                Text(
                                                    text = state.pkg.fullTranscript.ifBlank { "Transkrip audio lengkap disimpan di arsip." },
                                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                                    color = BinderTextPrimary
                                                )
                                            }
                                            3 -> {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = "Definisi & Istilah Akademik:\n${state.pkg.definitions}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = BinderTextPrimary
                                                    )
                                                    Text(
                                                        text = "Poin Inti Pembahasan:\n${state.pkg.keyPoints}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = BinderTextSecondary
                                                    )
                                                }
                                            }
                                            4 -> {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = state.pkg.conclusion,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        color = BinderEspresso
                                                    )
                                                    if (state.pkg.studyRecommendations.isNotBlank()) {
                                                        Text(
                                                            text = "Materi yang Perlu Dipelajari:\n${state.pkg.studyRecommendations}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = BinderTextSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons (Simpan Semua ke Binder, Salin, Unduh)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveAiPackageToBinder(state.pkg, state.targetCourseId, state.meetingNo)
                                        Toast.makeText(context, "Semua hasil berhasil disimpan ke Binder!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_ai_pkg_to_binder")
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan ke Binder", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val copyText = "${state.pkg.title}\n\n${state.pkg.summaryText}\n\n${state.pkg.structuredNotes}"
                                        clipboardManager.setText(AnnotatedString(copyText))
                                        Toast.makeText(context, "Catatan lengkap disalin!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin")
                                }
                            }
                        }
                    }
                }
            }

            is AiUiState.AssistantResponse -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 Jawaban Tutor AI",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BinderEspresso
                                )
                                IconButton(onClick = { viewModel.dismissAiState() }) {
                                    Text("✕", color = BinderTextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.answer,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = BinderTextPrimary
                            )
                        }
                    }
                }
            }

            is AiUiState.NotConfigured -> {
                // Requirement 13: "Jika layanan AI belum terhubung, tampilkan status fitur belum tersedia, bukan membuat hasil AI palsu."
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = BinderGoldBrass)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status Layanan AI: Siap Dihubungkan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BinderEspresso
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = BinderTextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Petunjuk: Masukkan API Key Gemini Anda di Secrets Panel AI Studio (GEMINI_API_KEY). Tanpa API Key, aplikasi mematuhi ketentuan integritas akademik dengan tidak membuat data transkripsi palsu.",
                                style = MaterialTheme.typography.labelSmall,
                                color = BinderTextSecondary
                            )
                        }
                    }
                }
            }

            is AiUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTerracotta,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.dismissAiState() }) {
                                Text("✕", color = PastelTerracotta)
                            }
                        }
                    }
                }
            }

            AiUiState.Idle -> {
                // Normal view
            }
        }

        // 3. AI Resume Catatan Manual / Materi (Requirement 8)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rangkum Catatan Kuliah dengan AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                    Text(
                        text = "Tempel teks catatan atau materi dosen untuk menghasilkan 8 struktur resume.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = manualNotesInput,
                        onValueChange = { manualNotesInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("ai_manual_notes_input"),
                        placeholder = { Text("Tempel materi kuliah di sini...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinderEspresso,
                            unfocusedBorderColor = BinderPaperSand
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Panjang Resume Selector: Ringkas, Sedang, Lengkap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Kedalaman:", style = MaterialTheme.typography.labelSmall, color = BinderTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SummaryLength.values().forEach { len ->
                                val isSel = selectedSummaryLength == len
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) BinderWarmCaramel else BinderPaperSand)
                                        .clickable { selectedSummaryLength = len }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = len.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSel) Color.White else BinderTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (manualNotesInput.isBlank()) {
                                Toast.makeText(context, "Silakan masukkan materi catatan terlebih dahulu", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val targetCourse = selectedCourseForNotes ?: courses.firstOrNull()
                            val cName = targetCourse?.name ?: "Kuliah Umum"
                            val cId = targetCourse?.id ?: 1L
                            viewModel.generateSummaryFromNotes(manualNotesInput, cName, cId, selectedSummaryLength)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_summary_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat 8 Struktur Resume Sekarang")
                    }
                }
            }
        }

        // 4. Ask AI Study Tutor Chat (Matching 1793344204658_82a1021d.jpg)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tanya AI Seputar Kuliah",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                    Text(
                        text = "Konsultasikan pertanyaan ujian, istilah rumit, atau konsep materi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Chips (Matching user image)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val prompts = listOf(
                            "Jelaskan istilah sulit di materi ini",
                            "Buatkan 3 latihan soal ujian",
                            "Apa beda teori utama dalam pembahasan ini?"
                        )
                        items(prompts) { p ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BinderPaperSand,
                                modifier = Modifier.clickable { userQuestion = p }
                            ) {
                                Text(
                                    text = p,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BinderEspresso,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userQuestion,
                            onValueChange = { userQuestion = it },
                            placeholder = { Text("Tanyakan apa saja seputar materi...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ask_ai_input"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinderEspresso,
                                unfocusedBorderColor = BinderPaperSand
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = BinderEspresso,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    if (userQuestion.isNotBlank()) {
                                        viewModel.askStudyAssistant(userQuestion, manualNotesInput)
                                        userQuestion = ""
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
