package app.view.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.comm.BaseScaffold
import app.model.LayoutTree
import app.state.pages.ViewLayoutState
import base.mvvm.AbstractView
import base.mvvm.ViewCompose
import compose.*
import extensions.right
import res.ColorRes
import res.IconRes
import res.TextStyleRes

class ViewLayoutPage : AbstractView<ViewLayoutState>() {
    override fun createState() = ViewLayoutState()

    @Composable
    override fun viewCompose() {
        // 查询结果发生改变, 滚动到指定位置
        LaunchedEffect("${state.queryKeyword.value}${state.queryNowIndex.value}") {
            if (state.queryLayoutTree.isEmpty() || state.queryNowIndex.value == -1) return@LaunchedEffect

            state.queryNowNode = state.queryLayoutTree[state.queryNowIndex.value]
            val offset = state.getTreeNodePosition(state.queryNowNode!!)
            if (offset != null) {
                state.verticalScrollState.scrollBy(offset.y - 250)
            }
        }

        val nothingView = remember { UnknownPage("没有布局信息..") }

        ComposeOverlayContainer(
            controller = state.queryOverlayController,
            content = {
                BaseScaffold(
                    floatingActionButton = {
                        FloatingActionButtonGroup()
                    },
                    content = {
                        if (state.layoutTree.isEmpty()) {
                            ViewCompose { nothingView }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .verticalScroll(state.verticalScrollState)
                                        .horizontalScroll(state.horizontalScrollState),
                                    content = {
                                        TreeNodeView(
                                            nodes = state.layoutTree,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                )
                                VerticalScrollbar(
                                    adapter = rememberScrollbarAdapter(state.verticalScrollState),
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(horizontal = 4.dp, vertical = 12.dp)
                                )
                                HorizontalScrollbar(
                                    adapter = rememberScrollbarAdapter(state.horizontalScrollState),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                )
            }
        )
    }

    /// 悬浮按钮组
    @Composable
    private fun FloatingActionButtonGroup() {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            if (state.layoutTree.isNotEmpty()) {
                FloatingActionButton(
                    contentColor = Color.White,
                    backgroundColor = ColorRes.primary,
                    onClick = {
                        if (state.queryOverlayController.isShowing) {
                            state.queryOverlayController.hide()
                        } else {
                            state.changeChildrenOpenState(state.layoutTree.first(), true)
                            state.queryOverlayController
                                .setView { QueryOverlayView() }
                                .show()
                        }
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "搜索",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }
            FloatingActionButton(
                contentColor = Color.White,
                backgroundColor = ColorRes.primary,
                onClick = {
                    state.queryOverlayController.hide()
                    state.loadLayoutXml()
                },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "载入",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            if (state.layoutTree.isNotEmpty()) {
                Spacer(modifier = Modifier.height(72.dp)) //底部垫高
            }
        }
    }

    /// 树形视图
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TreeNodeView(
        nodes: List<LayoutTree>,
        modifier: Modifier,
    ) {
        Column(modifier = modifier) {
            for (node in nodes) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        // 左侧扩展, 写if else 太不舒服了, 抽成组件也看着不舒服, 直接写下边了, 而且写else布局大小还不一定相同
                        val hasChildren = node.children.isNotEmpty()
                        Surface(
                            elevation = if (hasChildren) 4.dp else 0.dp,
                            enabled = hasChildren,
                            color = if (hasChildren) ColorRes.white else ColorRes.transparent,
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                node.isOpen.value = !node.isOpen.value
                                if (!node.isOpen.value) {
                                    state.changeChildrenOpenState(node, false)
                                }
                            },
                            content = {
                                Icon(
                                    painter = if (node.isOpen.value) IconRes.fold else IconRes.open,
                                    contentDescription = "展开/折叠",
                                    tint = if (hasChildren) ColorRes.icon else ColorRes.transparent,
                                    modifier = Modifier.padding(4.dp).size(18.dp)
                                )
                            }
                        )

                        // 右侧内容
                        Surface(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.onGloballyPositioned {
                                state.rememberTreeNodePosition(node, it.positionInWindow())
                            },
                            onClick = {
                                //详细信息弹层
                                ComposeDialog
                                    .setView { NodeDescDialogView(node = node) }
                                    .show()
                            },
                            content = {
                                var style = TextStyleRes.bodyMedium
                                // 当前定位高亮样式
                                if (state.queryNowNode == node) style = style.copy(color = ColorRes.orange)
                                Text(
                                    text = "${node.className} ${if (node.children.isEmpty()) "" else "(${node.children.size})"}",
                                    style = style,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                        )
                    }
                )

                //如果有子项, 递归构建
                if (node.isOpen.value) {
                    if (node.children.isNotEmpty()) {
                        TreeNodeView(
                            nodes = node.children,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }

    /// 详情弹层
    @Composable
    private fun BoxWithConstraintsScope.NodeDescDialogView(
        node: LayoutTree,
    ) {
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 16.dp)
                .sizeIn(maxWidth = 440.dp),
            content = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            Text(
                                text = node.className.right(".", true),
                                style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
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
                    VerScrollableContainer(contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            NodeDescDialogItem("index", node.index)
                            NodeDescDialogItem("package", node.packageName)
                            NodeDescDialogItem("className", node.className)
                            NodeDescDialogItem("id", node.resourceId)
                            NodeDescDialogItem("text", node.text)
                            NodeDescDialogItem("contentDesc", node.contentDesc)
                            NodeDescDialogItem("password", node.password)
                            NodeDescDialogItem("enabled", node.enabled)
                            NodeDescDialogItem("checkable", node.checkable)
                            NodeDescDialogItem("checked", node.checked)
                            NodeDescDialogItem("clickable", node.clickable)
                            NodeDescDialogItem("longClickable", node.longClickable)
                            NodeDescDialogItem("focusable", node.focusable)
                            NodeDescDialogItem("focused", node.focused)
                            NodeDescDialogItem("scrollable", node.scrollable)
                            NodeDescDialogItem("selected", node.selected)
                            NodeDescDialogItem("bounds", node.bounds)
                        }
                    }
                }
            },
        )
    }

    /// 详情弹层项
    @Composable
    private fun NodeDescDialogItem(
        title: String,
        value: String,
        singleLine: Boolean = true,
    ) {
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                ) {
                    Text(
                        text = title,
                        style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp).weight(3f),
                    )
                    CustomTextField(
                        value = mutableStateOf(value),
                        enabled = true,
                        hintText = "该属性没有值",
                        singleLine = singleLine,
                        onValueChange = { /* BasicTextField 可以响应 Ctrl+A 组合按键, 这里禁止编辑 */ },
                        textStyle = TextStyleRes.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).weight(9f),
                    )
                }
            }
        )
    }

