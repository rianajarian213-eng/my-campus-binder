package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BinderPage
import com.example.data.model.PaperType
import com.example.ui.components.AestheticBinderPaperContainer
import com.example.ui.theme.*
import com.example.ui.viewmodel.BinderViewModel

@Composable
fun NoteEditorScreen(
    courseId: Long,
    pageId: Long?,
    viewModel: BinderViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val currentPage by viewModel.currentPage.collectAsState()

    var title by remember(currentPage) { mutableStateOf(currentPage?.title ?: "") }
    var content by remember(currentPage) { mutableStateOf(currentPage?.content ?: "") }
    var meetingNo by remember(currentPage) { mutableIntStateOf(currentPage?.meetingNumber ?: 1) }
    var isImportant by remember(currentPage) { mutableStateOf(currentPage?.isImportant ?: false) }
    var paperType by remember(currentPage) {
        mutableStateOf(
            try { PaperType.valueOf(currentPage?.paperType ?: PaperType.LINED.name) }
            catch (_: Exception) { PaperType.LINED }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BinderCreamBg)
    ) {
        // Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BinderPaperWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = BinderEspresso
                        )
                    }
                    Text(
                        text = if (pageId == null) "Tulis Catatan Baru" else "Edit Catatan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BinderTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark / Penanda Penting
                    IconButton(onClick = { isImportant = !isImportant }) {
                        Icon(
                            imageVector = if (isImportant) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Penanda Penting",
                            tint = if (isImportant) BinderGoldRing else BinderTextSecondary
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Silakan isi judul materi terlebih dahulu", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.savePage(
                                pageId = pageId,
                                courseId = courseId,
                                title = title,
                                content = content,
                                meetingNo = meetingNo,
                                paperType = paperType,
                                isImportant = isImportant
                            )
                            Toast.makeText(context, "Catatan tersimpan di binder!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan")
                    }
                }
            }
        }

        // Sub-controls: Meeting number & Paper type selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meeting dropdown/stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pertemuan:",
                    style = MaterialTheme.typography.labelMedium,
                    color = BinderTextSecondary
                )
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
                        Text(
                            text = "▲",
                            modifier = Modifier.clickable { if (meetingNo < 16) meetingNo++ },
                            fontSize = 10.sp,
                            color = BinderEspresso
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            modifier = Modifier.clickable { if (meetingNo > 1) meetingNo-- },
                            fontSize = 10.sp,
                            color = BinderEspresso
                        )
                    }
                }
            }

            // Paper Types
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaperType.values().forEach { pt ->
                    val isSel = paperType == pt
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) BinderWarmCaramel else BinderPaperSand)
                            .clickable { paperType = pt }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = pt.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSel) Color.White else BinderTextPrimary
                        )
                    }
                }
            }
        }

        // Text Formatting Quick Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BinderPaperSand.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { content += " **Teks Tebal** " },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Tebal", tint = BinderEspresso)
                }
                IconButton(
                    onClick = { content += " *Teks Miring* " },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Miring", tint = BinderEspresso)
                }
                IconButton(
                    onClick = { content += "\n• " },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Poin-poin", tint = BinderEspresso)
                }
                IconButton(
                    onClick = { content += "\n1. " },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.FormatListNumbered, contentDescription = "Nomor", tint = BinderEspresso)
                }
                IconButton(
                    onClick = { content += " [PENTING: ] " },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Highlight, contentDescription = "Penanda Penting", tint = BinderGoldBrass)
                }
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Foto materi kuliah dapat disematkan di lembar binder", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Foto Materi", tint = BinderEspresso)
                }
            }
        }

        // Notebook Page Canvas Container with Ruled Paper & Margins
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
        ) {
            AestheticBinderPaperContainer(
                modifier = Modifier.fillMaxSize(),
                paperType = paperType,
                showRings = true
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title Input Field
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = BinderTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "Judul Materi Kuliah...",
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = BinderTextSecondary.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content Editor Field
                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = BinderTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .testTag("note_content_input"),
                        decorationBox = { innerTextField ->
                            if (content.isEmpty()) {
                                Text(
                                    text = "Tulis penjelasan dosen, rumus, poin materi, atau rangkuman di lembar binder ini...",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        lineHeight = 26.sp,
                                        color = BinderTextSecondary.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}
