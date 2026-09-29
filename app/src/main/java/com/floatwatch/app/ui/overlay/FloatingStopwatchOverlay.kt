package com.floatwatch.app.ui.overlay

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floatwatch.app.data.OverlayTheme
import com.floatwatch.app.engine.StopwatchEngine
import com.floatwatch.app.ui.theme.getOverlayThemeColors

@Composable
fun FloatingStopwatchOverlay(
    theme: OverlayTheme,
    opacity: Float,
    isProUser: Boolean,
    hapticsEnabled: Boolean,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    onCloseClicked: () -> Unit
) {
    val context = LocalContext.current
    val timerState by StopwatchEngine.state.collectAsState()
    val colors = getOverlayThemeColors(theme)

    var isCompactMode by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        if (!hapticsEnabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        }
    }

    val (mainTime, millis) = remember(timerState.elapsedRealtimeMs) {
        val (m, ms, _) = StopwatchEngine.formatTime(timerState.elapsedRealtimeMs)
        Pair(m, ms)
    }

    Box(
        modifier = Modifier
            .alpha(opacity)
            .shadow(16.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(colors.background)
            .border(1.2.dp, colors.borderColor, RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle & Mode toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag Pill Indicator
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryText.copy(alpha = 0.4f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimize / Compact Toggle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .clickable {
                                triggerHaptic()
                                isCompactMode = !isCompactMode
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompactMode) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Toggle Mini Mode",
                            tint = colors.primaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .clickable {
                                triggerHaptic()
                                onCloseClicked()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Floating Stopwatch",
                            tint = colors.secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Timer Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = mainTime,
                    fontSize = if (isCompactMode) 22.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.primaryText,
                    letterSpacing = 1.sp
                )
                Text(
                    text = millis,
                    fontSize = if (isCompactMode) 14.sp else 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.accent,
                    modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                )
            }

            // Controls (Hidden in compact mini-hud mode)
            AnimatedVisibility(
                visible = !isCompactMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Play/Pause Action
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                if (timerState.isRunning) {
                                    StopwatchEngine.pause()
                                } else {
                                    StopwatchEngine.start()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        ) {
                            Icon(
                                imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (timerState.isRunning) "Pause" else "Start",
                                tint = colors.background,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Lap Action
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                StopwatchEngine.lap(isProUser)
                            },
                            enabled = timerState.elapsedRealtimeMs > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Lap",
                                tint = if (timerState.elapsedRealtimeMs > 0) colors.primaryText else colors.secondaryText.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset Action
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                StopwatchEngine.reset()
                            },
                            enabled = timerState.elapsedRealtimeMs > 0 && !timerState.isRunning,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = if (timerState.elapsedRealtimeMs > 0 && !timerState.isRunning) colors.primaryText else colors.secondaryText.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Last lap preview if exists
                    if (timerState.laps.isNotEmpty()) {
                        val latestLap = timerState.laps.first()
                        val (_, _, formattedLap) = StopwatchEngine.formatTime(latestLap.lapTimeMs)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lap ${latestLap.lapIndex}: $formattedLap",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}
