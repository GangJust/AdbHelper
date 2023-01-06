package app.view.pages

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.model.MessageModel
import app.state.pages.PortForwardState
import base.mvvm.AbstractView
import kotlinx.coroutines.delay
import res.ColorRes
import res.TextStyleRes

class PortForwardPage : AbstractView<PortForwardState>() {
    override fun createState() = PortForwardState()

    init {
        state.launch {
            delay(100)
            state.sendMessage("命令列表")
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun viewCompose() {
        //滚动监听, 当 chatMessageList size 发生改变时, 会自动跳转到底部
        LaunchedEffect(state.chatMessageList.size) {
            if (state.chatMessageList.isEmpty()) return@LaunchedEffect
            state.chatMessageLazyListState.animateScrollToItem(state.chatMessageList.size - 1)
        }

        Scaffold(
            backgroundColor = ColorRes.transparent,
            contentColor = ColorRes.transparent,
            isFloatingActionButtonDocked = true,
            bottomBar = {
                BottomAppBar(
                    backgroundColor = ColorRes.white,
                    contentColor = ColorRes.transparent,
                    modifier = Modifier.padding(start = 1.dp),
                    content = {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).weight(1f),
                            content = {
                                BasicTextField(
                                    value = state.messageValue,
                                    onValueChange = {
                                        state.messageValue = it
                                    },
                                    singleLine = true,
                                    textStyle = TextStyleRes.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .onPreviewKeyEvent {
                                            if (it.key == Key.Enter) {
                                                return@onPreviewKeyEvent onSenderMessage()
                                            }
                                            false
                                        },
                                )
                                if (state.messageHint.value) {
                                    Text(
                                        text = "请输入被转发的端口",
                                        style = TextStyleRes.bodyMedium.copy(color = ColorRes.secondaryText),
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                            }
                        )
                        Button(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            onClick = { onSenderMessage() },
                            content = {
                                Text(
                                    text = "发送",
                                    style = TextStyleRes.bodyMedium.copy(ColorRes.white),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        )
                    }
                )
            },
            content = {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(
                        primary = ColorRes.text, //重新设置主题, 避免文本选择颜色被覆盖, 无法辨别
                        onSurface = ColorRes.onSurface,
                    ),
                    content = {
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 64.dp),
                            content = {
                                LazyColumn(
                                    state = state.chatMessageLazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(state.chatMessageList.size) {
                                        val model = state.chatMessageList[it]
                                        ChatMessageItem(model)
                                    }
                                }

                                VerticalScrollbar(
                                    adapter = ScrollbarAdapter(state.chatMessageLazyListState),
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        )
                    }
                )

            }
        )
    }

    @Composable
    private fun ChatMessageItem(model: MessageModel) {
        val radius = 12.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {

            //如果是用户消息, 扩展到最右边
            if (model.msgType == 1) {
                Spacer(Modifier.weight(1f))
            }

            Card(
                shape = RoundedCornerShape(
                    bottomEnd = radius,
                    bottomStart = radius,
                    topEnd = if (model.msgType == 0) radius else 0.dp,  //系统消息圆角
                    topStart = if (model.msgType == 1) radius else 0.dp,  //用户消息圆角
                ),
                elevation = 1.dp,
                backgroundColor = if (model.msgType == 0) ColorRes.messageSystem else ColorRes.messageUser,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                content = {
                    SelectionContainer(modifier = Modifier) {
                        Text(
                            text = model.message,
                            style = TextStyleRes.bodyMedium.copy(lineHeight = 1.5.em),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            )

            //如果是系统消息, 扩展到最左边
            if (model.msgType == 0) {
                Spacer(Modifier.weight(1f))
            }
        }
    }

    private fun onSenderMessage(): Boolean {
        state.sendMessage(state.messageValue)
        return true
    }
}