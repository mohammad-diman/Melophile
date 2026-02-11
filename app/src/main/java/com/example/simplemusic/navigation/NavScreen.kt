package com.example.simplemusic.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.simplemusic.R

sealed class NavScreen(val route: String, val title: String, val iconRes: Int? = null, val iconVector: ImageVector? = null) {
    object Home : NavScreen("home", "Home", iconRes = R.drawable.ic_home_custom)
    object Library : NavScreen("library", "Library", iconRes = R.drawable.ic_library_custom)
    object Settings : NavScreen("settings", "Settings", iconVector = Icons.Rounded.Settings)
}

val bottomNavItems = listOf(
    NavScreen.Home,
    NavScreen.Library
)
