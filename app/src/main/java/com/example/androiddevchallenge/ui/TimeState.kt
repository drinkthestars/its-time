/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class TimeState(
    private val coroutineScope: CoroutineScope,
    private val onCountdownStarted: () -> Unit,
    private val onCountdownPaused: () -> Unit,
    private val onCountdownComplete: () -> Unit,
    private val onCountdownReset: () -> Unit
) {
    var isPlaying by mutableStateOf(false)
    var isTimeDisplayAlpha by mutableStateOf(1f)

    var hoursAngle by mutableStateOf(0f)
    var minutesAngle by mutableStateOf(0f)
    var secondsAngle by mutableStateOf(0f)

    var hoursLeft by mutableStateOf(0)
    var minutesLeft by mutableStateOf(0)
    var secondsLeft by mutableStateOf(0)

    private var wasPaused by mutableStateOf(false)
    private var timerJob: Job? = null
    private var blinkJob: Job? = null
    private val totalSecondsLeft: Int
        get() = (hoursLeft * 3600) + (minutesLeft * 60) + secondsLeft

    fun playPause() {
        if (isPlaying) {
            isPlaying = false
            wasPaused = true
            onCountdownPaused()
            timerJob?.cancel()
        } else {
            if (totalSecondsLeft == 0) return
            timerJob?.cancel()
            timerJob = coroutineScope.launch {
                if (!wasPaused) {
                    wasPaused = false
                    delay(TickerStartDelayMillis)
                }
                isPlaying = true
                onCountdownStarted()
                while (totalSecondsLeft > 0) {
                    delay(TickerIntervalMillis)
                    countDown()
                }
                blink()
            }
        }
    }

    private fun countDown() {
        if (secondsLeft > 0) {
            secondsLeft -= 1
            secondsAngle -= 6f
        }
        if (secondsLeft == 0) {
            if (minutesLeft > 0) {
                minutesLeft -= 1
                secondsLeft = 60
                secondsAngle = 360f
                minutesAngle -= 6f
            }
        }
        if (minutesLeft == 0) {
            if (hoursLeft > 0) {
                hoursLeft -= 1
                minutesLeft = 60
                minutesAngle = 360f
                hoursAngle -= 30f
            }
        }
    }

    fun changeHours(change: Change) {
        hoursLeft = when (change) {
            Change.Increase -> hoursLeft + 1
            Change.Decrease -> hoursLeft - 1
        }.coerceIn(0, 12)
        hoursAngle = hoursLeft * 30f
    }

    fun changeMinutes(change: Change) {
        minutesLeft = when (change) {
            Change.Increase -> minutesLeft + 1
            Change.Decrease -> minutesLeft - 1
        }.coerceIn(0, 60)
        minutesAngle = minutesLeft * 6f
    }

    fun changeSeconds(change: Change) {
        secondsLeft = when (change) {
            Change.Increase -> secondsLeft + 1
            Change.Decrease -> secondsLeft - 1
        }.coerceIn(0, 60)
        secondsAngle = secondsLeft * 6f
    }

    fun reset() {
        isPlaying = false
        wasPaused = false
        hoursAngle = 0f
        minutesAngle = 0f
        secondsAngle = 0f
        hoursLeft = 0
        minutesLeft = 0
        secondsLeft = 0
        timerJob?.cancel()
        timerJob = null
        blinkJob?.cancel()
        blinkJob = null
        onCountdownReset()
    }

    private fun blink() {
        blinkJob?.cancel()
        blinkJob = coroutineScope.launch {
            repeat(4) {
                delay(300)
                isTimeDisplayAlpha = if (isTimeDisplayAlpha == 1f) 0f else 1f
            }
            isTimeDisplayAlpha = 1f
            isPlaying = false
            onCountdownComplete()
        }
    }

    enum class Change { Increase, Decrease }
}
