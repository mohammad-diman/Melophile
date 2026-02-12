package com.example.simplemusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

// Imports
import com.example.simplemusic.ui.theme.*
import com.example.simplemusic.model.*
import com.example.simplemusic.utils.*
import com.example.simplemusic.ui.screens.*
import com.example.simplemusic.ui.components.*
import com.example.simplemusic.navigation.NavScreen
import com.example.simplemusic.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val musicViewModel: MusicViewModel = viewModel()
            
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = DarkBackground, surface = GlassColor,
                    primary = musicViewModel.dynamicAccentColor, // Dynamic Primary
                    onBackground = SoftWhite, onSurface = SoftWhite
                ),
                typography = Typography
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    MusicAppRoot(musicViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAppRoot(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Restore last route
    LaunchedEffect(Unit) {
        val lastRoute = viewModel.settingsManager.getLastRoute()
        if (lastRoute != null && lastRoute != NavScreen.Home.route) {
            navController.navigate(lastRoute) {
                popUpTo(NavScreen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Save route changes
    DisposableEffect(navController) {
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { viewModel.settingsManager.saveLastRoute(it) }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) viewModel.loadSongs(context)
    }

    LaunchedEffect(viewModel.selectedFolderUri) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadSongs(context)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background dengan warna dinamis yang meluncur halus
        AnimatedBackgroundRoot(viewModel.dynamicAccentColor)

        Scaffold(
            bottomBar = { MusicBottomNavigation(navController, viewModel.dynamicAccentColor) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(navController = navController, startDestination = NavScreen.Home.route) {
                    composable(NavScreen.Home.route) {
                        HomeScreen(
                            dailyMix = viewModel.dailyMix,
                            stats = viewModel.stats,
                            accentColor = viewModel.dynamicAccentColor,
                            onSongClick = { viewModel.playSong(it) }
                        )
                    }
                    composable(NavScreen.Library.route) { 
                        LibraryScreen(
                            songs = viewModel.songs,
                            currentSong = viewModel.currentSong,
                            searchQuery = viewModel.searchQuery,
                            accentColor = viewModel.dynamicAccentColor,
                            onSearchChange = { viewModel.searchQuery = it },
                            onSongClick = { viewModel.playSong(it) },
                            onEditClick = { viewModel.songToEdit = it },
                            onSettingsClick = { navController.navigate(NavScreen.Settings.route) },
                            onSortChange = { viewModel.updateSortOrder(it) }
                        )
                    }
                    composable(NavScreen.Settings.route) {
                        SettingsScreen(
                            currentUri = viewModel.selectedFolderUri,
                            accentColor = viewModel.dynamicAccentColor,
                            onBack = { navController.popBackStack() },
                            onDirectorySelected = { viewModel.updateFolder(it) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = viewModel.currentSong != null && !viewModel.showFullPlayer,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    viewModel.currentSong?.let { song ->
                        MiniPlayerGlass(
                            song = song,
                            isPlaying = viewModel.isPlaying,
                            accentColor = viewModel.dynamicAccentColor,
                            onTogglePlay = { viewModel.togglePlay() },
                            onDismiss = { 
                                viewModel.player?.pause()
                                viewModel.currentSong = null 
                            },
                            onClick = { viewModel.showFullPlayer = true }
                        )
                    }
                }
            }
        }
    }

    // Edit Song Dialog
    viewModel.songToEdit?.let { song ->
        var tempTitle by remember { mutableStateOf(song.title) }
        var tempArtist by remember { mutableStateOf(song.artist) }

        AlertDialog(
            onDismissRequest = { viewModel.songToEdit = null },
            containerColor = GlassColor,
            title = { Text("Edit Music Info", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, 
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = viewModel.dynamicAccentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = tempArtist,
                        onValueChange = { tempArtist = it },
                        label = { Text("Artist") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, 
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = viewModel.dynamicAccentColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSongInfo(song.id, tempTitle, tempArtist)
                        viewModel.songToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.dynamicAccentColor)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.songToEdit = null }) {
                    Text("Cancel", color = SoftWhite)
                }
            }
        )
    }

    AnimatedVisibility(
        visible = viewModel.showFullPlayer,
        enter = slideInVertically(
            initialOffsetY = { it }, 
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }, 
            animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
        ) + fadeOut(animationSpec = tween(400))
    ) {
        viewModel.currentSong?.let { song ->
            BackHandler { viewModel.showFullPlayer = false }
            FullPlayerGlass(
                song = song,
                isPlaying = viewModel.isPlaying,
                repeatMode = viewModel.repeatMode,
                isShuffleEnabled = viewModel.isShuffleEnabled,
                isSleepTimerActive = viewModel.isSleepTimerActive,
                sleepTimerText = "${viewModel.sleepTimerMinutes}m",
                currentPosition = viewModel.currentPosition,
                duration = viewModel.duration,
                accentColor = viewModel.dynamicAccentColor,
                onTogglePlay = { viewModel.togglePlay() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onRepeatToggle = { viewModel.toggleRepeat() },
                onShuffleToggle = { viewModel.toggleShuffle() },
                onSleepTimerClick = { viewModel.showSleepTimerSheet = true },
                onSeekTo = { viewModel.seekTo(it) },
                onClose = { viewModel.showFullPlayer = false }
            )
        }
    }

    if (viewModel.showSleepTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showSleepTimerSheet = false },
            containerColor = GlassColor,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Sleep Timer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                listOf(0, 15, 30, 45, 60).forEach { mins ->
                    ListItem(
                        headlineContent = { Text(if (mins == 0) "Off" else "$mins Minutes") },
                        modifier = Modifier.clickable {
                            viewModel.setSleepTimer(mins)
                            viewModel.showSleepTimerSheet = false
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBackgroundRoot(accentColor: Color) {
    // Animasi perubahan warna agar meluncur halus saat ganti lagu
    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(1500)
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(animatedAccent.copy(0.15f), DarkBackground, Color(0xFF0D47A1).copy(0.05f)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height)
            )
        )
    }
}
