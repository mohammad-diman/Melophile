package com.example.simplemusic.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.simplemusic.model.Song
import com.example.simplemusic.ui.theme.*
import java.util.Calendar

import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MicExternalOn
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.simplemusic.R
import com.example.simplemusic.ui.components.AboutDialog

@Composable
fun HomeScreen(
    dailyMix: List<Song>,
    stats: Map<String, String>,
    weeklyActivity: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    accentColor: androidx.compose.ui.graphics.Color = AccentColor,
    onSongClick: (Song) -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dashboard Header with Decorative Icons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            // Decorative Background Icons Cluster
            val decoIcons = listOf(
                Icons.Rounded.MusicNote to (140.dp to androidx.compose.ui.unit.DpOffset(40.dp, (-30).dp)),
                Icons.Rounded.GraphicEq to (80.dp to androidx.compose.ui.unit.DpOffset((-20).dp, 40.dp)),
                Icons.Rounded.Audiotrack to (60.dp to androidx.compose.ui.unit.DpOffset((-10).dp, (-20).dp)),
                Icons.Rounded.Headset to (100.dp to androidx.compose.ui.unit.DpOffset(120.dp, 20.dp)),
                Icons.Rounded.Album to (70.dp to androidx.compose.ui.unit.DpOffset(200.dp, (-40).dp)),
                Icons.Rounded.MusicVideo to (50.dp to androidx.compose.ui.unit.DpOffset(280.dp, 30.dp)),
                Icons.Rounded.MicExternalOn to (90.dp to androidx.compose.ui.unit.DpOffset((-60).dp, 10.dp))
            )

            decoIcons.forEachIndexed { index, (icon, pos) ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(pos.first)
                        .align(if (index % 2 == 0) Alignment.CenterEnd else Alignment.CenterStart)
                        .offset(x = pos.second.x, y = pos.second.y)
                        .graphicsLayer { 
                            rotationZ = (index * 45f) 
                            alpha = if (index % 3 == 0) 0.04f else 0.02f
                        },
                    tint = SoftWhite
                )
            }

            IconButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 20.dp)
                    .size(52.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(SoftWhite.copy(alpha = 0.15f), GlassColor.copy(alpha = 0.6f))
                        )
                    )
                    .border(BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "About",
                    tint = SoftWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("$greeting,", style = MaterialTheme.typography.bodyLarge, color = MutedText)
                Text(stringResource(R.string.dashboard), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp))
            }
        }

        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false }, accentColor = accentColor)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (dailyMix.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, GlassColor.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(stringResource(R.string.daily_mix), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.based_on_taste), style = MaterialTheme.typography.bodySmall, color = MutedText)
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(end = 4.dp)
                            ) {
                                items(dailyMix) { song ->
                                    DailyMixCard(song) { onSongClick(song) }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, GlassColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.listening_stats), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(stringResource(R.string.total_plays), stats["total"] ?: "0", Modifier.weight(1f))
                            StatCard(stringResource(R.string.top_artist), stats["artist"] ?: "N/A", Modifier.weight(1.5f))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(stringResource(R.string.weekly_activity), style = MaterialTheme.typography.labelSmall, color = MutedText)
                        Spacer(modifier = Modifier.height(16.dp))
                        ModernAreaChart(
                            data = weeklyActivity,
                            accentColor = accentColor,
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentColor.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.discovery), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.explore_collection), style = MaterialTheme.typography.bodySmall, color = MutedText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyMixCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(120.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp)).background(GlassColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(song.artist, style = MaterialTheme.typography.labelSmall, color = MutedText, maxLines = 1)
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedText)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ModernAreaChart(data: List<Float>, accentColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val transitionProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "chartAnimation"
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1).coerceAtLeast(1)
        
        val points = data.mapIndexed { index, value ->
            androidx.compose.ui.geometry.Offset(
                x = index * spacing,
                y = height - (value * height * transitionProgress)
            )
        }

        val path = androidx.compose.ui.graphics.Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val p0 = points[i - 1]
                    val p1 = points[i]
                    cubicTo(
                        (p0.x + p1.x) / 2, p0.y,
                        (p0.x + p1.x) / 2, p1.y,
                        p1.x, p1.y
                    )
                }
            }
        }

        // Draw Fill Gradient
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw Line
        drawPath(
            path = path,
            color = accentColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Draw Dots
        points.forEach { point ->
            drawCircle(
                color = accentColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = DarkBackground,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
