package app.view.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.comm.BaseScaffold
import app.model.FileModel
import app.model.FileType
import app.state.pages.FileSystemState
import base.mvvm.AbstractView
import compose.*
import extensions.pathSeparator
import res.ColorRes
import res.IconRes
import res.TextStyleRes
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.TransferHandler

/// 文件管理
class FileSystemPage : AbstractView<FileSystemState>() {
    override fun createState() = FileSystemState()

    @Composable
    override fun viewCompose() {
        BaseScaffold(
            topBar = { TopBarView() },
            floatingActionButton = { FloatingActionButtonView() },
            content = {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    LazyColumn(state = state.pathListScroller) {
                        items(state.pathList.size) {
                            if (it >= state.pathList.size) //不知道为什么会有数组越界的情况出现
                                Box { }
                            else
                                FileListItemView(item = state.pathList[it])
                        }
                    }
                    VerticalScrollbar(
                        adapter = ScrollbarAdapter(state.pathListScroller),
                        modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterEnd)
                    )
                }
            }
        )
    }

    @Composable
    private fun FloatingActionButtonView() {
        FloatingActionButton(
            contentColor = Color.White,
            backgroundColor = ColorRes.primary,
            onClick = { state.showPushDialog.value = true },
            content = {
                Icon(
                    painter = IconRes.push,
                    contentDescription = "推送",
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        //文件push弹窗
        if (state.showPushDialog.value) {
            Dialog(
                onCloseRequest = { state.showPushDialog.value = false },
                title = "推送文件",
                content = {
                    SwingContainer(modifier = Modifier.fillMaxSize()) {
                        JLabel().apply {
                            text = "将文件(目录)托入窗口"
                            horizontalAlignment = SwingConstants.CENTER
                            verticalAlignment = SwingConstants.CENTER
                            transferHandler = object : TransferHandler() {
                                override fun importData(comp: JComponent, t: Transferable): Boolean {
                                    val transferData = t.getTransferData(DataFlavor.javaFileListFlavor)
                                    if (transferData is List<*>) {

                                        //隐藏dialog
                                        state.showPushDialog.value = false

                                        //发送安装事件
                                        val file = transferData[0] as File
                                        state.onPushFile(file)

                                        //显示compose dialog, 接收安装信息
                                        ComposeDialog
                                            .setView {
                                                MessageDialogView(
                                                    modifier = Modifier.align(Alignment.Center).widthIn(max = 320.dp),
                                                    onlyConfirm = true,
                                                    title = "推送文件",
                                                    message = "正在推送文件, 如果是大文件可能耗时较久",
                                                    confirmText = "确定",
                                                    confirmCallback = {
                                                        //ComposeDialog.hide()
                                                    }
                                                )
                                            }
                                            .show()
                                    }
                                    return true
                                }

                                override fun canImport(comp: JComponent, transferFlavors: Array<out DataFlavor>): Boolean {
                                    return true
                                }
                            }
                        }
                    }
                },
            )
        }
    }

    @Composable
    private fun TopBarView() {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                //回到首页按钮
                CardButton(
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    elevation = 4.dp,
                    onClick = {
                        state.currentPath.value = "/"
                        state.loadPath()
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "首页",
                            tint = ColorRes.icon,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                )

                //回到首页按钮
                CardButton(
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    elevation = 4.dp,
                    onClick = {
                        state.currentPath.value = "/storage/emulated/0"
                        state.loadPath(File(state.currentPath.value))
                    },
                    content = {
                        Icon(
                            painter = IconRes.storage,
                            contentDescription = "外置存储器",
                            tint = ColorRes.icon,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                )

                //路径框
                Card(
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp,
                    content = {
                        CustomTextField(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fillMaxWidth = true,
                            value = state.currentPath,
                            hintText = "请输入路径..",
                            textStyle = TextStyleRes.bodyMedium,
                            onValueChange = {
                                state.currentPath.value = it
                            },
                        )
                    }
                )

                //刷新当前页
                CardButton(
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    elevation = 4.dp,
                    onClick = {
                        state.loadPath(File(state.currentPath.value))
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "刷新",
                            tint = ColorRes.icon,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                )

                //跳转到某个路径
                CardButton(
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    elevation = 4.dp,
                    onClick = {
                        state.toTargetPath(state.currentPath.value)
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "跳转",
                            tint = ColorRes.icon,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                )
            }
        )
    }

    // 文件列表项
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun FileListItemView(item: FileModel) {
        CardButton(
            shape = RoundedCornerShape(8.dp),
            onMouseClick = {
                if (this.buttons.isPrimaryPressed) {
                    state.onItemPrimaryClick(item, state.currentPath.value)
                } else {
                    state.onItemSecondaryClick(item, state.currentPath.value) {
                        state.dialogContentType.value = 1 //默认显示
                        state.getFileSize(item)
                        ComposeDialog
                            .setView {
                                when (state.dialogContentType.value) {
                                    1 -> FileDescDialogView(it)
                                    2 -> FileDropFileDialogView(it)
                                    3 -> FilePermChangeDialogView(it)
                                }
                            }
                            .show()
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            color = Color.White,
            elevation = 4.dp,
            content = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart,
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                Icon(
                                    painter = when (item.type) {
                                        FileType.FILE -> IconRes.file
                                        FileType.DIR -> IconRes.folder
                                        FileType.LINK_DIR -> IconRes.linkFolder
                                        FileType.LINK_FILE -> IconRes.linkFile
                                        FileType.UNKNOWN -> IconRes.unknownFile
                                    },
                                    contentDescription = item.type.name,
                                    tint = ColorRes.icon,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = item.name,
                                    style = TextStyleRes.bodyMedium.copy(
                                        if (state.pushFileName.value == item.name) ColorRes.primary else ColorRes.text,
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.lastModifyTime,
                                    style = TextStyleRes.bodySmall.copy(color = ColorRes.description),
                                )
                            }
                        )
                    }
                )
            }
        )

    }

    /// 因为 ComposeDialog 是单例封装, 这里就直接动态更新 Dialog 弹层中的内容
    /// state.dialogContentType

    //文件(夹)详情弹窗内容  -- 默认
    @Composable
    private fun BoxWithConstraintsScope.FileDescDialogView(item: FileModel) {
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp).sizeIn(maxWidth = 360.dp),
            content = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            Text(
                                text = "详情",
                                style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    val selection = StringSelection(File(item.path, item.name).path.pathSeparator())
                                    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                                    ComposeToast.show("路径复制成功!")
                                },
                                content = {
                                    Icon(
                                        painter = IconRes.contentCopy,
                                        tint = ColorRes.icon,
                                        contentDescription = "复制路径",
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            )
                            IconButton(
                                onClick = { ComposeDialog.hide() },
                                content = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        tint = ColorRes.icon,
                                        contentDescription = "关闭",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        FileDescDialogItem("名  称", item.name)
                        FileDescDialogItem("目  录", item.path)
                        //state.getFileSize()
                        FileDescDialogItem("大  小", state.currentFileSize.value)
                        FileDescDialogItem("权  限", item.typePerm)
                        FileDescDialogItem("所有者", item.ownerName)
                        FileDescDialogItem("用户组", item.groupName)
                        FileDescDialogItem("修改时间", item.lastModifyTime)
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            CardButton(
                                modifier = Modifier,
                                onClick = {
                                    state.dialogContentType.value = 2
                                },
                                content = {
                                    Text(
                                        text = "删除",
                                        style = TextStyleRes.bodyMediumSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            )
                            Spacer(Modifier.weight(1f))
                            CardButton(
                                modifier = Modifier,
                                onClick = {
                                    state.dialogContentType.value = 3
                                },
                                content = {
                                    Text(
                                        text = "权限",
                                        style = TextStyleRes.bodyMediumSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            CardButton(
                                modifier = Modifier,
                                onClick = {
                                    state.onPullFile(item)
                                },
                                content = {
                                    Text(
                                        text = "提取",
                                        style = TextStyleRes.bodyMediumSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            },
        )
    }

    @Composable
    private fun FileDescDialogItem(
        title: String,
        value: String,
        highlight: Boolean = false,
        singleLine: Boolean = true,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
            content = {
                Text(
                    modifier = Modifier.weight(3f),
                    text = title,
                    style = TextStyleRes.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (highlight) ColorRes.primary else ColorRes.text,
                    ),
                )
                CustomTextField(
                    value = mutableStateOf(value),
                    enabled = true,
                    hintText = "无",
                    singleLine = singleLine,
                    onValueChange = { /* BasicTextField 可以响应 Ctrl+A 组合按键, 这里禁止编辑 */ },
                    textStyle = TextStyleRes.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp).weight(9f),
                )
            }
        )
    }

    //删除文件(夹)弹窗内容
    @Composable
    private fun BoxWithConstraintsScope.FileDropFileDialogView(item: FileModel) {
        MessageDialogView(
            modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp).sizeIn(maxWidth = 360.dp),
            title = "提示",
            message = "是否要删除该项?",
            cancelText = "取消",
            confirmText = "确定",
            cancelCallback = {
                state.dialogContentType.value = 1
            },
            confirmCallback = {
                state.onDropFile(item)
            }
        )
    }

    //文件(夹)权限修改弹窗内容
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun BoxWithConstraintsScope.FilePermChangeDialogView(item: FileModel) {
        state.getPermGroup(item)
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp).sizeIn(maxWidth = 300.dp),
            content = {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(
                        text = "权限修改",
                        style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp),
                    )

                    /*
                        0     1    2    3
                        4     5    6    7
                        8     9   10   11
                        12   13   14   15
                     */
                    LazyVerticalGrid(
                        modifier = Modifier.padding(vertical = 8.dp),
                        cells = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center,
                        content = {
                            //state.getPermGroup()
                            items(state.permGroupValue.size) {
                                when (it) {
                                    0 -> {
                                        Text(
                                            text = " ",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                        )
                                    }

                                    1 -> {
                                        Text(
                                            text = "读",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    2 -> {
                                        Text(
                                            text = "写",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    3 -> {
                                        Text(
                                            text = "执行",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    4 -> {
                                        Text(
                                            text = "所有者",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    8 -> {
                                        Text(
                                            text = "用户组",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    12 -> {
                                        Text(
                                            text = "其他",
                                            style = TextStyleRes.bodyMedium.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                    }

                                    else -> {
                                        Checkbox(
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ColorRes.primary,
                                                uncheckedColor = ColorRes.icon,
                                            ),
                                            checked = state.permGroupValue[it],
                                            onCheckedChange = { check ->
                                                state.permGroupValue[it] = check
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        CardButton(
                            modifier = Modifier,
                            onClick = { state.dialogContentType.value = 1 },
                            content = {
                                Text(
                                    text = "取消",
                                    style = TextStyleRes.bodyMediumSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        CardButton(
                            modifier = Modifier,
                            onClick = {
                                state.onPermChange(item)
                            },
                            content = {
                                Text(
                                    text = "修改",
                                    style = TextStyleRes.bodyMediumSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        )
                    }
                }
            },
        )
    }
}