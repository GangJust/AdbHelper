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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import base.mvvm.IView
import base.mvvm.ViewCompose
import res.TextStyleRes

class MessageComposeDialog : ComposeDialogController() {
    private var _title: String = "Title"
    private var _message: String = "This is a Message!"
    private var _cancelText: String = "Cancel"
    private var _confirmText: String = "Confirm"
    private var _cancelCallback: (() -> Unit)? = null
    private var _confirmCallback: (() -> Unit)? = null

    companion object {
        private val instance = MessageComposeDialog()

        fun setTitle(title: String): Companion {
            instance._title = title
            return this
        }

        fun setMessage(message: String): Companion {
            instance._message = message
            return this
        }

        fun setCancelText(cancelText: String): Companion {
            instance._cancelText = cancelText
            return this
        }

        fun setConfirmText(confirmText: String): Companion {
            instance._confirmText = confirmText
            return this
        }

        fun setCancelCallback(block: () -> Unit): Companion {
            instance._cancelCallback = block
            return this
        }

        fun setConfirmCallback(block: () -> Unit): Companion {
            instance._confirmCallback = block
            return this
        }

        fun show() = instance.show()

        fun defaultDialogController() = instance
    }

    val title: String
        get() = _title
    val message: String
        get() = _message
    val cancelText
        get() = _cancelText
    val confirmText: String
        get() = _confirmText
    val cancelCallback
        get() = _cancelCallback
    val confirmCallback
        get() = _confirmCallback
}

@Composable
fun MessageDialogContainer(
    controller: MessageComposeDialog = MessageComposeDialog.defaultDialogController(),
    content: @Composable () -> Unit
) {
    val radius = 12.dp
    ComposeDialogContainer(
        controller,
        dialogContent = {
            Card(
                modifier = Modifier.align(Alignment.Center).widthIn(max = 280.dp),
                shape = RoundedCornerShape(radius),
                content = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                        content = {
                            Text(
                                text = controller.title,
                                style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                            )
                            BasicTextField(
                                value = controller.message,
                                readOnly = true,
                                textStyle = TextStyleRes.bodyMedium,
                                onValueChange = {/* 禁止修改 */ },
                                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)
                            )
                            Row(
                                content = {
                                    CardButton(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(bottomStart = radius),
                                        onClick = {
                                            controller.hide()
                                            controller.cancelCallback?.invoke()
                                        },
                                        content = {
                                            Text(
                                                text = controller.cancelText,
                                                style = TextStyleRes.bodyMedium,
                                            )
                                        },
                                    )

                                    CardButton(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(bottomEnd = radius),
                                        onClick = {
                                            controller.hide()
                                            controller.confirmCallback?.invoke()
                                        },
                                        content = {
                                            Text(
                                                text = controller.confirmText,
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
        },
        content = content
    )
}


///
open class ComposeDialogController : AbsComposeController {
    private var controller = mutableStateOf(false)
    private var dialogView: ComposeDialogView? = null

    companion object {
        private val instance = ComposeDialogController()

        fun show() {
            instance.show()
        }

        fun hide() {
            instance.hide()
        }

        fun defaultDialogController() = instance
    }

    override fun show() {
        controller.value = true
    }

    override fun hide() {
        controller.value = false
    }

    override fun isShowing() = controller.value
}

open class ComposeDialogView(
    private var dialogView: @Composable (BoxWithConstraintsScope.() -> Unit)? = null,
) : IView {
    private var boxScope: BoxWithConstraintsScope? = null

    fun setBoxScope(scope: BoxWithConstraintsScope) {
        this.boxScope = scope
    }

    fun setDialogView(dialogView: @Composable BoxWithConstraintsScope.() -> Unit) {
        this.dialogView = dialogView
    }

    @Composable
    override fun viewCompose() {
        dialogView?.invoke(boxScope!!)
    }
}

@Composable
fun ComposeDialogContainer(
    controller: ComposeDialogController,
    dialogContent: @Composable BoxWithConstraintsScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    ComposeDialogContainer(
        controller = controller,
        composeDialogView = remember { ComposeDialogView(dialogContent) },
        content = content,
    )
}

/**
 * 可动态显示的dialogView, 需要持有[composeDialogView]实例对象
 *
 * 通过[composeDialogView#setDialogView] 对视图进行替换
 *
 * 须知, 背景暗色不会超过 [ComposeDialogContainer] 包裹的范围
 *
 * 最好将, [ComposeDialogContainer]放在顶层, 然后将 [composeDialogView] 持有传递
 *
 * ```
 * val composeDialogController = ComposeDialogController.defaultDialogController()
 * val composeDialogView = remember { ComposeDialogView() }
 * ComposeDialogContainer(controller = composeDialogController, composeDialogView = composeDialogView) {
 *   Row(
 *       horizontalArrangement = Arrangement.Center,
 *       verticalAlignment = Alignment.CenterVertically,
 *       modifier = Modifier.fillMaxSize()
 *     ) {
 *         Button(
 *           onClick = {
 *               composeDialogView.setDialogView {
 *                   Card(modifier = Modifier.align(Alignment.Center)) {
 *                       Column {
 *                         Text("你好, 我是动态的Dialog")
 *                         Button(onClick = { composeDialogController.hide() }) { Text("关闭") }
 *                       }
 *                   }
 *               }
 *               composeDialogController.show()
 *           }
 *        ) { Text(text = "显示Dialog") }
 *     }
 * }
 * ```
 */
@Composable
fun ComposeDialogContainer(
    controller: ComposeDialogController,
    composeDialogView: ComposeDialogView,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        content = {
            content()

            //外部暗色背景
            AnimatedVisibility(
                visible = controller.isShowing(),
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
                                content = {
                                    composeDialogView.setBoxScope(this) //必须先调用该方法, 设置 BoxWithConstraints 的作用域
                                    ViewCompose {
                                        composeDialogView
                                    }
                                }
                            )
                        }
                    )
                }
            )
        }
    )
}