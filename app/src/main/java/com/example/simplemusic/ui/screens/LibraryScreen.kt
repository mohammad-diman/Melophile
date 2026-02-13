package com.example.simplemusic.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplemusic.model.Song
import com.example.simplemusic.model.SortOrder
import com.example.simplemusic.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.example.simplemusic.R
import com.example.simplemusic.ui.components.SongCard
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset

@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentSong: Song?,
    searchQuery: String,
    accentColor: Color,
    onSearchChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onEditClick: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    onSortChange: (SortOrder) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative Background Icons Cluster
            val decoIcons = listOf(
                Icons.Rounded.QueueMusic to (120.dp to DpOffset(20.dp, (-20).dp)),
                Icons.Rounded.Radio to (80.dp to DpOffset(100.dp, 40.dp)),
                Icons.Rounded.LibraryMusic to (60.dp to DpOffset((-30).dp, 10.dp)),
                Icons.Rounded.SpeakerGroup to (100.dp to DpOffset(220.dp, (-30).dp)),
                Icons.Rounded.Piano to (70.dp to DpOffset(300.dp, 20.dp)),
                Icons.Rounded.VolumeUp to (50.dp to DpOffset((-10).dp, 60.dp))
            )

            decoIcons.forEachIndexed { index, (icon, pos) ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(pos.first)
                        .align(if (index % 2 == 0) Alignment.TopEnd else Alignment.TopStart)
                        .offset(x = pos.second.x, y = pos.second.y)
                        .graphicsLayer { 
                            rotationZ = (index * 30f) 
                            alpha = if (index % 3 == 0) 0.03f else 0.015f
                        },
                    tint = SoftWhite
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(stringResource(R.string.library), style = MaterialTheme.typography.titleMedium, color = MutedText)
                                        Text("${songs.size} ${stringResource(R.string.songs)}", style = MaterialTheme.typography.labelSmall, color = accentColor)
                                    }
                                    Row {
                    
                        Box {
                            var showSortMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Rounded.Sort, null, tint = SoftWhite)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(GlassColor)
                            ) {
                                SortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label, color = SoftWhite, style = MaterialTheme.typography.bodyMedium) },
                                        onClick = { onSortChange(order); showSortMenu = false }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Rounded.Settings, null, tint = SoftWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassColor.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, SoftWhite.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = SoftWhite),
                    cursorBrush = SolidColor(accentColor),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Rounded.Search, 
                                null, 
                                tint = accentColor.copy(alpha = 0.8f), 
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        stringResource(R.string.search_hint), 
                                        color = MutedText.copy(alpha = 0.7f), 
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, null, tint = MutedText, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SongCard(
                        song = song, 
                        isCurrent = currentSong?.id == song.id,
                        accentColor = accentColor,
                        onEditClick = { onEditClick(song) }
                    ) {
                        onSongClick(song)
                    }
                }
            }
        }
    }
}
