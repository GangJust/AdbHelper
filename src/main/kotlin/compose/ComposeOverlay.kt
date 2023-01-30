package compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier

/// 食用方法: 见 AppManagerPage->搜索 ComposeOverlayContainer
class ComposeOverlay : ComposeController {
    private var status = mutableStateOf(false)
    private var overlayView: (@Composable BoxScope.() -> Unit)? = null

    fun setView(view: @Composable BoxScope.() -> Unit): ComposeOverlay {
        overlayView = view
        return this
    }

    override fun show() {
        status.value = true
    }

    override fun hide() {
        status.value = false
    }

    override val isShowing: Boolean
        get() = status.value

    val view: @Composable BoxScope.() -> Unit
        get() = overlayView!!
}

/// 悬浮层容器
@Composable
fun ComposeOverlayContainer(
    controller: ComposeOverlay,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        content = {
            content()

            if (controller.isShowing) {
                controller.view(this@Box)
            }
        }
    )
}