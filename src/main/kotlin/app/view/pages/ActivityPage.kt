package app.view.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.state.pages.ActivityState
import base.mvvm.AbstractView
import res.ColorRes
import res.TextStyleRes

/// 活动信息
class ActivityPage : AbstractView<ActivityState>() {
    override fun createState() = ActivityState()

    @Composable
    override fun viewCompose() {
        Scaffold(
            backgroundColor = ColorRes.transparent,
            contentColor = ColorRes.transparent,
            isFloatingActionButtonDocked = true,
            floatingActionButton = {
                FloatingActionButton(
                    contentColor = Color.White,
                    backgroundColor = ColorRes.primary,
                    onClick = {
                        state.loadActivity()
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                )
            },
            bottomBar = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().height(56.dp), //底部占位, 统一高度
                    content = {
                        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                            SwitchItem(
                                title = "全类名显示",
                                checked = state.fullClassName.value,
                                onCheckedChange = {
                                    state.fullClassName.value = !state.fullClassName.value
                                    state.loadActivity()
                                }
                            )
                            SwitchItem(
                                title = "单行显示",
                                checked = state.singleTextLine.value,
                                onCheckedChange = {
                                    state.singleTextLine.value = !state.singleTextLine.value
                                }
                            )
                        }
                    },
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                SingleHistoryItem(
                    title = "当前包名:",
                    singleLine = state.singleTextLine.value,
                    value = state.packageName.value,
                )
                SingleHistoryItem(
                    title = "当前进程:",
                    singleLine = state.singleTextLine.value,
                    value = state.processName.value,
                )
                SingleHistoryItem(
                    title = "启动活动:",
                    singleLine = state.singleTextLine.value,
                    value = state.launchActivity.value,
                )
                SingleHistoryItem(
                    title = "前台活动:",
                    singleLine = state.singleTextLine.value,
                    value = state.resumedActivity.value,
                )
                SingleHistoryItem(
                    title = "上次活动:",
                    singleLine = state.singleTextLine.value,
                    value = state.lastPausedActivity.value,
                )
                MultipleHistoryItem(
                    title = "活动堆栈:",
                    singleLine = state.singleTextLine.value,
                    values = state.stackActivities.toTypedArray().reversedArray(),
                )
            }
        }
    }

    //开关
    @Composable
    private fun SwitchItem(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = ColorRes.onSurface,
                    checkedThumbColor = ColorRes.primary,
                    uncheckedTrackColor = Color.Gray,
                ),
            )
        }
    }

    //单条
    @Composable
    private fun SingleHistoryItem(
        title: String,
        singleLine: Boolean = false,
        value: String,
    ) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = title,
                    style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                BasicTextField(
                    value = value,
                    readOnly = true,
                    singleLine = singleLine,
                    onValueChange = { /* BasicTextField 可以响应 Ctrl+A 组合按键, 这里禁止编辑 */ },
                    textStyle = TextStyleRes.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }

    //多条
    @Composable
    private fun MultipleHistoryItem(
        title: String,
        singleLine: Boolean = false,
        vararg values: String,
    ) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = title,
                    style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                LazyColumn {
                    items(values.size) {
                        BasicTextField(
                            value = values[it],
                            readOnly = true,
                            singleLine = singleLine,
                            onValueChange = { /* BasicTextField 可以响应 Ctrl+A 组合按键, 这里禁止编辑 */ },
                            textStyle = TextStyleRes.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}