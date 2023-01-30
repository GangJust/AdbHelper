package compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import res.TextStyleRes

@Composable
fun MessageDialogView(
    modifier: Modifier = Modifier,
    title: String = "Title",
    message: String = "This is a Message!",
    cancelText: String = "Cancel",
    confirmText: String = "Confirm",
    onlyConfirm: Boolean = false,
    cancelCallback: (() -> Unit)? = null,
    confirmCallback: (() -> Unit)? = null,
) {
    val radius = 12.dp
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                content = {
                    Text(
                        text = title,
                        style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    )
                    BasicTextField(
                        value = message,
                        readOnly = true,
                        textStyle = TextStyleRes.bodyMedium,
                        onValueChange = {/* 禁止修改 */ },
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)
                    )

                    Row(
                        modifier = Modifier,
                        content = {
                            if (!onlyConfirm) {
                                CardButton(
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(bottomStart = radius),
                                    onClick = {
                                        cancelCallback?.invoke()
                                    },
                                    content = {
                                        Text(
                                            text = cancelText,
                                            style = TextStyleRes.bodyMedium,
                                        )
                                    },
                                )
                            }

                            CardButton(
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(bottomStart = if (onlyConfirm) radius else 0.dp, bottomEnd = radius),
                                onClick = {
                                    confirmCallback?.invoke()
                                },
                                content = {
                                    Text(
                                        text = confirmText,
                                        style = TextStyleRes.bodyMediumSurface,
                                    )
                                },
                            )
                        }
                    )
                }
            )
        }
    )
}

//全局通用的Dialog
class ComposeDialog private constructor() : ComposeController {
    private var controller = mutableStateOf(false)
    private var dialogView: (@Composable BoxWithConstraintsScope.() -> Unit)? = null

    companion object {
        private val instance = ComposeDialog()

        fun hide() {
            instance.hide()
        }

        fun setView(view: @Composable BoxWithConstraintsScope.() -> Unit): ComposeDialog {
            instance.dialogView = view
            return instance
        }

        fun defaultDialogController() = instance
    }

    override fun show() {
        if (dialogView == null) throw RuntimeException("dialogView == null")
        if (!controller.value) controller.value = true
    }

    override fun hide() {
        controller.value = false
    }

    override val isShowing: Boolean
        get() = controller.value

    val view: @Composable BoxWithConstraintsScope.() -> Unit
        get() = dialogView!!
}

@Composable
fun ComposeDialogContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val controller = ComposeDialog.defaultDialogController()
    Box(
        modifier = modifier,
        content = {
            //主要内容
            content()

            //外部暗色背景
            AnimatedVisibility(
                visible = controller.isShowing,
                enter = fadeIn(),
                exit = fadeOut(),
                content = {
                    Surface(
                        elevation = 0.dp,
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            //内部dialog内容
                            //自适应内容大小
                            BoxWithConstraints(
                                modifier = Modifier.align(Alignment.Center),
                                content = controller.view
                            )
                        }
                    )
                }
            )
        }
    )
}
