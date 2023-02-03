package app.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import app.state.HomeState
import app.view.pages.*
import base.mvvm.AbstractView
import base.mvvm.ViewCompose
import compose.CardButton
import compose.ComposeDialog
import compose.ComposeToast
import compose.CustomTextField
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
            elevation = 4.dp,
            modifier = Modifier.width(state.leftPaneWidth.value).padding(end = 1.dp), //预留出左侧1dp的阴影
            content = {
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
                                ComposeDialog
                                    .setView { DialogContent() }
                                    .show()
                            },
                        content = {
                            if (state.devicesList.isEmpty()) {
                                Text(
                                    text = "请选择ADB设备",
                                    style = TextStyleRes.bodyMedium,
                                    modifier = Modifier
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                        .align(Alignment.Center),
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                        .align(Alignment.Center),
                                    content = {
                                        Icon(
                                            painterResource("icon/ic_phone.svg"),
                                            tint = ColorRes.icon,
                                            contentDescription = "Device",
                                            modifier = Modifier.padding(horizontal = 8.dp).size(18.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = state.currentDevice.value,
                                                style = TextStyleRes.bodyMedium,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier,
                                            )
                                            Text(
                                                text = "(${state.phoneAbiMap[state.currentDevice.value]})",
                                                style = TextStyleRes.bodyMedium,
                                                modifier = Modifier,
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        )
    }

    /// 右侧
    @Composable
    private fun RightContent() {
        CustomScaffold(
            application = application,
            windowScope = windowScope,
            windowState = windowState,
            title = {},
            content = {
                //主要视图, 统一加载, 选择切换
                val activityHistoryPage = ActivityPage()
                val appManagerPage = AppManagerPage()
                val portForwardPage = PortForwardPage()
                val viewLayoutPage = ViewLayoutPage()
                val unknownPage = UnknownPage()

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    ViewCompose {
                        when (state.leftMenuSelectIndex.value) {
                            0 -> activityHistoryPage
                            1 -> appManagerPage
                            2 -> portForwardPage
                            3 -> viewLayoutPage
                            else -> unknownPage
                        }
                    }
                }
            }
        )
    }


    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun BoxWithConstraintsScope.DialogContent() {

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .width(480.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            shape = RoundedCornerShape(8.dp),
            content = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.showConnectWifiDeviceView.value) "ADB WIFI" else "在线设备",
                            style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { state.showConnectWifiDeviceView.value = !state.showConnectWifiDeviceView.value },
                            content = {
                                Icon(
                                    painter = IconRes.wifi,
                                    contentDescription = "wifi connect",
                                    tint = ColorRes.icon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        AnimatedVisibility(
                            visible = !state.showConnectWifiDeviceView.value,
                            content = {
                                IconButton(
                                    onClick = { state.loadDevices() },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "refresh",
                                            tint = ColorRes.icon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            },
                        )
                        IconButton(
                            onClick = { ComposeDialog.hide() },
                            content = {
                                Icon(
                                    painter = IconRes.windowClosed,
                                    contentDescription = "closed",
                                    tint = ColorRes.icon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (state.showConnectWifiDeviceView.value) {
                        DialogContentConnectWifiDevice()
                    } else {
                        DialogContentDevicesList()
                    }
                }
            },
        )
    }

    ///设备列表
    @Composable
    private fun DialogContentDevicesList() {
        Column {
            if (state.devicesList.isEmpty()) {
                Text(
                    text = "没有ADB设备",
                    style = TextStyleRes.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            LazyColumn {
                items(state.devicesList.size) {
                    val device = state.devicesList[it]
                    CardButton(
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            state.currentDevice.value = device
                            ComposeDialog.hide()
                        },
                        content = {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = {
                                    Icon(
                                        painter = IconRes.phone,
                                        tint = ColorRes.icon,
                                        contentDescription = "Device",
                                        modifier = Modifier.padding(horizontal = 8.dp).size(18.dp)
                                    )
                                    Text(
                                        text = "${state.phoneBrandMap[device]} ",
                                        style = TextStyleRes.bodyMedium/*.copy(fontWeight = FontWeight.Bold)*/,
                                        softWrap = false,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                    Text(
                                        text = "$device (${state.phoneAbiMap[device]})",
                                        style = TextStyleRes.bodyMedium,
                                        softWrap = false,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }


    ///增加wifi连接设备
    @Composable
    private fun DialogContentConnectWifiDevice() {
        val textValue = mutableStateOf("")

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            content = {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = textValue,
                    onValueChange = { textValue.value = it },
                    hintText = "请输入ip地址:端口号",
                    fillMaxWidth = true,
                    textStyle = TextStyleRes.bodyMedium,
                )
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    CardButton(
                        onClick = {
                            if (textValue.value.isBlank()) return@CardButton
                            if (!textValue.value.contains("^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?):([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])\$".toRegex())) {
                                ComposeToast.show("ip:port错误!")
                                return@CardButton
                            }
                            state.wifiConnect(textValue.value) {
                                if (it) {
                                    state.loadDevices()
                                    state.showConnectWifiDeviceView.value = false
                                    ComposeToast.show("连接成功!")
                                } else {
                                    ComposeToast.show("连接失败，请检查ip:port!")
                                }
                            }
                        },
                        content = {
                            Text(
                                text = "连接",
                                style = TextStyleRes.bodyMediumSurface,
                            )
                        }
                    )
                }
            }
        )
    }
}