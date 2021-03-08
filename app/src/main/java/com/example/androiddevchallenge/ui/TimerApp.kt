package com.example.androiddevchallenge.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import com.example.androiddevchallenge.ui.theme.green
import com.example.androiddevchallenge.ui.theme.innerRingDark
import com.example.androiddevchallenge.ui.theme.innerRingLight
import com.example.androiddevchallenge.ui.theme.lightNavy
import com.example.androiddevchallenge.ui.theme.midRingDark
import com.example.androiddevchallenge.ui.theme.midRingLight
import com.example.androiddevchallenge.ui.theme.orange
import com.example.androiddevchallenge.ui.theme.outerRingDark
import com.example.androiddevchallenge.ui.theme.outerRingLight
import com.example.androiddevchallenge.ui.theme.red

internal const val TickerIntervalMillis = 1000L
internal const val TickerStartDelayMillis = 400L

@Composable
fun TimerApp() {
    val vibrator = ContextCompat.getSystemService(LocalContext.current, Vibrator::class.java)
    Surface(color = MaterialTheme.colors.background) {
        val coroutineScope = rememberCoroutineScope()
        val state = remember {
            TimeState(coroutineScope, onCountDownComplete = { vibrator?.vibrate() })
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Timer(modifier = Modifier.fillMaxWidth(0.83f), state)
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
private fun Timer(modifier: Modifier = Modifier, state: TimeState) {
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
            showControls = !state.isPlaying,
            onChange = { state.changeHours(it) }
        ) {
            TimeText(state.hoursLeft, dragEnabled = !state.isPlaying) { state.changeHours(it) }
        }
        Spacer(Modifier.width(8.dp))
        TimeSeparator()
        Spacer(Modifier.width(8.dp))
        TimeChooser(
            showControls = !state.isPlaying,
            onChange = { state.changeMinutes(it) }
        ) {
            TimeText(
                state.minutesLeft,
                dragEnabled = !state.isPlaying
            ) { state.changeMinutes(it) }
        }
        Spacer(Modifier.width(8.dp))
        TimeSeparator()
        Spacer(Modifier.width(8.dp))
        TimeChooser(
            showControls = !state.isPlaying,
            onChange = { state.changeSeconds(it) }
        ) {
            TimeText(
                state.secondsLeft,
                dragEnabled = !state.isPlaying
            ) { state.changeSeconds(it) }
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
    val hours by animateFloatAsState(
        state.hoursAngle,
        tween(TickerIntervalMillis.toInt(), easing = LinearEasing)
    )
    val minutes by animateFloatAsState(
        state.minutesAngle,
        tween(TickerIntervalMillis.toInt(), easing = LinearEasing)
    )
    val seconds by animateFloatAsState(
        state.secondsAngle,
        tween(TickerIntervalMillis.toInt(), easing = LinearEasing)
    )
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
//    drawCircle(
//        brush = SolidColor(Color.White.copy(alpha = 0.2f)),
//        alpha = 0.3f,
//        style = Stroke(width = 10f)
//    )
    drawArc(
        brush = brushes.outerSweepGradient,
        startAngle = 1.5f,
        sweepAngle = seconds,
        useCenter = false,
        style = stroke
    )
//    drawCircle(
//        brush = SolidColor(Color.White.copy(alpha = 0.2f)),
//        radius = size.width * 0.43f,
//        alpha = 0.3f,
//        style = Stroke(width = 10f)
//    )
    drawArc(
        brush = brushes.midSweepGradient,
        startAngle = 1.5f,
        sweepAngle = minutes,
        useCenter = false,
        topLeft = Offset(64f, 64f),
        size = Size(size.width * 0.43f * 2f, size.width * 0.43f * 2f),
        style = stroke
    )
//    drawCircle(
//        brush = SolidColor(Color.White.copy(alpha = 0.2f)),
//        radius = size.width * 0.36f,
//        alpha = 0.3f,
//        style = Stroke(width = 10f)
//    )
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
                        tint = green
                    )
                } else {
                    PlaybackControlIcon(
                        imageVector = Icons.Default.Pause,
                        tint = orange
                    )
                }
            }
            Spacer(Modifier.fillMaxWidth(0.14f))
            IconButton(modifier = modifier, onClick = onReset) {
                PlaybackControlIcon(
                    imageVector = Icons.Default.Refresh,
                    tint = red
                )
            }
        }
    }
}

private fun Vibrator.vibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    } else {
        //deprecated in API 26
        vibrate(500);
    }
}

class TimeCircleBrushes {
    val outerSweepGradient = Brush.sweepGradient(listOf(outerRingDark, outerRingLight))
    val midSweepGradient = Brush.sweepGradient(listOf(midRingDark, midRingLight))
    val innerSweepGradient = Brush.sweepGradient(listOf(innerRingDark, innerRingLight))

//    val outerRadialGradient = Brush.radialGradient(listOf(outerRingLighter, outerRingLight))
//    val midRadialGradient = Brush.radialGradient(listOf(midRingLighter, midRingLight))
//    val innerRadialGradient = Brush.radialGradient(listOf(innerRingLighter, innerRingLight))
}
