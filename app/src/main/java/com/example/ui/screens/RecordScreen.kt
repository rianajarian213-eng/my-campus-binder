package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AudioRecording
import com.example.data.model.Course
import com.example.ui.components.AudioPlaybackBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.formatDuration
import com.example.ui.theme.*
import com.example.ui.viewmodel.BinderViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: BinderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val courses by viewModel.activeCourses.collectAsState()
    val allRecordings by viewModel.allRecordings.collectAsState()

    val isRecording by viewModel.recorderManager.isRecording.collectAsState()
    val isPaused by viewModel.recorderManager.isPaused.collectAsState()
    val elapsedSeconds by viewModel.recorderManager.elapsedSeconds.collectAsState()
    val waveAmplitudes by viewModel.recorderManager.waveAmplitudes.collectAsState()

    val isAudioPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val audioPosition by viewModel.playerManager.currentPosition.collectAsState()
    val audioDuration by viewModel.playerManager.duration.collectAsState()
    val audioSpeed by viewModel.playerManager.playbackSpeed.collectAsState()
    val currentAudioPath by viewModel.playerManager.currentPlayingPath.collectAsState()

    var selectedCourse by remember(courses) { mutableStateOf(courses.firstOrNull()) }
    var meetingNo by remember { mutableIntStateOf(1) }
    var recordingTitle by remember { mutableStateOf("Penjelasan Dosen") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Permission launcher for microphone
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (!granted) {
            Toast.makeText(context, "Izin mikrofon diperlukan untuk merekam perkuliahan", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Perekam Suara Dosen",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = BinderTextPrimary
                )
                Text(
                    text = "Rekam penjelasan kuliah langsung, pantau waveform, dan transkripsikan dengan AI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BinderTextSecondary
                )
            }
        }

        // 2. Active Recorder Console Card (Matching 1793344203089_0b3963e8.jpg aesthetic)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Course Selector dropdown
                    if (!isRecording) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCourse?.name ?: "Pilih Mata Kuliah...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mata Kuliah") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BinderEspresso,
                                    unfocusedBorderColor = BinderPaperSand
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                courses.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.name) },
                                        onClick = {
                                            selectedCourse = course
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Meeting Number Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Pertemuan:", style = MaterialTheme.typography.labelMedium, color = BinderTextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BinderPaperSand
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Ke-$meetingNo",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BinderEspresso
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("▲", modifier = Modifier.clickable { if (meetingNo < 16) meetingNo++ }, fontSize = 10.sp, color = BinderEspresso)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("▼", modifier = Modifier.clickable { if (meetingNo > 1) meetingNo-- }, fontSize = 10.sp, color = BinderEspresso)
                                    }
                                }
                            }

                            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date())
                            Text(text = "Tanggal: $dateStr", style = MaterialTheme.typography.labelSmall, color = BinderTextSecondary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        // While recording: display active course banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BinderPaperSand,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Merekam: ${selectedCourse?.name ?: "Perkuliahan"} (Pertemuan $meetingNo)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderEspresso,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Live Elapsed Timer Display (00:15)
                    Text(
                        text = formatDuration(elapsedSeconds),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isRecording) PastelTerracotta else BinderTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Waveform Visualizer
                    WaveformVisualizer(
                        amplitudes = waveAmplitudes,
                        isRecording = isRecording && !isPaused,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Recording Controls: Start, Pause, Resume, Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            // Big Start Recording Button
                            Surface(
                                shape = CircleShape,
                                color = PastelTerracotta,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("start_recording_button")
                                    .clickable {
                                        if (!hasMicPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            return@clickable
                                        }
                                        val targetCourse = selectedCourse ?: courses.firstOrNull()
                                        if (targetCourse == null) {
                                            Toast.makeText(context, "Silakan pilih mata kuliah terlebih dahulu", Toast.LENGTH_SHORT).show()
                                            return@clickable
                                        }
                                        viewModel.recorderManager.startRecording(targetCourse.id, meetingNo)
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mulai Rekam",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(18.dp)
                                        .fillMaxSize()
                                )
                            }
                        } else {
                            // Pause / Resume Button
                            Surface(
                                shape = CircleShape,
                                color = BinderPaperSand,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable {
                                        if (isPaused) viewModel.recorderManager.resumeRecording()
                                        else viewModel.recorderManager.pauseRecording()
                                    }
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Lanjutkan" else "Jeda",
                                    tint = BinderEspresso,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(28.dp))

                            // Stop & Save Recording Button
                            Surface(
                                shape = CircleShape,
                                color = PastelTerracotta,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("stop_recording_button")
                                    .clickable {
                                        val recordedFile = viewModel.recorderManager.stopRecording()
                                        val targetCourse = selectedCourse ?: courses.firstOrNull()
                                        if (recordedFile != null && targetCourse != null) {
                                            val title = "Kuliah ${targetCourse.name} - Pertemuan $meetingNo"
                                            viewModel.saveRecording(
                                                courseId = targetCourse.id,
                                                meetingNo = meetingNo,
                                                title = title,
                                                file = recordedFile,
                                                durationSeconds = elapsedSeconds
                                            )
                                            Toast.makeText(context, "Rekaman tersimpan di binder!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Berhenti & Simpan",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (!isRecording) "Ketuk tombol untuk mulai merekam penjelasan dosen"
                        else if (isPaused) "Rekaman dijeda"
                        else "Sedang merekam suara perkuliahan...",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinderTextSecondary
                    )
                }
            }
        }

        // 3. Saved Recordings List Header
        item {
            Text(
                text = "Daftar Rekaman Perkuliahan",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = BinderTextPrimary
            )
        }

        if (allRecordings.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = BinderPaperWhite
                ) {
                    Text(
                        text = "Belum ada arsip rekaman suara dosen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(allRecordings, key = { it.id }) { rec ->
                val course = courses.find { it.id == rec.courseId }
                val isThisPlaying = isAudioPlaying && currentAudioPath == rec.filePath

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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
                                        .size(40.dp)
                                        .clickable {
                                            if (isThisPlaying) viewModel.playerManager.pause()
                                            else viewModel.playerManager.play(rec.filePath)
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isThisPlaying) Color.White else BinderEspresso,
                                        modifier = Modifier.padding(10.dp)
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
                                        text = "${course?.name ?: "Mata Kuliah"} • Durasi: ${formatDuration(rec.durationSeconds)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BinderTextSecondary
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteRecording(rec) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelTerracotta)
                            }
                        }

                        // Bottom Actions: Status & One-Tap AI Processing
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (rec.transcriptionText != null) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE8F5E9)) {
                                    Text(
                                        text = "✔ Terarsip AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelSageGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(4.dp), color = BinderPaperSand) {
                                    Text(
                                        text = "Audio Tersimpan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BinderTextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (course != null) {
                                        viewModel.processRecordingWithAi(rec, course)
                                        viewModel.setTab(com.example.ui.viewmodel.ScreenTab.AI_ASSISTANT)
                                    } else {
                                        Toast.makeText(context, "Mata kuliah terkait tidak ditemukan", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("ai_process_recording_${rec.id}")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Proses dengan AI", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Audio Player Controller Bar (if playback active)
        if (currentAudioPath != null) {
            item {
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
