package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.ui.components.CourseBinderCoverCard
import com.example.ui.theme.BinderCreamBg
import com.example.ui.theme.BinderEspresso
import com.example.ui.theme.BinderGoldRing
import com.example.ui.theme.BinderPaperCard
import com.example.ui.theme.BinderPaperSand
import com.example.ui.theme.BinderPaperWhite
import com.example.ui.theme.BinderTextPrimary
import com.example.ui.theme.BinderTextSecondary
import com.example.ui.viewmodel.BinderViewModel

@Composable
fun MyBindersScreen(
    viewModel: BinderViewModel,
    onOpenAddCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSem by viewModel.activeSemester.collectAsState()
    val allSemesters by viewModel.allSemesters.collectAsState()
    val courses by viewModel.activeCourses.collectAsState()

    var showSemesterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Semester selector badge
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Binder Mata Kuliah",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = BinderTextPrimary
                            )
                            Text(
                                text = "Koleksi buku catatan digital per mata kuliah",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinderTextSecondary
                            )
                        }

                        // Semester Switcher Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BinderPaperSand,
                            modifier = Modifier
                                .testTag("semester_switcher_button")
                                .clickable { showSemesterDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = BinderEspresso,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sem ${activeSem?.semesterNumber ?: 3}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BinderEspresso
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tahun Akademik: ${activeSem?.academicYear ?: "2024/2025"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinderTextSecondary
                    )
                }
            }

            if (courses.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = BinderPaperWhite
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📚",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Binder di Semester Ini",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BinderTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tambahkan mata kuliah baru untuk membuat binder catatan fisik digital Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BinderTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenAddCourse,
                                colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Mata Kuliah")
                            }
                        }
                    }
                }
            } else {
                items(courses, key = { it.id }) { course ->
                    CourseBinderCoverCard(
                        course = course,
                        notesCount = 4, // Reactive preview
                        recordingsCount = 2,
                        onClick = { viewModel.openCourseBinder(course.id) }
                    )
                }
            }
        }

        // Floating Action Button to Add Course
        FloatingActionButton(
            onClick = onOpenAddCourse,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("fab_add_course"),
            containerColor = BinderEspresso,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Mata Kuliah")
        }
    }

    // Semester Switcher Modal
    if (showSemesterDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSemesterDialog = false },
            title = {
                Text(
                    text = "Pilih Semester Aktif",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = BinderTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Catatan semester lama otomatis disimpan sebagai Arsip Seumur Kuliah dan tidak akan hilang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    allSemesters.forEach { sem ->
                        val isCurrent = sem.id == activeSem?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchSemester(sem.id)
                                    showSemesterDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) BinderPaperSand else BinderPaperWhite
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Semester ${sem.semesterNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BinderTextPrimary
                                    )
                                    Text(
                                        text = "Tahun: ${sem.academicYear}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BinderTextSecondary
                                    )
                                }
                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BinderEspresso
                                    ) {
                                        Text(
                                            text = "Aktif",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { showSemesterDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tutup", color = BinderEspresso)
                }
            },
            containerColor = BinderPaperCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
