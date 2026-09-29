package com.floatwatch.app.data

enum class OverlayTheme(val displayName: String, val isPro: Boolean) {
    MINIMAL_DARK("Obsidian Minimal", false),
    MINIMAL_LIGHT("Clean White", false),
    CYBERPUNK("Cyberpunk Neon", true),
    GLASSMORPHISM("Frosted Glass", true),
    OLED_RED("OLED Stealth Red", true)
}

data class Lap(
    val lapIndex: Int,
    val lapTimeMs: Long,
    val splitTimeMs: Long
)

data class TimerState(
    val isRunning: Boolean = false,
    val elapsedRealtimeMs: Long = 0L,
    val laps: List<Lap> = emptyList()
)
