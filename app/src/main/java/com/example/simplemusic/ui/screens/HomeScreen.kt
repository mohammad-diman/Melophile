package com.example.simplemusic.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun HomeScreen(
    dailyMix: List<Song>,
    stats: Map<String, String>,
    onSongClick: (Song) -> Unit
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Simple Dashboard Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp, 24.dp, 24.dp, 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = com.example.simplemusic.R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).padding(end = 12.dp)
                )
                Column {
                    Text("$greeting,", style = MaterialTheme.typography.bodyLarge, color = MutedText)
                    Text("Dashboard", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (dailyMix.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text("Daily Mix", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Based on your taste", style = MaterialTheme.typography.bodySmall, color = MutedText)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(dailyMix) { song ->
                                DailyMixCard(song) { onSongClick(song) }
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("Listening Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Total Plays", stats["total"] ?: "0", Modifier.weight(1f))
                        StatCard("Top Artist", stats["artist"] ?: "N/A", Modifier.weight(1.5f))
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentColor.copy(alpha = 0.1f))
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
