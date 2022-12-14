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
import androidx.compose.material.icons.filled.Search
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
import compose.ComposeToast
import compose.MessageComposeDialog
import compose.SwingContainer
import compose.TabItem
import res.ColorRes
import res.IconRes
import res.TextStyleRes
import utils.toFileLength
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.TransferHandler

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
                    onClick = { state.showApkInstallDialog.value = true },
                    content = {
                        Icon(
                            painter = IconRes.apkInstall,
                            contentDescription = "??????",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                )

                if (state.showApkInstallDialog.value) {
                    Dialog(
                        onCloseRequest = {
                            state.showApkInstallDialog.value = false
                        },
                        title = "???Apk????????????????????????",
                        content = {
                            SwingContainer(modifier = Modifier.fillMaxSize()) {
                                JLabel().apply {
                                    text = "???Apk????????????????????????"
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
            topBar = { TopBar() },
            bottomBar = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().height(56.dp), //????????????, ????????????
                    content = {},
                )
            },
            content = { AppList() }
        )
    }

    @Composable
    private fun TopBar() {
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            TabItem(
                index = 0,
                title = "?????? (${state.allAppList.size})",
                selected = state.currentTabIndex.value == 0,
                onSelect = { state.currentTabIndex.value = it }
            )
            TabItem(
                index = 1,
                title = "?????? (${state.systemAppList.size})",
                selected = state.currentTabIndex.value == 1,
                onSelect = { state.currentTabIndex.value = it }
            )
            TabItem(
                index = 2,
                title = "?????? (${state.userAppList.size})",
                selected = state.currentTabIndex.value == 2,
                onSelect = { state.currentTabIndex.value = it }
            )

            //??????????????????, ?????????????????????
            if (state.filterKeyWords.value.isNotBlank() && state.filterAppList.isNotEmpty()) {
                TabItem(
                    index = 3,
                    title = "?????? (${state.filterAppList.size})",
                    selected = state.currentTabIndex.value == 3,
                    onSelect = { state.currentTabIndex.value = it }
                )
            }

            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = {
                    // TODO: ?????????
                    ComposeToast.show("?????????..")
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "??????",
                        tint = ColorRes.icon,
                    )
                }
            )
            IconButton(
                onClick = { state.reloadAppList() },
                content = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "??????",
                        tint = ColorRes.icon,
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
    private fun AppListItem(appDesc: AppDescModel) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            content = {
                Box {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ItemView(
                            title = "???       ???:",
                            value = appDesc.packageName,
                        )
                        ItemView(
                            title = "???  ???  ???:",
                            value = appDesc.versionName,
                        )
                        ItemView(
                            title = "???  ???  ???:",
                            value = appDesc.versionCode,
                        )
                        ItemView(
                            title = "??????SDK:",
                            value = appDesc.targetSdk,
                        )
                        ItemView(
                            title = "??????SDK:",
                            value = appDesc.minSdk,
                        )
                        ItemView(
                            title = "????????????:",
                            value = if (appDesc.isSystemApp) "???" else "???",
                        )
                        ItemView(
                            title = "????????????:",
                            value = "v${appDesc.apkSigningVersion}",
                        )
                        ItemView(
                            title = "????????????:",
                            value = appDesc.length.toFileLength(),
                        )
                        ItemView(
                            title = "????????????:",
                            value = appDesc.installedPath,
                            singleLine = false,
                        )
                    }

                    BoxWithConstraints(modifier = Modifier.align(Alignment.TopEnd)) {
                        Row {
                            IconButton(
                                onClick = {
                                    //????????????200M
                                    if (appDesc.length > 200 * 1024 * 1024) {
                                        MessageComposeDialog
                                            .setTitle("???????????????")
                                            .setMessage("App????????????, ??????????????????????????????!")
                                            .setCancelText("??????")
                                            .setConfirmText("??????")
                                            .setConfirmCallback { state.exportNotes(appDesc) }
                                            .show()
                                        return@IconButton
                                    }
                                    state.exportNotes(appDesc)
                                },
                                content = {
                                    Icon(
                                        painter = IconRes.exportNotes,
                                        contentDescription = "??????Apk",
                                        tint = ColorRes.icon,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                            if (!appDesc.isSystemApp) {
                                IconButton(
                                    onClick = {
                                        MessageComposeDialog
                                            .setTitle("??????")
                                            .setMessage("?????????????????????????")
                                            .setCancelText("??????")
                                            .setConfirmText("??????")
                                            .setConfirmCallback { state.uninstallApp(appDesc) }
                                            .show()
                                    },
                                    content = {
                                        Icon(
                                            painter = IconRes.delete,
                                            contentDescription = "??????Apk",
                                            tint = ColorRes.icon,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
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
            content = {
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
                    onValueChange = { /*????????????*/ },
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }
}