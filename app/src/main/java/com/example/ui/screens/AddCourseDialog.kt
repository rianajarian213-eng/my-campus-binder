package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BinderEspresso
import com.example.ui.theme.BinderPaperCard
import com.example.ui.theme.BinderPaperSand
import com.example.ui.theme.BinderTextPrimary
import com.example.ui.theme.BinderTextSecondary
import com.example.ui.viewmodel.BinderViewModel

data class BinderColorOption(val name: String, val hex: String)

val binderCoverColors = listOf(
    BinderColorOption("Espresso", "#4A3525"),
    BinderColorOption("Caramel", "#9E7154"),
    BinderColorOption("Sage Green", "#6E8B74"),
    BinderColorOption("Dusty Rose", "#BF8292"),
    BinderColorOption("Slate Navy", "#4A6572"),
    BinderColorOption("Honey Gold", "#D4AF37")
)

@Composable
fun AddCourseDialog(
    viewModel: BinderViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var credits by remember { mutableIntStateOf(3) }
    var day by remember { mutableStateOf("Senin") }
    var time by remember { mutableStateOf("08:00 - 10:30") }
    var room by remember { mutableStateOf("Ruang R-302") }
    var selectedColorHex by remember { mutableStateOf(binderCoverColors.first().hex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah Mata Kuliah & Sampul Binder",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = BinderTextPrimary
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Mata Kuliah *") },
                        placeholder = { Text("Contoh: Komunikasi Bisnis") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_course_name_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Kode MK") },
                            placeholder = { Text("KOM301") },
                            modifier = Modifier.weight(1f)
                        )

                        // SKS Stepper
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SKS:", style = MaterialTheme.typography.labelSmall, color = BinderTextSecondary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BinderPaperSand,
                                modifier = Modifier.height(54.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$credits SKS",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BinderEspresso
                                    )
                                    Row {
                                        Text(
                                            text = "▲",
                                            modifier = Modifier
                                                .clickable { if (credits < 6) credits++ }
                                                .padding(4.dp),
                                            fontSize = 12.sp,
                                            color = BinderEspresso
                                        )
                                        Text(
                                            text = "▼",
                                            modifier = Modifier
                                                .clickable { if (credits > 1) credits-- }
                                                .padding(4.dp),
                                            fontSize = 12.sp,
                                            color = BinderEspresso
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = lecturer,
                        onValueChange = { lecturer = it },
                        label = { Text("Nama Dosen Pengampu") },
                        placeholder = { Text("Contoh: Dr. H. Rusdianto, M.Si") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = { day = it },
                            label = { Text("Hari") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Jam Kuliah") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Ruang Kelas") },
                        placeholder = { Text("Gedung FDK R-302") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Pilih Warna Sampul Binder:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BinderTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            binderCoverColors.forEach { colorOpt ->
                                val isSelected = selectedColorHex == colorOpt.hex
                                val col = Color(android.graphics.Color.parseColor(colorOpt.hex))

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .clickable { selectedColorHex = colorOpt.hex }
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Silakan isi nama mata kuliah", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addCourse(
                        name = name,
                        code = code,
                        lecturer = lecturer,
                        credits = credits,
                        day = day,
                        time = time,
                        room = room,
                        colorHex = selectedColorHex
                    )
                    Toast.makeText(context, "Binder mata kuliah berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BinderEspresso),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_add_course_button")
            ) {
                Text("Buat Binder")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal")
            }
        },
        containerColor = BinderPaperCard,
        shape = RoundedCornerShape(16.dp)
    )
}
