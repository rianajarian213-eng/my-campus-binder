package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioRecording
import com.example.data.model.BinderPage
import com.example.data.model.Course
import com.example.data.model.PaperType
import com.example.ui.components.AestheticBinderPaperContainer
import com.example.ui.components.AudioPlaybackBar
import com.example.ui.components.formatDuration
import androidx.compose.material.icons.filled.Pause
import com.example.ui.theme.*
import com.example.ui.viewmodel.BinderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CourseBinderDetailScreen(
    course: Course,
    viewModel: BinderViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val pages by viewModel.currentCoursePages.collectAsState()
    val recordings by viewModel.currentCourseRecordings.collectAsState()
    val summaries by viewModel.currentCourseSummaries.collectAsState()

    val isAudioPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val audioPosition by viewModel.playerManager.currentPosition.collectAsState()
    val audioDuration by viewModel.playerManager.duration.collectAsState()
    val audioSpeed by viewModel.playerManager.playbackSpeed.collectAsState()
    val currentAudioPath by viewModel.playerManager.currentPlayingPath.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📖 Catatan", "🎙️ Rekaman Dosen", "💡 AI Resume", "ℹ️ Info MK")

    // Current page index for flipping
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val activePage: BinderPage? = if (pages.isNotEmpty() && currentPageIndex < pages.size) pages[currentPageIndex] else null

    var currentPaperType by remember(activePage) {
        mutableStateOf(
            try { PaperType.valueOf(activePage?.paperType ?: PaperType.LINED.name) }
            catch (_: Exception) { PaperType.LINED }
        )
    }

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
    ) {
        // 1. Top Binder Header (Spine & Course Title)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = try {
                Color(android.graphics.Color.parseColor(course.coverColorHex))
            } catch (_: Exception) {
                BinderEspresso
            },
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Daftar Binder",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${course.lecturerName.ifBlank { "Dosen Pengampu" }} • ${course.credits} SKS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opsi Binder",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Hapus Binder Mata Kuliah") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteCourse(course)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = PastelTerracotta)
                                }
                            )
                        }
                    }
                }

                // Sub-tabs (Catatan, Rekaman, AI Resume, Info)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == index) BinderGoldRing else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        )
                    }
                }
            }
        }

        // 2. Tab Contents
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: BINDER NOTEBOOK PAGES (The Core Physical Binder Screen)
                    if (pages.isEmpty()) {
                        // Empty notebook state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📖", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Buku Catatan Masih Kosong",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mulai tulis catatan kuliah pertemuan pertama di binder fisik ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinderTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.openPageEditor(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tulis Catatan Halaman 1")
                            }
                        }
                    } else if (activePage != null) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Page Controls Bar (Page indicator, Paper selector, Actions)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Page Flipper (Hal X dari Y)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (currentPageIndex > 0) currentPageIndex--
                                        },
                                        enabled = currentPageIndex > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NavigateBefore,
                                            contentDescription = "Halaman Sebelumnya",
                                            tint = if (currentPageIndex > 0) BinderEspresso else Color.LightGray
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BinderPaperSand
                                    ) {
                                        Text(
                                            text = "Hal ${currentPageIndex + 1} / ${pages.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BinderEspresso,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (currentPageIndex < pages.size - 1) currentPageIndex++
                                        },
                                        enabled = currentPageIndex < pages.size - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NavigateNext,
                                            contentDescription = "Halaman Berikutnya",
                                            tint = if (currentPageIndex < pages.size - 1) BinderEspresso else Color.LightGray
                                        )
                                    }
                                }

                                // Paper Type Switcher Chips
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PaperType.values().forEach { type ->
                                        val isSel = currentPaperType == type
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) BinderWarmCaramel else BinderPaperSand)
                                                .clickable { currentPaperType = type }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = type.displayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSel) Color.White else BinderTextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // The Binder Paper Container with Gold Rings and Ruled/Grid Lines
                            AestheticBinderPaperContainer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                paperType = currentPaperType,
                                showRings = true
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Header: Meeting number, Date, Bookmark & Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = BinderPaperSand
                                                ) {
                                                    Text(
                                                        text = "Pertemuan ke-${activePage.meetingNumber}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = BinderEspresso,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(activePage.dateCreated))
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BinderTextSecondary
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Copy
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("${activePage.title}\n\n${activePage.content}"))
                                                    Toast.makeText(context, "Catatan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Salin Catatan",
                                                    tint = BinderEspresso,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            // Share / Export Outside App
                                            IconButton(
                                                onClick = {
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TITLE, activePage.title)
                                                        putExtra(Intent.EXTRA_SUBJECT, "MY CAMPUS BINDER - ${course.name} - ${activePage.title}")
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            "MY CAMPUS BINDER\nMata Kuliah: ${course.name} (${course.credits} SKS)\nDosen: ${course.lecturerName}\nPertemuan Ke-${activePage.meetingNumber}\n\nJudul: ${activePage.title}\n\n${activePage.content}"
                                                        )
                                                        type = "text/plain"
                                                    }
                                                    val shareIntent = Intent.createChooser(sendIntent, "Ekspor / Bagikan Catatan")
                                                    context.startActivity(shareIntent)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = "Ekspor Catatan ke Luar Aplikasi",
                                                    tint = BinderEspresso,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            // Edit
                                            IconButton(
                                                onClick = { viewModel.openPageEditor(activePage.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Catatan",
                                                    tint = BinderEspresso,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            // Delete
                                            IconButton(
                                                onClick = { viewModel.deletePage(activePage) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus Halaman",
                                                    tint = PastelTerracotta,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Title
                                    Text(
                                        text = activePage.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        ),
                                        color = BinderTextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Content Scrollable
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Text(
                                                text = activePage.content,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    lineHeight = 26.sp,
                                                    fontFamily = FontFamily.SansSerif
                                                ),
                                                color = BinderTextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom bar: + Tambah Halaman baru
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("${course.name}\n${activePage.title}\n\n${activePage.content}"))
                                        Toast.makeText(context, "Dokumen catatan berhasil disalin untuk dicetak/ekspor", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ekspor / Cetak")
                                }

                                Button(
                                    onClick = { viewModel.openPageEditor(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Halaman")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: REKAMAN SUARA DOSEN
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rekaman Kuliah ${course.name}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderTextPrimary
                            )
                            Button(
                                onClick = { viewModel.setTab(com.example.ui.viewmodel.ScreenTab.RECORD) },
                                colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rekam Sekarang")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (recordings.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = BinderPaperWhite
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎙️", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum Ada Rekaman Kuliah",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BinderTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tekan 'Rekam Sekarang' saat dosen menjelaskan materi di kelas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BinderTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recordings, key = { it.id }) { rec ->
                                    val isThisPlaying = isAudioPlaying && currentAudioPath == rec.filePath
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (isThisPlaying) PastelTerracotta else BinderPaperSand,
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clickable {
                                                                if (isThisPlaying) viewModel.playerManager.pause()
                                                                else viewModel.playerManager.play(rec.filePath)
                                                            }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = if (isThisPlaying) Color.White else BinderEspresso,
                                                            modifier = Modifier.padding(8.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = rec.title,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = BinderTextPrimary
                                                        )
                                                        Text(
                                                            text = "Pertemuan ${rec.meetingNumber} • Durasi: ${formatDuration(rec.durationSeconds)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = BinderTextSecondary
                                                        )
                                                    }
                                                }

                                                IconButton(onClick = { viewModel.deleteRecording(rec) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelTerracotta)
                                                }
                                            }

                                            // Transcription Status & Action Button
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (rec.transcriptionText != null) {
                                                    Text(
                                                        text = "✅ Sudah Ditranskripsikan AI",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PastelSageGreen
                                                    )
                                                } else {
                                                    Text(
                                                        text = "⏳ Belum ditranskripsi",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = BinderTextSecondary
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.processRecordingWithAi(rec, course)
                                                        viewModel.setTab(com.example.ui.viewmodel.ScreenTab.AI_ASSISTANT)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BinderWarmCaramel),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Proses AI", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Floating audio player bar if active
                            if (currentAudioPath != null) {
                                AudioPlaybackBar(
                                    isPlaying = isAudioPlaying,
                                    currentSeconds = audioPosition,
                                    durationSeconds = audioDuration,
                                    playbackSpeed = audioSpeed,
                                    onPlayPause = {
                                        if (isAudioPlaying) viewModel.playerManager.pause()
                                        else currentAudioPath?.let { viewModel.playerManager.play(it) }
                                    },
                                    onSeek = { viewModel.playerManager.seekTo(it) },
                                    onSpeedChange = { viewModel.playerManager.setSpeed(it) },
                                    onRewind = { viewModel.playerManager.seekBackward(10) },
                                    onForward = { viewModel.playerManager.seekForward(10) }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: AI RESUME & CATATAN OTOMATIS
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Rangkuman & Catatan AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BinderTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (summaries.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = BinderPaperWhite
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("💡", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum Ada Resume AI",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BinderTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Proses rekaman suara dosen atau buat rangkuman dari catatan materi Anda di tab AI Assistant.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BinderTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.setTab(com.example.ui.viewmodel.ScreenTab.AI_ASSISTANT) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Buka AI Assistant")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(summaries, key = { it.id }) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = BinderEspresso
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = BinderPaperSand
                                                ) {
                                                    Text(
                                                        text = "8 Struktur AI",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = BinderEspresso,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.summaryText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BinderTextPrimary
                                            )

                                            if (item.definitions.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Definisi & Istilah:",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = BinderTextPrimary
                                                )
                                                Text(
                                                    text = item.definitions,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = BinderTextSecondary
                                                )
                                            }

                                            if (item.conclusion.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Kesimpulan: ${item.conclusion}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = BinderEspresso
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 4: INFORMASI MATA KULIAH
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Informasi Akademik",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderEspresso
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            InfoRow(label = "Kode Mata Kuliah", value = course.code.ifBlank { "-" })
                            InfoRow(label = "Dosen Pengampu", value = course.lecturerName.ifBlank { "-" })
                            InfoRow(label = "Bobot SKS", value = "${course.credits} SKS")
                            InfoRow(label = "Jadwal Perkuliahan", value = "${course.scheduleDay}, ${course.scheduleTime}")
                            InfoRow(label = "Ruang Kelas", value = course.room)
                            InfoRow(label = "Total Halaman Catatan", value = "${pages.size} Halaman")
                            InfoRow(label = "Total Rekaman Suara", value = "${recordings.size} Rekaman")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = BinderTextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = BinderTextPrimary)
    }
}
