package app.view.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import base.mvvm.IView
import base.mvvm.StateManager
import compose.VerScrollableContainer
import extensions.encodingTo
import res.ColorRes
import res.IconRes
import res.TextStyleRes
import java.nio.charset.Charset

// 错误页
class ErrorPage(
    application: ApplicationScope,
    windowScope: WindowScope,
    windowState: WindowState,
    e: Exception,
) : ScaffoldPage(
    application, windowScope,
    windowState,
    title = {
        Text(
            text = "AdbHelper",
            style = TextStyle(fontSize = 24.sp),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    },
    content = {
        VerScrollableContainer(scrollbarPadding = PaddingValues(end = 8.dp)) {
            BasicTextField(
                modifier = Modifier.padding(horizontal = 24.dp),
                value = e.stackTraceToString().encodingTo(Charset.defaultCharset()),
                onValueChange = {/*禁止修改*/ },
                textStyle = TextStyleRes.bodyMedium,
            )
        }
    }
)

// 脚手架页
open class ScaffoldPage(
    private val application: ApplicationScope,
    private val windowScope: WindowScope,
    private val windowState: WindowState,
    private val title: @Composable () -> Unit,
    private val content: @Composable () -> Unit,
) : IView {
    @Composable
    override fun viewCompose() {
        CustomScaffold(
            application = application,
            windowScope = windowScope,
            windowState = windowState,
            title = title,
            content = content,
        )
    }
}


// 自定义脚手架组件
@Composable
fun CustomScaffold(
    application: ApplicationScope,
    windowScope: WindowScope,
    windowState: WindowState,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        content = {
            Column(modifier = Modifier) {
                windowScope.WindowDraggableArea {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        title()
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                windowState.isMinimized = true
                            },
                            content = {
                                Icon(
                                    painter = IconRes.windowMinimized,
                                    contentDescription = "minimized",
                                    tint = ColorRes.text,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        IconButton(
                            onClick = {
                                StateManager.clearStateMaps()
                                application.exitApplication()
                            },
                            content = {
                                Icon(
                                    painter = IconRes.windowClosed,
                                    contentDescription = "closed",
                                    tint = ColorRes.text,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                content()
            }
        },
    )
}