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

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.androiddevchallenge.ui.theme.cubeEdges
import com.example.androiddevchallenge.ui.theme.lightNavy
import com.example.androiddevchallenge.ui.theme.lightPurpleBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal const val TickerIntervalMillis = 1000L
internal const val TickerStartDelayMillis = 400L

@Composable
fun TimerApp() {
    Surface(color = MaterialTheme.colors.background) {
        val vibrator = vibrator()
        val coroutineScope = rememberCoroutineScope()
        val cubeState = rememberCubeState(coroutineScope)
        val state = remember {
            TimeState(
                coroutineScope,
                onCountdownStarted = { cubeState.animate() },
                onCountdownComplete = {
                    vibrator?.vibrate()
                    cubeState.stop()
                },
                onCountdownPaused = { cubeState.stop() },
                onCountdownReset = { cubeState.stop() }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Timer(modifier = Modifier.fillMaxWidth(0.83f), state, cubeState)
            TimerPlaybackControls(
                modifier = Modifier.size(50.dp),
                onPlayPause = { state.playPause() },
                onReset = { state.reset() },
                state = state
            )
        }
    }
}

@Composable
private fun Timer(
    modifier: Modifier = Modifier,
    state: TimeState,
    cubeState: CubeState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Brush.radialGradient(listOf(lightNavy, MaterialTheme.colors.background)))
    ) {
        TimeCircles(
            modifier = modifier,
            state = state
        ) {
            RotatingCube(
                modifier = Modifier.align(Alignment.Center),
                cubeState = cubeState,
                color = cubeEdges,
                scaleFactor = 1.55f
            )
            TimeDisplay(modifier = Modifier.align(Alignment.Center), state)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TimeDisplay(modifier: Modifier, state: TimeState) {
    Row(
        modifier = modifier.fillMaxSize().alpha(state.isTimeDisplayAlpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TimeChooser(
            controlsSize = 30.dp,
            showControls = !state.isPlaying,
            onChange = { state.changeHours(it) }
        ) {
            TimeText(state.hoursLeft)
        }
        Spacer(Modifier.width(4.dp))
        TimeSeparator()
        Spacer(Modifier.width(4.dp))
        TimeChooser(
            controlsSize = 30.dp,
            showControls = !state.isPlaying,
            onChange = { state.changeMinutes(it) }
        ) {
            TimeText(state.minutesLeft)
        }
        Spacer(Modifier.width(4.dp))
        TimeSeparator()
        Spacer(Modifier.width(4.dp))
        TimeChooser(
            controlsSize = 30.dp,
            showControls = !state.isPlaying,
            onChange = { state.changeSeconds(it) }
        ) {
            TimeText(state.secondsLeft)
        }
    }
}

@Composable
private fun TimeCircles(
    modifier: Modifier = Modifier,
    state: TimeState,
    content: @Composable BoxScope.() -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val hours by animatedTimeAngleAsState(state.hoursAngle,)
    val minutes by animatedTimeAngleAsState(state.minutesAngle)
    val seconds by animatedTimeAngleAsState(state.secondsAngle)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onSizeChanged { size = it }
            .drawWithCache {
                val timeCircleBrushes = TimeCircleBrushes()
                val stroke = Stroke(
                    width = 12f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                onDrawBehind {
                    rotate(degrees = -90f) {
                        drawTimeCircles(
                            stroke = stroke,
                            brushes = timeCircleBrushes,
                            size,
                            seconds,
                            minutes,
                            hours
                        )
                    }
                }
            },
    ) {
        content()
    }
}

private fun DrawScope.drawTimeCircles(
    stroke: Stroke,
    brushes: TimeCircleBrushes,
    size: IntSize,
    seconds: Float,
    minutes: Float,
    hours: Float
) {
    drawArc(
        brush = brushes.outerSweepGradient,
        startAngle = 1.5f,
        sweepAngle = seconds,
        useCenter = false,
        style = stroke
    )
    drawArc(
        brush = brushes.midSweepGradient,
        startAngle = 1.5f,
        sweepAngle = minutes,
        useCenter = false,
        topLeft = Offset(64f, 64f),
        size = Size(size.width * 0.43f * 2f, size.width * 0.43f * 2f),
        style = stroke
    )
    drawArc(
        brush = brushes.innerSweepGradient,
        startAngle = 1.5f,
        sweepAngle = hours,
        useCenter = false,
        topLeft = Offset(64f * 2f, 64f * 2f),
        size = Size(size.width * 0.36f * 2f, size.width * 0.36f * 2f),
        style = stroke
    )
}

@Composable
private fun TimerPlaybackControls(
    modifier: Modifier = Modifier,
    state: TimeState,
    onPlayPause: () -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(modifier = modifier, onClick = onPlayPause) {
                if (!state.isPlaying) {
                    PlaybackControlIcon(
                        imageVector = Icons.Default.PlayArrow,
                        tint = lightPurpleBlue
                    )
                } else {
                    PlaybackControlIcon(
                        imageVector = Icons.Default.Pause,
                        tint = lightPurpleBlue
                    )
                }
            }
            Spacer(Modifier.fillMaxWidth(0.14f))
            IconButton(modifier = modifier, onClick = onReset) {
                PlaybackControlIcon(
                    imageVector = Icons.Default.Refresh,
                    tint = lightPurpleBlue
                )
            }
        }
    }
}

private fun Vibrator.vibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrate(500)
    }
}

@Composable
private fun animatedTimeAngleAsState(angle: Float): State<Float> {
    return animateFloatAsState(
        angle,
        tween(TickerIntervalMillis.toInt(), easing = LinearEasing)
    )
}

private fun Animatable<Float, AnimationVector1D>.stop(
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch { stop() }
}

private fun Animatable<Float, AnimationVector1D>.animate(
    coroutineScope: CoroutineScope,
) {
    coroutineScope.launch {
        animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(990000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
}

@Composable
private fun vibrator() = ContextCompat.getSystemService(LocalContext.current, Vibrator::class.java)
