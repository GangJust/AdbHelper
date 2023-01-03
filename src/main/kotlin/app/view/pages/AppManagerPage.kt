package app.view.pages

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.ui.window.Dialog
import app.model.AppDescModel
import app.state.pages.AppManagerState
import base.mvvm.AbstractView
import compose.SwingContainer
import compose.TabItem
import res.ColorRes
import res.IconRes
import res.TextStyleRes
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.util.Arrays
import javax.swing.*

class AppManagerPage : AbstractView<AppManagerState>() {
    override fun createState() = AppManagerState()

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
                    onClick = { state.showApkInstallWindow.value = true },
                    content = {
                        Icon(
                            painter = IconRes.apkInstall,
                            contentDescription = "安装",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                )

                if (state.showApkInstallWindow.value) {
                    Dialog(
                        onCloseRequest = {
                            state.showApkInstallWindow.value = false
                        },
                        title = "将Apk文件托入窗口安装",
                        content = {
                            SwingContainer(modifier = Modifier.fillMaxSize()) {
                                JLabel().apply {
                                    text = "将Apk文件托入窗口安装"
                                    horizontalAlignment = SwingConstants.CENTER
                                    verticalAlignment = SwingConstants.CENTER
                                    transferHandler = object : TransferHandler() {
                                        override fun importData(comp: JComponent, t: Transferable): Boolean {
                                            val transferData = t.getTransferData(DataFlavor.javaFileListFlavor)
                                            if (transferData is List<*>) {
                                                val file = transferData[0] as File
                                                state.installApk(file.absolutePath) {
                                                    text = it
                                                }
                                            }
                                            return true
                                        }

                                        override fun canImport(
                                            comp: JComponent,
                                            transferFlavors: Array<out DataFlavor>
                                        ): Boolean {
                                            return true
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            },
            topBar = { TopContainer() },
            bottomBar = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().height(56.dp), //底部占位, 统一高度
                    content = {},
                )
            },
            content = { AppList() }
        )
    }

    @Composable
    private fun TopContainer() {
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            TabItem(
                index = 0,
                title = "全部 (${state.allAppList.size})",
                selected = state.currentTabIndex.value == 0,
                onSelect = { state.currentTabIndex.value = it }
            )
            TabItem(
                index = 1,
                title = "系统 (${state.systemAppList.size})",
                selected = state.currentTabIndex.value == 1,
                onSelect = { state.currentTabIndex.value = it }
            )
            TabItem(
                index = 2,
                title = "用户 (${state.userAppList.size})",
                selected = state.currentTabIndex.value == 2,
                onSelect = { state.currentTabIndex.value = it }
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { state.reloadAppList() },
                content = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = ColorRes.divider,
                    )
                }
            )
        }
    }

    @Composable
    private fun AppList() {
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(state = state.currentLazyListState) {
                items(state.currentAppList.size) {
                    AppListItem(state.currentAppList[it])
                }
            }

            VerticalScrollbar(
                adapter = ScrollbarAdapter(state.currentLazyListState),
                modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterEnd)
            )
        }
    }

    @Composable
    private fun AppListItem(
        appDesc: AppDescModel,
    ) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                ItemView(
                    title = "包       名:",
                    value = appDesc.packageName,
                )
                ItemView(
                    title = "版  本  名:",
                    value = appDesc.versionName,
                )
                ItemView(
                    title = "版  本  号:",
                    value = appDesc.versionCode,
                )
                ItemView(
                    title = "编译SDK:",
                    value = appDesc.targetSdk,
                )
                ItemView(
                    title = "最小SDK:",
                    value = appDesc.minSdk,
                )
                ItemView(
                    title = "系统应用:",
                    value = if (appDesc.isSystemApp) "是" else "否",
                )
                ItemView(
                    title = "签名版本:",
                    value = "v${appDesc.apkSigningVersion}",
                )
                ItemView(
                    title = "安装路径:",
                    value = appDesc.installedPath,
                    singleLine = false,
                )
            }
        }
    }

    @Composable
    private fun ItemView(
        title: String,
        value: String,
        singleLine: Boolean = true,
    ) {
        Row(
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            modifier = Modifier.padding(vertical = 2.dp),
        ) {
            Text(
                text = title,
                style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.widthIn(min = 80.dp).padding(end = 4.dp)
            )
            BasicTextField(
                value = value,
                textStyle = TextStyleRes.bodyMedium,
                readOnly = true,
                singleLine = singleLine,
                onValueChange = { /*禁止修改*/ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}