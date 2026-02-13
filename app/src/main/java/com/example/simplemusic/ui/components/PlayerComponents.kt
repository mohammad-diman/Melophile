package com.example.simplemusic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.simplemusic.model.Song
import com.example.simplemusic.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun SongCard(
    song: Song, 
    isCurrent: Boolean, 
    accentColor: Color = AccentColor,
    onEditClick: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val backgroundColor = if (isCurrent) accentColor.copy(0.2f) else GlassColor.copy(0.3f)
    val borderBrush = if (isCurrent) Brush.horizontalGradient(listOf(accentColor, Color.Transparent)) else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(if (borderBrush != null) Modifier.border(1.dp, borderBrush, RoundedCornerShape(20.dp)) else Modifier)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.albumArtUri)
                .crossfade(true)
                .size(160, 160) // Optimized for list thumbnails
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MutedText, maxLines = 1)
        }
        
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            ) {
                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = if (isCurrent) SoftWhite else MutedText)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .width(160.dp)
                    .background(GlassColor.copy(alpha = 0.95f))
                    .border(BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Info", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) },
                    leadingIcon = { 
                        Icon(
                            Icons.Rounded.Edit, 
                            null, 
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        ) 
                    },
                    onClick = {
                        showMenu = false
                        onEditClick()
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = SoftWhite,
                        leadingIconColor = accentColor
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayerGlass(
    song: Song, 
    isPlaying: Boolean, 
    accentColor: Color,
    onTogglePlay: () -> Unit, 
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(76.dp)
            .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.toInt(), 0) }
            .shadow(20.dp, RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (kotlin.math.abs(offsetX.value) > size.width / 3) {
                            val target = if (offsetX.value > 0) size.width.toFloat() else -size.width.toFloat()
                            scope.launch {
                                offsetX.animateTo(target, tween(300))
                                onDismiss()
                                offsetX.snapTo(0f)
                            }
                        } else {
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    }
                )
            }
            .clickable(
                onClick = onClick, 
                interactionSource = remember { MutableInteractionSource() }, 
                indication = null
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri, contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title, 
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), 
                    maxLines = 1, 
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    song.artist, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MutedText
                )
            }
            IconButton(
                onClick = onTogglePlay, 
                modifier = Modifier.size(48.dp).background(accentColor, CircleShape)
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, 
                    contentDescription = null, 
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 1500 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isPlaying) 8.dp.value else 1.dp.value,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "amplitude"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onValueChange((offset.x / size.width).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val centerY = height / 2
        val thumbX = width * value

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Bagian Sisa (Belum diputar - Garis Lurus Tenang)
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(thumbX, centerY),
                end = androidx.compose.ui.geometry.Offset(width, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. Bagian Aktif (Sudah diputar - Gelombang Energi)
            val wavePath = Path()
            wavePath.moveTo(0f, centerY)
            
            val amplitudePx = animatedAmplitude.dp.toPx()
            val wavelength = 50.dp.toPx()

            for (x in 0..thumbX.toInt() step 2) {
                val relativeX = x / wavelength
                val y = centerY + amplitudePx * sin(relativeX * 2 * Math.PI.toFloat() - phase)
                wavePath.lineTo(x.toFloat(), y)
            }
            // Pastikan ujung gelombang menutup pas ke Thumb
            wavePath.lineTo(thumbX, centerY)

            drawPath(
                path = wavePath,
                color = accentColor,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Thumb (Kepala penunjuk)
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(thumbX, centerY),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = accentColor,
                radius = 7.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(thumbX, centerY)
            )
        }
    }
}