    /// 搜索悬浮层
    @Composable
    private fun BoxScope.QueryOverlayView() {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopEnd)
                .width(260.dp),
            verticalArrangement = Arrangement.Center,
            content = {
                Card(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    content = {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                CustomTextField(
                                    value = state.queryKeyword,
                                    hintText = "请输入关键字",
                                    onValueChange = {
                                        state.queryKeyword.value = it
                                        state.queryNowIndex.value = 0
                                        state.queryLayoutXml()
                                    },
                                    singleLine = true,
                                    textStyle = TextStyleRes.bodyMedium.copy(color = ColorRes.secondaryText),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        )
                    }
                )

                if (state.queryLayoutTree.isNotEmpty()) {
                    Card(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(vertical = 16.dp),
                        content = {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = {
                                    Text(
                                        text = "${state.queryLayoutTree.size}个匹配项",
                                        style = TextStyleRes.bodyMedium.copy(color = ColorRes.secondaryText),
                                        modifier = Modifier
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${state.queryNowIndex.value + 1}/${state.queryLayoutTree.size}",
                                        style = TextStyleRes.bodyMedium.copy(color = ColorRes.description),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    CardButton(
                                        modifier = Modifier,
                                        shape = RoundedCornerShape(16.dp),
                                        onClick = { state.previousResult() },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                                contentDescription = "向上",
                                                tint = ColorRes.icon,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                    CardButton(
                                        modifier = Modifier,
                                        shape = RoundedCornerShape(16.dp),
                                        onClick = { state.nextResult() },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = "向下",
                                                tint = ColorRes.icon,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            }
        )
    }
}