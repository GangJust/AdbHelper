package compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import res.ColorRes
import res.TextStyleRes

/// 该类是将默认的dialog弹层(Window窗口)封装, 组件方式参见: ComposeDialog.kt

/// Dialog控制器
class WindowDialogController(show: Boolean) {
    private val _dialogState = DialogState()
    private val dialogController = mutableStateOf(show)
    var bundle: Any? = null

    companion object {

    }

    fun show() {
        if (!dialogController.value) {
            dialogController.value = true
        }
    }

    fun hide() {
        if (dialogController.value) {
            dialogController.value = false
        }
    }

    val isShowing
        get() = dialogController.value

    val dialogState
        get() = _dialogState
}

@Composable
fun WindowDialogController.Companion.defaultController(show: Boolean = false): WindowDialogController =
    remember { WindowDialogController(show) }

@Composable
fun MessageWindowDialog(
    controller: WindowDialogController = WindowDialogController.defaultController(),
    title: String,
    message: String,
    radius: Dp = 12.dp,
    backgroundColor: Color = Color(0xffF8F8FF),
    cancelText: String = "取消",
    confirmText: String = "确定",
    onCancel: (WindowDialogController) -> Unit,
    onConfirm: (WindowDialogController) -> Unit,
) {
    WindowDialogContainer(controller = controller) {
        Card(
            elevation = 12.dp,
            shape = RoundedCornerShape(radius),
            border = BorderStroke(1.dp, ColorRes.icon.copy(alpha = 0.5f)),
            backgroundColor = backgroundColor,
            modifier = Modifier.padding(16.dp),
        ) {
            Column {
                Text(
                    text = title,
                    style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)
                )
                BasicTextField(
                    value = message,
                    onValueChange = { /*禁止修改*/ },
                    readOnly = true,
                    textStyle = TextStyleRes.bodyMedium.copy(lineHeight = 1.2.sp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CardButton(
                        onClick = { onCancel.invoke(controller) },
                        shape = RoundedCornerShape(bottomStart = radius),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        content = {
                            Text(
                                text = cancelText,
                                style = TextStyleRes.bodyMedium,
                            )
                        }
                    )
                    CardButton(
                        onClick = { onConfirm.invoke(controller) },
                        shape = RoundedCornerShape(bottomEnd = radius),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        content = {
                            Text(
                                text = confirmText,
                                style = TextStyleRes.bodyMediumSurface,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WindowDialogContainer(
    controller: WindowDialogController = WindowDialogController.defaultController(),
    content: @Composable () -> Unit,
) {
    if (controller.isShowing) {
        Dialog(
            onCloseRequest = { },
            state = controller.dialogState,
            undecorated = true,
            transparent = true,
            resizable = false,
            content = { this.WindowDraggableArea { content() } }
        )
    }
}