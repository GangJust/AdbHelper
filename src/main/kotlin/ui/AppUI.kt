package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.launch
import state.AppState
import utils.PlatformUtils
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.util.*

@Composable
fun AppUI(state: AppState) {
    Box {
        Scaffold(
            content = { HomeUI(state.homeState) },
            floatingActionButton = { FloatButtonPart(state) },
        )
    }
}

@Composable  //底部悬浮按钮组
fun FloatButtonPart(state: AppState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(start = 36.dp) //左侧未预留间距, 故手动设置
    ) {
        DevicesPart(state)
        Spacer(Modifier.weight(1f))
        RefreshPart(state)
        MenuPart(state)
    }
}

@Composable  //设备选项按钮
fun DevicesPart(state: AppState) {
    var devices by remember { mutableStateOf(state.getDevices()) } //获取设备列表
    var expanded by remember { mutableStateOf(false) }

    Box {
        ExtendedFloatingActionButton(
            onClick = {
                devices = state.getDevices() //扩展菜单显示, 重新获取设备列表
                expanded = true
            },
            backgroundColor = state.config.themeConfig.devicesButtonColor,
            contentColor = Color.White,
            text = {
                Text(
                    text = state.currentDevice.value,
                    modifier = Modifier.widthIn(max = 200.dp),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            icon = {
                if (expanded) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "more",
                    )
                } else {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "more",
                    )
                }
            }
        )

        //设备选择弹窗
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            focusable = true,
            offset = DpOffset(0.dp, 10.dp),
            modifier = Modifier.widthIn(min = 120.dp),
        ) {
            for (device in devices) {
                DropdownMenuItem(onClick = {
                    state.currentDevice.value = device //替换当前选中的设备
                    expanded = false
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource("icon/phone.svg"),
                            contentDescription = device,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = device,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable  //刷新按钮
fun RefreshPart(state: AppState) {
    FloatingActionButton(
        onClick = {
            state.homeState.reloadDetail()
            PlatformUtils.getPlatform()
        },
        backgroundColor = state.config.themeConfig.refreshButtonColor,
    ) {
        Icon(Icons.Default.Refresh, "刷新", tint = Color.White)
    }
}

@Composable  //菜单按钮
fun MenuPart(state: AppState) {
    //是否显示扩展菜单
    var expend by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        //右侧平移动画
        AnimatedVisibility(
            visible = expend,
            enter = slideInHorizontally { fullWidth -> fullWidth },
            exit = slideOutHorizontally { fullWidth -> fullWidth }
        ) {
            MenuExpendPart(state)
        }

        FloatingActionButton(
            onClick = { expend = !expend },
            backgroundColor = state.config.themeConfig.menuButtonColor,
        ) {
            Icon(Icons.Default.Menu, "菜单", tint = Color.White)
        }
    }
}

@Composable //扩展菜单-按钮组
fun MenuExpendPart(state: AppState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        //setting
        var showSettingDialog by remember { mutableStateOf(false) }
        FloatingActionButton(
            onClick = { showSettingDialog = true },
            backgroundColor = state.config.themeConfig.menuButtonColor,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "setting",
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )

            //设置Dialog
            if (showSettingDialog) {
                Dialog(
                    title = "设置",
                    onCloseRequest = { showSettingDialog = false },
                    resizable = false,
                    state = DialogState(
                        position = WindowPosition(Alignment.Center),
                        size = DpSize(300.dp, 350.dp)
                    )
                ) { SettingUI(state.settingState) }
            }
        }

        //monitor
        FloatingActionButton(
            onClick = {
                state.openMonitor()
            },
            backgroundColor = state.config.themeConfig.menuButtonColor,
        ) {
            Icon(
                painter = painterResource("icon/monitor.svg"),
                contentDescription = "open monitor",
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }

        //installApk
        var showInstallApkDialog by remember { mutableStateOf(false) }
        FloatingActionButton(
            onClick = { showInstallApkDialog = true },
            backgroundColor = state.config.themeConfig.menuButtonColor,
        ) {
            Icon(
                painter = painterResource("icon/apk_install.svg"),
                contentDescription = "install apk",
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )

            //安装Dialog
            if (showInstallApkDialog) {
                var installTips by remember { mutableStateOf("将Apk文件拖拽到这里安装...") }
                var hasInstallTask by remember { mutableStateOf(false) }

                Dialog(
                    title = "安装APK",
                    onCloseRequest = {
                        if (!hasInstallTask) showInstallApkDialog = false //如果有安装任务, 不允许关闭
                    },
                    state = DialogState(
                        position = WindowPosition(Alignment.Center),
                        size = DpSize(400.dp, 250.dp)
                    )
                ) {
                    LaunchedEffect(Unit) {
                        window.dropTarget = DropTarget().apply {
                            addDropTargetListener(object : DropTargetAdapter() {
                                override fun drop(dtde: DropTargetDropEvent) {

                                    //如果存在安装任务
                                    if (hasInstallTask) return

                                    //否则开始安装
                                    dtde.acceptDrop(DnDConstants.ACTION_COPY)
                                    val fileListFlavor = dtde.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                                    if (fileListFlavor is List<*>) {
                                        val file = fileListFlavor[0] as File
                                        if (file.isFile && file.extension.lowercase(Locale.getDefault()) == "apk") {
                                            hasInstallTask = true
                                            installTips = "${file.absolutePath}\n\n正在安装, 请稍后..."
                                            state.installApk(file.absolutePath) { result, message ->
                                                installTips = if (result) {
                                                    "${file.absolutePath}\n\n安装成功!"
                                                } else {
                                                    "${file.absolutePath}\n\n安装失败!\n\n$message"
                                                }
                                                hasInstallTask = false
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SelectionContainer { Text(installTips, textAlign = TextAlign.Center) }
                    }
                }
            }
        }
    }
}