package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.PaperType
import com.example.ui.theme.*

@Composable
fun AestheticBinderPaperContainer(
    modifier: Modifier = Modifier,
    paperType: PaperType = PaperType.LINED,
    showRings: Boolean = true,
    content: @Composable () -> Unit
) {
    // Outer leather border + gold ring spine + ruled/grid paper
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BinderEspresso)
            .padding(4.dp) // Outer leather edge
    ) {
        // Inner white/cream paper sheet
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(BinderPaperWhite)
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // 1. Margin line on the left (at 48.dp from left edge)
                    val marginX = 46.dp.toPx()
                    drawLine(
                        color = BinderMarginRed.copy(alpha = 0.55f),
                        start = Offset(marginX, 0f),
                        end = Offset(marginX, height),
                        strokeWidth = 1.8f
                    )

                    // 2. Ruled lines or Grid lines
                    when (paperType) {
                        PaperType.LINED, PaperType.CORNELL -> {
                            val lineSpacing = 28.dp.toPx()
                            var y = 48.dp.toPx()
                            while (y < height) {
                                drawLine(
                                    color = BinderRuledLine,
                                    start = Offset(if (paperType == PaperType.CORNELL) marginX else 0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                                y += lineSpacing
                            }

                            if (paperType == PaperType.CORNELL) {
                                // Bottom summary divider line
                                val summaryY = height - 120.dp.toPx()
                                drawLine(
                                    color = BinderGoldBrass.copy(alpha = 0.6f),
                                    start = Offset(0f, summaryY),
                                    end = Offset(width, summaryY),
                                    strokeWidth = 2f
                                )
                            }
                        }
                        PaperType.GRID -> {
                            val gridSpacing = 20.dp.toPx()
                            var x = 0f
                            while (x < width) {
                                drawLine(
                                    color = BinderRuledLine.copy(alpha = 0.6f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 0.8f
                                )
                                x += gridSpacing
                            }
                            var y = 0f
                            while (y < height) {
                                drawLine(
                                    color = BinderRuledLine.copy(alpha = 0.6f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 0.8f
                                )
                                y += gridSpacing
                            }
                        }
                        PaperType.BLANK -> {
                            // Clean smooth paper
                        }
                    }

                    // 3. Draw metallic Gold Ring Binder Gutter on the left edge if enabled
                    if (showRings) {
                        val ringX = 14.dp.toPx()
                        val ringCount = 6
                        val ringGap = height / (ringCount + 1)

                        for (i in 1..ringCount) {
                            val ringY = i * ringGap

                            // Hole punch shadow
                            drawCircle(
                                color = Color(0x33000000),
                                radius = 6.dp.toPx(),
                                center = Offset(ringX, ringY)
                            )
                            drawCircle(
                                color = BinderPaperSand,
                                radius = 5.dp.toPx(),
                                center = Offset(ringX, ringY)
                            )

                            // Metallic Gold Ring curved segment
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(BinderGoldRing, BinderGoldBrass, BinderGoldRing),
                                    start = Offset(ringX - 10.dp.toPx(), ringY - 8.dp.toPx()),
                                    end = Offset(ringX + 8.dp.toPx(), ringY + 8.dp.toPx())
                                ),
                                startAngle = -70f,
                                sweepAngle = 140f,
                                useCenter = false,
                                topLeft = Offset(ringX - 12.dp.toPx(), ringY - 8.dp.toPx()),
                                size = Size(20.dp.toPx(), 16.dp.toPx()),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                    }
                }
                .padding(start = if (showRings) 54.dp else 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CourseBinderCoverCard(
    course: Course,
    notesCount: Int,
    recordingsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverColor = try {
        Color(android.graphics.Color.parseColor(course.coverColorHex))
    } catch (_: Exception) {
        BinderEspresso
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("course_binder_card_${course.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = coverColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Spine (Binder Spine with Gold Rings effect)
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .background(Color(0x33000000))
                    .drawBehind {
                        // 3 rings on spine
                        val spineWidth = size.width
                        val spineHeight = size.height
                        val yOffsets = listOf(0.25f, 0.5f, 0.75f)
                        yOffsets.forEach { fraction ->
                            val y = spineHeight * fraction
                            drawRoundRect(
                                color = BinderGoldRing,
                                topLeft = Offset(4.dp.toPx(), y - 4.dp.toPx()),
                                size = Size(spineWidth - 8.dp.toPx(), 8.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }
            )

            // Binder Cover Label
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3DFFFFFF)
                        ) {
                            Text(
                                text = "${course.code.ifBlank { "MK" }} • ${course.credits} SKS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${course.scheduleDay}, ${course.scheduleTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
                        text = "Dosen: ${course.lecturerName.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom stats pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$notesCount Catatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$recordingsCount Rekaman",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Text(
                        text = "Buka Binder ❯",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BinderGoldRing
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BinderPaperCard, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barsCount = 28
        val paddedList = if (amplitudes.size < barsCount) {
            List(barsCount - amplitudes.size) { 0.1f } + amplitudes
        } else {
            amplitudes.takeLast(barsCount)
        }

        paddedList.forEach { amp ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isRecording) (amp * 40f).coerceIn(4f, 40f) else 6f,
                animationSpec = tween(durationMillis = 80),
                label = "waveHeight"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(animatedHeight.dp)
                    .background(
                        if (isRecording) PastelTerracotta else BinderTextSecondary.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun AudioPlaybackBar(
    isPlaying: Boolean,
    currentSeconds: Int,
    durationSeconds: Int,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = BinderPaperCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Seekbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(currentSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = BinderTextSecondary
                )
                Slider(
                    value = currentSeconds.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..durationSeconds.coerceAtLeast(1).toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = BinderEspresso,
                        activeTrackColor = BinderWarmCaramel,
                        inactiveTrackColor = BinderPaperSand
                    )
                )
                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = BinderTextSecondary
                )
            }

            // Controls & Speed Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind 10s
                IconButton(onClick = onRewind) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Mundur 10 detik",
                        tint = BinderEspresso
                    )
                }

                // Play / Pause Button
                Surface(
                    shape = CircleShape,
                    color = BinderEspresso,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onPlayPause)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Jeda" else "Putar",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    )
                }

                // Forward 10s
                IconButton(onClick = onForward) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Maju 10 detik",
                        tint = BinderEspresso
                    )
                }

                // Speed Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BinderWarmCaramel else BinderPaperSand)
                                .clickable { onSpeedChange(speed) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else BinderTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
