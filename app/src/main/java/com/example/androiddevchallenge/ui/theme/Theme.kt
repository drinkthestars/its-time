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
package com.example.androiddevchallenge.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorPalette = darkColors(
    background = darkNavy,
    primary = darkNavy,
    primaryVariant = outerRingLight,
    secondary = darkPurple,
    onPrimary = Color.White,
    onSecondary = Color.White,
)

@Composable
fun MyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = DarkColorPalette
    val view = LocalView.current
    val window = (LocalContext.current as Activity).window
    window.statusBarColor = colors.primary.value.toInt()
    window.navigationBarColor = colors.primary.value.toInt()

    val insetsController = remember(view, window) {
        WindowCompat.getInsetsController(window, view)
    }

    insetsController?.run {
        isAppearanceLightNavigationBars = !darkTheme
        isAppearanceLightStatusBars = !darkTheme
    }
    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
