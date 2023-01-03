// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.view.HomeUI
import base.mvvm.*
import compose.ToastContainer
import res.ColorRes


class AppLogic : AbstractLogic() {
    override fun dispose() {
    }
}

class AppState : AbstractState<AppLogic>() {
    override fun createLogic() = AppLogic()
}

class App(private var application: ApplicationScope) : AbstractView<AppState>() {
    override fun createState() = AppState()

    @Composable
    override fun viewCompose() {
        val windowState = rememberWindowState(
            size = DpSize(800.dp, 600.dp),
            position = WindowPosition.Aligned(Alignment.Center),
        )

        Window(
            title = "AdbHelper",
            resizable = false,
            undecorated = true,
            transparent = true,
            onCloseRequest = {
                StateManager.clearStateMaps()
                application.exitApplication()
            },
            state = windowState,
        ) {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    primary = ColorRes.primary,
                    onSurface = ColorRes.onSurface,
                ),
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = 2.dp,
                    border = BorderStroke(1.dp, ColorRes.icon.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(12.dp),
                ) {
                    ToastContainer {
                        ViewCompose {
                            HomeUI(
                                application = application,
                                window = this,
                                windowState = windowState,
                            )
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    ViewCompose {
        App(this)
    }
}