package com.example.simplemusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplemusic.ui.theme.GlassColor
import com.example.simplemusic.ui.theme.MutedText
import com.example.simplemusic.ui.theme.SoftWhite

@Composable
fun AboutDialog(onDismiss: () -> Unit, accentColor: Color) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassColor,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                "About Melophile",
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Melophile adalah pemutar musik modern yang dirancang untuk memberikan pengalaman mendengarkan yang elegan dan personal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Aplikasi ini dibuat sebagai proyek eksplorasi UI/UX menggunakan Jetpack Compose, berfokus pada estetika Glassmorphism dan kemudahan navigasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Aplikasi ini dibuat karena developer suka mendengar musik setiap hari dan dia mau aplikasi yang terlihat premium dan no iklan tentu saja jika internet menyala.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = accentColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}
