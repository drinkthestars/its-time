package com.example.androiddevchallenge.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.gray
import kotlin.math.absoluteValue

@Composable
fun PlaybackControlIcon(imageVector: ImageVector, tint: Color) {
    Icon(
        modifier = Modifier.size(50.dp),
        imageVector = imageVector,
        contentDescription = null,
        tint = tint
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimeChooser(
    controlsSize: Dp = 20.dp,
    controlsColor: Color = gray,
    showControls: Boolean,
    onChange: (TimeState.Change) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.wrapContentWidth().height(150.dp)
            .padding(vertical = 12.dp)
            .animateContentSize(
                tween(
                    durationMillis = TickerStartDelayMillis.toInt(),
                    easing = FastOutSlowInEasing
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(showControls) {
            IconButton(
                modifier = Modifier.size(controlsSize),
                onClick = { onChange(TimeState.Change.Increase) }
            ) {
                Icon(
                    modifier = Modifier.size(controlsSize),
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = null,
                    tint = controlsColor
                )
            }
        }
        content()
        AnimatedVisibility(showControls) {
            IconButton(
                modifier = Modifier.size(controlsSize),
                onClick = { onChange(TimeState.Change.Decrease) }
            ) {
                Icon(
                    modifier = Modifier.size(controlsSize),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = controlsColor
                )
            }
        }
    }
}

@Composable
fun TimeSeparator() {
    Text(
        textAlign = TextAlign.Center,
        text = ":",
        style = MaterialTheme.typography.h4,
        fontWeight = FontWeight.Light,
        letterSpacing = 2.sp
    )
}

@Composable
fun TimeText(text: Int, dragEnabled: Boolean, onChange: (TimeState.Change) -> Unit) {
    Text(
        textAlign = TextAlign.Center,
        text = "$text",
        style = MaterialTheme.typography.h3,
        fontWeight = FontWeight.Light,
        letterSpacing = 2.sp,
        modifier = Modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    println("dragAmount = $dragAmount")
                    if (dragAmount.absoluteValue >= 2) {
                        if (dragEnabled) {
                            onChange(
                                when {
                                    dragAmount < 0 -> TimeState.Change.Increase
                                    else -> TimeState.Change.Decrease
                                }
                            )
                            change.consumePositionChange()
                        }
                    }
                }
            }
    )
}
