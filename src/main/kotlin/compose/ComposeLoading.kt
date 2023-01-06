package compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import res.ColorRes
import res.TextStyleRes


/// Loading控制器
class ComposeLoading {
    private var controller = mutableStateOf(false)
    private var loadingMessage = ""

    companion object {
        private val instance = ComposeLoading()

        fun show(message: String) {
            instance.loadingMessage = message
            instance.controller.value = true
        }

        fun hide() {
            instance.controller.value = false
        }

        fun defaultLoadingController() = instance
    }

    val isShowing
        get() = controller.value

    val message
        get() = loadingMessage
}

@Composable
fun LoadingContainer(
    controller: ComposeLoading = ComposeLoading.defaultLoadingController(),
    windowScope: WindowScope,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        content()

        if (controller.isShowing) {
            windowScope.WindowDraggableArea {
                Card(
                    elevation = 0.dp,
                    backgroundColor = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier,
                            content = {
                                CircularProgressIndicator()
                                Spacer(Modifier.padding(vertical = 6.dp))
                                Text(
                                    text = controller.message,
                                    style = TextStyleRes.bodyMedium.copy(color = ColorRes.white)
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}

