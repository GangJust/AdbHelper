// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import state.AppState
import ui.AppUI
import ui.ErrorUI
import ui.SettingUI


fun main() = application {
    var exception: Exception? = null
    var appState: AppState? = null
    try {
        appState = AppState()
    } catch (e: Exception) {
        exception = e
        e.printStackTrace()
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = DpSize(560.dp, 700.dp), position = WindowPosition(Alignment.Center)),
        resizable = false,
        title = "AdbHelper",
        content = {
            if (appState != null) {
                var showSettingDialog by remember { mutableStateOf(!hasPath(appState)) }
                if (showSettingDialog) {
                    Dialog(
                        onCloseRequest = ::exitApplication,
                        resizable = false,
                        title = "请设置相关路径",
                        state = DialogState(width = 300.dp, height = 350.dp)
                    ) {
                        SettingUI(appState.settingState)
                        appState.settingState.onSaveCallback = {
                            showSettingDialog = !it
                        }
                    }
                } else {
                    AppUI(appState)
                }
            } else {
                ErrorUI(exception)
            }
        }
    )
}

fun hasPath(appState: AppState): Boolean {
    return !(appState.config.settingConfig.androidSdkPath.isEmpty() || appState.config.settingConfig.androidAdbPath.isEmpty())
}