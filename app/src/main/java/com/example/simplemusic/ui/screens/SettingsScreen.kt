package com.example.simplemusic.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplemusic.R
import com.example.simplemusic.ui.theme.*
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUri: Uri?,
    accentColor: Color,
    onBack: () -> Unit,
    onDirectorySelected: (Uri) -> Unit,
    onRefresh: () -> Unit,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onResetStats: () -> Unit
) {
    var selectedPath by remember { 
        mutableStateOf(currentUri?.path?.split(":")?.last() ?: "All Device Music (Default)") 
    }
    
    var showLanguageMenu by remember { mutableStateOf(false) }
    val languages = listOf("system" to "System Default", "en" to "English", "id" to "Indonesian")
    val currentLanguageLabel = languages.find { it.first == currentLanguage }?.second ?: "System Default"

    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            selectedPath = it.path?.split(":")?.last() ?: it.toString()
            onDirectorySelected(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SoftWhite,
                    navigationIconContentColor = SoftWhite
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // ... (deco icons)
            val decoIcons = listOf(
                Icons.Rounded.Settings to (120.dp to DpOffset(20.dp, (-20).dp)),
                Icons.Rounded.Folder to (80.dp to DpOffset(100.dp, 40.dp))
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
                            rotationZ = (index * 45f) 
                            alpha = 0.02f
                        },
                    tint = SoftWhite
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Text(
                    stringResource(R.string.library_mgmt),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Music Directory Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)),
                    onClick = { directoryLauncher.launch(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentColor.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Folder, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(stringResource(R.string.music_dir), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(selectedPath, style = MaterialTheme.typography.bodySmall, color = MutedText, maxLines = 1)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Language Selection Card
                Box {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)),
                        onClick = { showLanguageMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Translate, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(26.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(stringResource(R.string.app_language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(currentLanguageLabel, style = MaterialTheme.typography.bodySmall, color = MutedText)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier
                            .width(200.dp)
                            .background(GlassColor.copy(alpha = 0.95f))
                            .border(BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    ) {
                        languages.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = SoftWhite) },
                                onClick = {
                                    onLanguageSelected(code)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Refresh Library Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassColor.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, SoftWhite.copy(alpha = 0.1f)),
                    onClick = onRefresh
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(26.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(stringResource(R.string.scan_refresh), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.scan_desc), style = MaterialTheme.typography.bodySmall, color = MutedText)
                        }
                    }
                }
                


// ... (inside Column)
                Spacer(modifier = Modifier.height(16.dp))

                // Reset Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                    onClick = onResetStats
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Red.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color.Red.copy(0.8f), modifier = Modifier.size(26.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text("Reset Statistics", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.Red.copy(0.8f))
                            Text("Clear play counts and activity", style = MaterialTheme.typography.bodySmall, color = MutedText)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(
                    onClick = { /* Reset Logic */ },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Reset to All Device Music", color = MutedText.copy(alpha = 0.6f))
                }
            }
        }
    }
}

