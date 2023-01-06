package app.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import app.state.HomeState
import app.view.pages.ActivityPage
import app.view.pages.AppManagerPage
import app.view.pages.PortForwardPage
import app.view.pages.UnknownPage
import base.mvvm.AbstractView
import base.mvvm.StateManager
import base.mvvm.ViewCompose
import kotlinx.coroutines.*
import res.ColorRes
import res.IconRes
import res.TextStyleRes

class HomeUI(
    private val application: ApplicationScope,
    private val windowScope: WindowScope,
    private val windowState: WindowState,
) : AbstractView<HomeState>() {
    override fun createState(): HomeState = HomeState()

    @Composable
    override fun viewCompose() {
        //初始化
        LaunchedEffect("initDevices") { state.loadDevices() }

        Row(modifier = Modifier.fillMaxSize()) {
            LeftContent()
            RightContent()
        }
    }

    /// 左侧
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun LeftContent() {
        Card(
            elevation = 2.dp,
            modifier = Modifier.width(state.leftPaneWidth.value)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //头部Logo
                windowScope.WindowDraggableArea {
                    BoxWithConstraints {
                        Text(
                            text = "ADB Helper",
                            style = TextStyle(color = ColorRes.text, fontSize = 24.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 24.dp)
                        )
                    }
                }

                //菜单
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        items(state.leftMenuTitleList.size) {
                            /// 写都写了, 删了可惜了
                            /*Surface(
                                color = if (state.leftMenuSelectIndex.value == it) ColorRes.onSurface else ColorRes.transparent,
                                contentColor = if (state.leftMenuSelectIndex.value == it) ColorRes.onSurface else ColorRes.transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(8.dp),
                                onClick = { state.leftMenuSelectIndex.value = it },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) {
                                    Icon(
                                        painter = state.leftMenuIconList[it],
                                        contentDescription = state.leftMenuTitleList[it],
                                        tint = if (state.leftMenuSelectIndex.value == it) ColorRes.primary else ColorRes.text,
                                        modifier = Modifier.padding(horizontal = 16.dp).size(24.dp),
                                    )
                                    Text(
                                        text = state.leftMenuTitleList[it],
                                        style = if (state.leftMenuSelectIndex.value == it) TextStyleRes.bodyMediumSurface else TextStyleRes.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }*/

                            TextButton(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(8.dp).heightIn(min = 48.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (state.leftMenuSelectIndex.value == it) ColorRes.onSurface else ColorRes.transparent,
                                    backgroundColor = if (state.leftMenuSelectIndex.value == it) ColorRes.onSurface else ColorRes.transparent,
                                ),
                                onClick = { state.leftMenuSelectIndex.value = it },
                            ) {
                                Icon(
                                    painter = state.leftMenuIconList[it],
                                    contentDescription = state.leftMenuTitleList[it],
                                    tint = if (state.leftMenuSelectIndex.value == it) ColorRes.primary else ColorRes.text,
                                    modifier = Modifier.padding(horizontal = 12.dp).size(24.dp),
                                )
                                Text(
                                    text = state.leftMenuTitleList[it],
                                    style = if (state.leftMenuSelectIndex.value == it) TextStyleRes.bodyMediumSurface else TextStyleRes.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                //底部ADB设备按钮
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            state.launch {
                                withContext(Dispatchers.IO) { state.loadDevices() }
                                state.showDropdownMenu.value = true
                            }
                        }
                ) {
                    //弹出设备菜单选项
                    DropdownMenu(
                        expanded = state.showDropdownMenu.value,
                        onDismissRequest = {
                            state.showDropdownMenu.value = false
                        },
                        modifier = Modifier.width(state.leftPaneWidth.value)
                    ) {
                        for (device in state.devicesList) {
                            DropdownMenuItem(
                                onClick = {
                                    state.currentDevice.value = device
                                    state.showDropdownMenu.value = false
                                },
                            ) {
                                Icon(
                                    IconRes.phone,
                                    tint = ColorRes.text,
                                    contentDescription = "Device",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$device (${state.phoneBrandMap[device] ?: "unknown"})",
                                    style = TextStyleRes.bodyMedium,
                                    softWrap = false,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }

                    //当前设备
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            painterResource("icon/ic_phone.svg"),
                            tint = ColorRes.text,
                            contentDescription = "Device",
                            modifier = Modifier.padding(horizontal = 8.dp).size(18.dp)
                        )
                        Text(
                            text = "${state.currentDevice.value} (${state.phoneBrandMap[state.currentDevice.value] ?: "unknown"})",
                            style = TextStyleRes.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    /// 右侧
    @Composable
    private fun RightContent() {
        Column {
            //顶部操作条
            windowScope.WindowDraggableArea {
                BoxWithConstraints {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                windowState.isMinimized = true
                            }
                        ) {
                            Icon(
                                painter = IconRes.windowMinimized,
                                contentDescription = "minimized",
                                tint = ColorRes.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                StateManager.clearStateMaps()
                                application.exitApplication()
                            }
                        ) {
                            Icon(
                                painter = IconRes.windowClosed,
                                contentDescription = "closed",
                                tint = ColorRes.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            //主要内容
            val activityHistoryPage = ActivityPage()
            val appManagerPage = AppManagerPage()
            val portForwardPage = PortForwardPage()
            val unknownPage = UnknownPage(message = "哎呀, 正在码不停蹄中..")

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                ViewCompose {
                    when (state.leftMenuSelectIndex.value) {
                        0 -> activityHistoryPage
                        1 -> appManagerPage
                        2 -> portForwardPage
                        else -> unknownPage
                    }
                }
            }
        }
    }
}