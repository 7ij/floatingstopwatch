package com.floatwatch.app.engine

import android.os.SystemClock
import com.floatwatch.app.data.Lap
import com.floatwatch.app.data.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object StopwatchEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null

    private var startTimeRealtime: Long = 0L
    private var accumulatedTimeMs: Long = 0L
    private var lastLapSplitMs: Long = 0L

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun start() {
        if (_state.value.isRunning) return

        startTimeRealtime = SystemClock.elapsedRealtime()
        _state.value = _state.value.copy(isRunning = true)

        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val currentElapsed = accumulatedTimeMs + (SystemClock.elapsedRealtime() - startTimeRealtime)
                _state.value = _state.value.copy(elapsedRealtimeMs = currentElapsed)
                delay(16) // Smooth 60fps refresh rate
            }
        }
    }

    fun pause() {
        if (!_state.value.isRunning) return

        tickerJob?.cancel()
        accumulatedTimeMs += SystemClock.elapsedRealtime() - startTimeRealtime
        _state.value = _state.value.copy(
            isRunning = false,
            elapsedRealtimeMs = accumulatedTimeMs
        )
    }

    fun reset() {
        tickerJob?.cancel()
        startTimeRealtime = 0L
        accumulatedTimeMs = 0L
        lastLapSplitMs = 0L
        _state.value = TimerState()
    }

    fun lap(isProUser: Boolean): Boolean {
        val currentTotal = if (_state.value.isRunning) {
            accumulatedTimeMs + (SystemClock.elapsedRealtime() - startTimeRealtime)
        } else {
            accumulatedTimeMs
        }

        val currentLaps = _state.value.laps
        // Free user limitation: 10 laps max
        if (!isProUser && currentLaps.size >= 10) {
            return false
        }

        val lapTime = currentTotal - lastLapSplitMs
        lastLapSplitMs = currentTotal

        val newLap = Lap(
            lapIndex = currentLaps.size + 1,
            lapTimeMs = lapTime,
            splitTimeMs = currentTotal
        )

        _state.value = _state.value.copy(laps = listOf(newLap) + currentLaps)
        return true
    }

    fun formatTime(timeMs: Long): Triple<String, String, String> {
        val totalSecs = timeMs / 1000
        val millis = (timeMs % 1000) / 10 // Two decimal places (centiseconds)
        val seconds = totalSecs % 60
        val minutes = (totalSecs / 60) % 60
        val hours = totalSecs / 3600

        val mainPart = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        val milliPart = String.format(".%02d", millis)
        val fullString = "$mainPart$milliPart"

        return Triple(mainPart, milliPart, fullString)
    }
}
