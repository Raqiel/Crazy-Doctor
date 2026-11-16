package com.plantaomaluco.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.plantaomaluco.domain.Tool
import com.plantaomaluco.resources.Res
import com.plantaomaluco.resources.tool_bandage
import com.plantaomaluco.resources.tool_needle
import com.plantaomaluco.resources.tool_sponge
import com.plantaomaluco.resources.tool_spray
import com.plantaomaluco.resources.tool_tweezers
import org.jetbrains.compose.resources.StringResource

val Gold = Color(0xFFFFB300)

private val ClinicColors = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    secondary = Color(0xFFFF7A59),
    onSecondary = Color.White,
    background = Color(0xFFEFF8F6),
    onBackground = Color(0xFF1F2D2B),
    surface = Color.White,
    onSurface = Color(0xFF1F2D2B),
    surfaceVariant = Color(0xFFDDEFEB),
    onSurfaceVariant = Color(0xFF51625F),
    error = Color(0xFFD7263D),
)

@Composable
fun ClinicTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ClinicColors, content = content)
}

val Tool.accent: Color
    get() = when (this) {
        Tool.TWEEZERS -> Color(0xFF607D8B)
        Tool.NEEDLE -> Color(0xFF3949AB)
        Tool.SPONGE -> Color(0xFFF9A825)
        Tool.SPRAY -> Color(0xFF039BE5)
        Tool.BANDAGE -> Color(0xFFD08C3A)
    }

val Tool.label: StringResource
    get() = when (this) {
        Tool.TWEEZERS -> Res.string.tool_tweezers
        Tool.NEEDLE -> Res.string.tool_needle
        Tool.SPONGE -> Res.string.tool_sponge
        Tool.SPRAY -> Res.string.tool_spray
        Tool.BANDAGE -> Res.string.tool_bandage
    }

fun starsText(stars: Int): String = "★".repeat(stars) + "☆".repeat(3 - stars)
