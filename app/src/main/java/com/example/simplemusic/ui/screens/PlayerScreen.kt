package com.example.simplemusic.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.simplemusic.model.Song
import com.example.simplemusic.ui.theme.*
import com.example.simplemusic.ui.components.WavySlider
import com.example.simplemusic.utils.formatTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullPlayerGlass(
    song: Song,
    isPlaying: Boolean,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    isSleepTimerActive: Boolean,
    sleepTimerText: String,
    currentPosition: Long,
    duration: Long,
    accentColor: Color,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRepeatToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        AsyncImage(
            model = song.albumArtUri, contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(80.dp).graphicsLayer { alpha = 0.4f },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = SoftWhite)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSleepTimerActive) accentColor.copy(0.2f) else Color.White.copy(0.1f))
                        .clickable { onSleepTimerClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Timer, null, tint = if (isSleepTimerActive) accentColor else SoftWhite, modifier = Modifier.size(18.dp))
                    if (isSleepTimerActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(sleepTimerText, color = accentColor, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).shadow(40.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp)
            ) {
                AsyncImage(model = song.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.weight(0.5f))
            Text(song.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.basicMarquee())
            Text(song.artist, style = MaterialTheme.typography.titleLarge, color = accentColor, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Komponen Slider Bergelombang Baru
            WavySlider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { onSeekTo((it * duration).toLong()) },
                isPlaying = isPlaying,
                accentColor = accentColor
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall, color = MutedText)
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall, color = MutedText)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRepeatToggle) {
                    val icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                        else -> Icons.Rounded.Repeat
                    }
                    Icon(imageVector = icon, contentDescription = null, tint = if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else SoftWhite.copy(0.5f))
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Rounded.SkipPrevious, null, modifier = Modifier.size(40.dp), tint = SoftWhite) }
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(accentColor).clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, null, modifier = Modifier.size(40.dp), tint = SoftWhite) }
                IconButton(onClick = onShuffleToggle) {
                    Icon(imageVector = Icons.Rounded.Shuffle, contentDescription = null, tint = if (isShuffleEnabled) accentColor else SoftWhite.copy(0.5f))
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
