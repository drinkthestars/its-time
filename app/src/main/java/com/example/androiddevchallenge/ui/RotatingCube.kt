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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

private const val CamDistance = 1.7f

@Composable
fun RotatingCube(
    cubeState: CubeState,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Float = 100f,
) {
    val cubePoints = remember { cubePoints(scaleFactor) }
    Canvas(modifier = modifier) {
        cubePoints.map {
            val rotatedX = multiply(
                a = rotationX(cubeState.animatedCubeAngle.value),
                b = it,
                r1 = 3,
                c1 = 3,
                c2 = 1
            )
            val rotatedXY = multiply(
                a = rotationY(cubeState.animatedCubeAngle.value),
                b = rotatedX,
                r1 = 3,
                c1 = 3,
                c2 = 1
            )
            val rotatedXYZ = multiply(
                a = rotationZ(cubeState.animatedCubeAngle.value),
                b = rotatedXY,
                r1 = 3,
                c1 = 3,
                c2 = 1
            )
//            val skewFactor = (CamDistance - normalize(rotatedXYZ.last(), max = 2.8f, min = -2.8f))
            val projected2D = multiply(
                a = skewed2DProjection(),
                b = rotatedXYZ,
                r1 = 2,
                c1 = 3,
                c2 = 1
            )
            Offset(projected2D[0] * size, projected2D[1] * size)
        }.also { offsets ->
            for (i in 0 until 4) {
                draw(
                    offsets,
                    start = i,
                    end = (i + 1) % 4,
                    color,
                    cubeState.animatedCubeAlpha.value
                )
                draw(
                    offsets,
                    start = i + 4,
                    end = ((i + 1) % 4) + 4,
                    color,
                    cubeState.animatedCubeAlpha.value
                )
                draw(offsets, start = i, end = i + 4, color, cubeState.animatedCubeAlpha.value)
            }
        }
    }
}

private fun cubePoints(scaleFactor: Float): Array<FloatArray> {
    val absScaleFactor = scaleFactor.absoluteValue
    return arrayOf(
        floatArrayOf(-absScaleFactor, -absScaleFactor, -absScaleFactor),
        floatArrayOf(absScaleFactor, -absScaleFactor, -absScaleFactor),
        floatArrayOf(absScaleFactor, absScaleFactor, -absScaleFactor),
        floatArrayOf(-absScaleFactor, absScaleFactor, -absScaleFactor),

        floatArrayOf(-absScaleFactor, -absScaleFactor, absScaleFactor),
        floatArrayOf(absScaleFactor, -absScaleFactor, absScaleFactor),
        floatArrayOf(absScaleFactor, absScaleFactor, absScaleFactor),
        floatArrayOf(-absScaleFactor, absScaleFactor, absScaleFactor)
    )
}

private fun skewed2DProjection(skew: Float = 1f) = arrayOf(
    floatArrayOf(skew, 0f, 0f),
    floatArrayOf(0f, skew, 0f)
)

private fun rotationX(angle: Float) = arrayOf(
    floatArrayOf(1f, 0f, 0f),
    floatArrayOf(0f, cos(angle), -sin(angle)),
    floatArrayOf(0f, sin(angle), cos(angle)),
)

private fun rotationY(angle: Float) = arrayOf(
    floatArrayOf(cos(angle), 0f, -sin(angle)),
    floatArrayOf(0f, 1f, 0f),
    floatArrayOf(sin(angle), 0f, cos(angle)),
)

fun rotationZ(angle: Float) = arrayOf(
    floatArrayOf(cos(angle), -sin(angle), 0f),
    floatArrayOf(sin(angle), cos(angle), 0f),
    floatArrayOf(0f, 0f, 1f),
)

private fun multiply(
    a: Array<FloatArray>,
    b: FloatArray,
    r1: Int = 3,
    c1: Int = 3,
    c2: Int = 1
): FloatArray {
    val product = FloatArray(r1) { 0f }
    for (i in 0 until r1) {
        for (j in 0 until c2) {
            for (k in 0 until c1) {
                product[i] += a[i][k] * b[k]
            }
        }
    }
    return product
}

private fun DrawScope.draw(
    offsets: List<Offset>,
    start: Int,
    end: Int,
    color: Color,
    alpha: Float
) {
    val startOffset = offsets[start]
    val endOffset = offsets[end]
    drawPoints(
        points = offsets,
        color = color,
        pointMode = PointMode.Points,
        strokeWidth = 15f,
        alpha = alpha,
        cap = StrokeCap.Round
    )
    drawLine(
        start = startOffset,
        end = endOffset,
        strokeWidth = 10f,
        color = color,
        alpha = alpha,
        cap = StrokeCap.Round
    )
}

@Composable
fun rememberCubeState(
    coroutineScope: CoroutineScope
): CubeState {
    return remember { CubeState(coroutineScope) }
}

@Stable
class CubeState(
    private val coroutineScope: CoroutineScope
) {
    val animatedCubeAngle = Animatable(10f)
    val animatedCubeAlpha = Animatable(0f)

    fun animate() {
        coroutineScope.launch {
            animatedCubeAlpha.animateTo(1f, tween(800, easing = LinearEasing))
        }
        coroutineScope.launch {
            animatedCubeAngle.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(998000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    fun stop() {
        coroutineScope.launch {
            animatedCubeAlpha.animateTo(0f, tween(700, easing = LinearEasing))
            stop()
        }
    }
}

private fun normalize(
    value: Float,
    max: Float,
    min: Float
): Float {
    return (value - min) / (max - min)
}
