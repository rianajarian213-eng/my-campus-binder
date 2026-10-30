package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Course
import com.example.ui.components.CourseBinderCoverCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BinderViewModel
import com.example.ui.viewmodel.ScreenTab

@Composable
fun DashboardScreen(
    viewModel: BinderViewModel,
    onOpenAddCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val activeSem by viewModel.activeSemester.collectAsState()
    val courses by viewModel.activeCourses.collectAsState()
    val totalCourses by viewModel.totalActiveCoursesCount.collectAsState()
    val totalPages by viewModel.totalPagesCount.collectAsState()
    val totalRecordings by viewModel.totalRecordingsCount.collectAsState()
    val recentPages by viewModel.recentPages.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Title & Campus Greeting Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MY CAMPUS BINDER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = BinderEspresso
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BinderGoldRing
                            ) {
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = "Halo, ${profile?.name ?: "Mahasiswa"}! 📖",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = BinderTextPrimary
                        )
                    }

                    // Search Button
                    Surface(
                        shape = CircleShape,
                        color = BinderPaperSand,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("dashboard_search_button")
                            .clickable { viewModel.setGlobalSearching(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Catatan",
                            tint = BinderEspresso,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile?.university ?: "UIN Sultan Syarif Kasim Riau"} • ${profile?.studyProgram ?: "Ilmu Komunikasi"} • Semester ${activeSem?.semesterNumber ?: 3}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BinderTextSecondary
                )
            }
        }

        // 2. Study Desk Aesthetic Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BinderPaperCard)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.study_desk_banner),
                        contentDescription = "Meja Belajar Digital",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle gradient overlay with quick quote
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x38000000))
                            .padding(14.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xCCFDFBF7)
                        ) {
                            Text(
                                text = "Meja Belajar Digital: Catat, Rekam & Rangkum Otomatis",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = BinderEspresso,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Stats Grid (Mata Kuliah, Catatan, Rekaman, SKS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Mata Kuliah",
                    value = "$totalCourses MK",
                    icon = Icons.Default.Book,
                    color = BinderWarmCaramel,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Catatan",
                    value = "$totalPages Lembar",
                    icon = Icons.Default.Edit,
                    color = PastelSageGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rekaman",
                    value = "$totalRecordings Audio",
                    icon = Icons.Default.Mic,
                    color = PastelTerracotta,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "+ Mata Kuliah",
                    icon = Icons.Default.Add,
                    onClick = onOpenAddCourse,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_course_button"
                )
                QuickActionChip(
                    label = "Mulai Rekam",
                    icon = Icons.Default.Mic,
                    onClick = { viewModel.setTab(ScreenTab.RECORD) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_record_button"
                )
                QuickActionChip(
                    label = "AI Resume",
                    icon = Icons.Default.AutoAwesome,
                    onClick = { viewModel.setTab(ScreenTab.AI_ASSISTANT) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_ai_button"
                )
            }
        }

        // 5. Today's Lecture Schedule (Jadwal Kuliah)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Perkuliahan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                    Text(
                        text = "Semester ${activeSem?.semesterNumber ?: 3}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinderTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (courses.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = BinderPaperSand
                    ) {
                        Text(
                            text = "Belum ada mata kuliah di semester ini. Tekan tombol + Tambah MK di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BinderTextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    courses.take(3).forEach { course ->
                        ScheduleItemCard(course = course, onClick = { viewModel.openCourseBinder(course.id) })
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 6. Recent Binder Notes Activity (Aktivitas Catatan Terbaru)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktivitas Catatan Terakhir",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BinderWarmCaramel,
                        modifier = Modifier.clickable { viewModel.setTab(ScreenTab.BINDERS) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentPages.isEmpty()) {
                    Text(
                        text = "Belum ada catatan yang ditulis. Buka salah satu binder untuk mulai mencatat materi dosen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BinderTextSecondary
                    )
                } else {
                    recentPages.forEach { page ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openCourseBinder(page.courseId)
                                    viewModel.openPageEditor(page.id)
                                }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BinderPaperSand),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P${page.meetingNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BinderEspresso
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = page.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = BinderTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = page.content.take(80).replace("\n", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BinderTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "❯",
                                    color = BinderWarmCaramel,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = BinderTextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = BinderTextSecondary
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = BinderPaperSand,
        modifier = modifier
            .height(44.dp)
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BinderEspresso,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = BinderEspresso,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ScheduleItemCard(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BinderPaperWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(course.coverColorHex))
                            } catch (_: Exception) {
                                BinderEspresso
                            }
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BinderTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${course.lecturerName.ifBlank { "Dosen" }} • ${course.room}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinderTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = BinderPaperSand
            ) {
                Text(
                    text = "${course.scheduleDay}, ${course.scheduleTime}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = BinderEspresso,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
