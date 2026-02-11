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
import com.example.simplemusic.ui.components.SongCard

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
                    Text("Your Library", style = MaterialTheme.typography.titleMedium, color = MutedText)
                    Text("${songs.size} songs", style = MaterialTheme.typography.labelSmall, color = accentColor)
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
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassColor.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = SoftWhite),
                cursorBrush = SolidColor(accentColor),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Rounded.Search, null, tint = MutedText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search library...", color = MutedText, style = MaterialTheme.typography.bodyMedium)
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
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
