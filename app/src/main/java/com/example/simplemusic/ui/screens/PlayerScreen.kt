package com.example.simplemusic.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextAlign
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
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = Player.RepeatMode.Restart
        ),
        label = "albumArtRotation"
    )

    val albumArtScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.85f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "albumArtScale"
    )

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Blurred Background
        AsyncImage(
            model = song.albumArtUri, contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(100.dp).graphicsLayer { alpha = 0.35f },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = SoftWhite)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = SoftWhite.copy(0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSleepTimerActive) accentColor.copy(0.15f) else Color.White.copy(0.08f))
                        .border(1.dp, if (isSleepTimerActive) accentColor.copy(0.3f) else Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                        .clickable { onSleepTimerClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Timer, 
                        null, 
                        tint = if (isSleepTimerActive) accentColor else SoftWhite, 
                        modifier = Modifier.size(18.dp)
                    )
                    if (isSleepTimerActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(sleepTimerText, color = accentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // Premium Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = albumArtScale
                        scaleY = albumArtScale
                        rotationZ = if (isPlaying) rotation else 0f
                    }
                    .shadow(60.dp, CircleShape, spotColor = accentColor.copy(0.5f))
                    .border(8.dp, Color.White.copy(0.05f), CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Song Info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    song.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress Section
            WavySlider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { onSeekTo((it * duration).toLong()) },
                isPlaying = isPlaying,
                accentColor = accentColor
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall, color = SoftWhite.copy(0.5f))
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall, color = SoftWhite.copy(0.5f))
            }
            
            Spacer(modifier = Modifier.height(48.dp))

            // Refined Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffleToggle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        tint = if (isShuffleEnabled) accentColor else SoftWhite.copy(0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Rounded.SkipPrevious, null, modifier = Modifier.size(44.dp), tint = SoftWhite)
                    }
                    
                    Surface(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(84.dp),
                        shape = CircleShape,
                        color = accentColor,
                        shadowElevation = 16.dp,
                        tonalElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(onClick = onNext) {
                        Icon(Icons.Rounded.SkipNext, null, modifier = Modifier.size(44.dp), tint = SoftWhite)
                    }
                }

                IconButton(onClick = onRepeatToggle) {
                    val icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                        else -> Icons.Rounded.Repeat
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else SoftWhite.copy(0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
