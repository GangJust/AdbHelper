// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ui.mApp

fun main() = application {
    Window(
        state = WindowState(size = DpSize(580.dp, 620.dp), position = WindowPosition(Alignment.Center)),
        onCloseRequest = ::exitApplication,
        title = "AdbHelper",
        resizable = false,
        content = {
            mApp()
        },
    )
}
