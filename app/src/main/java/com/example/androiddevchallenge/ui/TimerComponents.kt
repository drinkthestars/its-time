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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.innerRingDark
import com.example.androiddevchallenge.ui.theme.innerRingLight
import com.example.androiddevchallenge.ui.theme.midRingDark
import com.example.androiddevchallenge.ui.theme.midRingLight
import com.example.androiddevchallenge.ui.theme.outerRingDark
import com.example.androiddevchallenge.ui.theme.outerRingLight
import com.example.androiddevchallenge.ui.theme.timeControls

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
    controlsColor: Color = timeControls,
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
fun TimeText(text: Int) {
    Text(
        textAlign = TextAlign.Center,
        text = "$text",
        style = MaterialTheme.typography.h3,
        fontWeight = FontWeight.Light,
        letterSpacing = 2.sp
    )
}

class TimeCircleBrushes {
    val outerSweepGradient = Brush.sweepGradient(listOf(outerRingLight, outerRingDark))
    val midSweepGradient = Brush.sweepGradient(listOf(midRingLight, midRingDark))
    val innerSweepGradient = Brush.sweepGradient(listOf(innerRingLight, innerRingDark))
}
