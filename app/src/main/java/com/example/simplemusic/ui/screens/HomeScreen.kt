package com.example.simplemusic.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.simplemusic.ui.components.AboutDialog

@Composable
fun HomeScreen(
    dailyMix: List<Song>,
    stats: Map<String, String>,
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
        // Dashboard Header with Decorative Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            // Decorative Background Icons (Subtle Cluster)
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = (-30).dp)
                    .alpha(0.04f),
                tint = SoftWhite
            )
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-20).dp, y = 40.dp)
                    .alpha(0.03f),
                tint = SoftWhite
            )
            Icon(
                imageVector = Icons.Rounded.Audiotrack,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-10).dp, y = (-20).dp)
                    .alpha(0.03f),
                tint = SoftWhite
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("$greeting,", style = MaterialTheme.typography.bodyLarge, color = MutedText)
                        Text("Dashboard", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp))
                    }
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassColor.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = "About", tint = SoftWhite)
                    }
                }
            }
        }

        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false }, accentColor = accentColor)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
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
                            Text("Daily Mix", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Based on your taste", style = MaterialTheme.typography.bodySmall, color = MutedText)
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
                        Text("Listening Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Total Plays", stats["total"] ?: "0", Modifier.weight(1f))
                            StatCard("Top Artist", stats["artist"] ?: "N/A", Modifier.weight(1.5f))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Weekly Activity", style = MaterialTheme.typography.labelSmall, color = MutedText)
                        Spacer(modifier = Modifier.height(12.dp))
                        SimpleBarChart(
                            data = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.8f), // Mock data
                            modifier = Modifier.fillMaxWidth().height(80.dp)
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
                            Text("Discovery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Explore your full collection in the Library.", style = MaterialTheme.typography.bodySmall, color = MutedText)
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
fun SimpleBarChart(data: List<Float>, modifier: Modifier = Modifier) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        data.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bar container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SoftWhite.copy(alpha = 0.05f))
                ) {
                    // Actual progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(value.coerceIn(0.05f, 1f)) // Ensure at least a small visible bar
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Day Label
                Text(
                    text = days.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (value > 0.7f) AccentColor else MutedText,
                    fontWeight = if (value > 0.7f) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
