package app.state.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import app.logic.pages.ViewLayoutLogic
import app.model.LayoutTree
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import compose.ComposeOverlay
import compose.ComposeToast
import org.dom4j.DocumentHelper
import org.dom4j.Element

class ViewLayoutState : AbstractState<ViewLayoutLogic>() {
    override fun createLogic() = ViewLayoutLogic()
    private val homeState = StateManager.findState<HomeState>(HomeUI::class.java)
    private val device = homeState?.currentDevice?.value ?: ""

    // 视图树
    val layoutTree = mutableStateListOf<LayoutTree>()
    val verticalScrollState = ScrollState(0)
    val horizontalScrollState = ScrollState(0)

    // 树中搜索的结果
    val queryKeyword = mutableStateOf("")
    val queryLayoutTree = mutableStateListOf<LayoutTree>()

    // 当前定位节点
    val queryOverlayController = ComposeOverlay()
    var queryNowNode: LayoutTree? = null
    val queryNowIndex = mutableStateOf(0)

    // 树节点在屏幕中的像素坐标, [Modifier.onGloballyPositioned] 时获取
    private val treeNodePositionMap = mutableMapOf<LayoutTree, Offset>()

    /// 加载布局
    fun loadLayoutXml() {
        ComposeToast.show("正在加载布局信息, 请稍后..")
        launch {
            val layoutXml = logic.uiautomatorDump(device) { success, fail ->
                if (fail.isNotEmpty()) {
                    ComposeToast.show("布局获取失败!")
                    return@uiautomatorDump
                }
            }
            val document = DocumentHelper.parseText(layoutXml)
            val elements = document.rootElement.elements()

            layoutTree.clear() //重置树缓存
            queryKeyword.value = "" //重置搜索关键字缓存
            queryLayoutTree.clear() //重置搜索结果树缓存
            queryNowNode = null //重置当前定位节点缓存
            queryNowIndex.value = 0 //重置定位下标缓存
            treeNodePositionMap.clear() //重置树节点像素坐标缓存

            elements.forEach {
                val node = LayoutTree(
                    index = it.attributeValue("index"),
                    text = it.attributeValue("text"),
                    resourceId = it.attributeValue("resource-id"),
                    className = it.attributeValue("class"),
                    packageName = it.attributeValue("package"),
                    contentDesc = it.attributeValue("content-desc"),
                    checkable = it.attributeValue("checkable"),
                    checked = it.attributeValue("checked"),
                    clickable = it.attributeValue("clickable"),
                    enabled = it.attributeValue("enabled"),
                    focusable = it.attributeValue("focusable"),
                    focused = it.attributeValue("focused"),
                    scrollable = it.attributeValue("scrollable"),
                    longClickable = it.attributeValue("long-clickable"),
                    password = it.attributeValue("password"),
                    selected = it.attributeValue("selected"),
                    bounds = it.attributeValue("bounds"),
                    parent = null,
                    children = mutableListOf()
                )
                node.children = buildLayoutChildrenNode(node, it)
                layoutTree.add(node)
            }
        }
    }

    /// 构建子布局
    private fun buildLayoutChildrenNode(node: LayoutTree, parent: Element): MutableList<LayoutTree> {
        val layoutnodes = mutableListOf<LayoutTree>()
        val elements = parent.elements()
        if (elements.isEmpty()) return layoutnodes
        elements.forEach {
            val child = LayoutTree(
                index = it.attributeValue("index"),
                text = it.attributeValue("text"),
                resourceId = it.attributeValue("resource-id"),
                className = it.attributeValue("class"),
                packageName = it.attributeValue("package"),
                contentDesc = it.attributeValue("content-desc"),
                checkable = it.attributeValue("checkable"),
                checked = it.attributeValue("checked"),
                clickable = it.attributeValue("clickable"),
                enabled = it.attributeValue("enabled"),
                focusable = it.attributeValue("focusable"),
                focused = it.attributeValue("focused"),
                scrollable = it.attributeValue("scrollable"),
                longClickable = it.attributeValue("long-clickable"),
                password = it.attributeValue("password"),
                selected = it.attributeValue("selected"),
                bounds = it.attributeValue("bounds"),
                parent = node,
                children = mutableListOf()
            )
            child.children = buildLayoutChildrenNode(child, it)

            layoutnodes.add(child)
        }

        return layoutnodes
    }

    /// 递归改变树结构关闭/展开状态
    fun changeChildrenOpenState(node: LayoutTree, isOpen: Boolean) {
        node.isOpen.value = isOpen
        for (it in node.children) {
            it.isOpen.value = isOpen
            if (it.children.isNotEmpty()) {
                changeChildrenOpenState(it, isOpen)
            }
        }
    }

    /// 记录所有布局节点的位置
    fun rememberTreeNodePosition(node: LayoutTree, offset: Offset) {
        treeNodePositionMap[node] = offset
    }

    /// 获取某个布局节点的位置
    fun getTreeNodePosition(node: LayoutTree): Offset? {
        return treeNodePositionMap[node]
    }

    /// 查询布局
    fun queryLayoutXml() {
        //没有布局 或 没有关键字, 清空查询结果
        if (layoutTree.isEmpty() || queryKeyword.value.isEmpty()) {
            queryNowNode = null
            queryNowIndex.value = -1
            queryLayoutTree.clear()
            return
        }

        queryLayoutTree.clear()
        queryChildren(queryKeyword.value, layoutTree)
    }

    /// 递归查询包含某个关键字的所有布局
    private fun queryChildren(keyword: String, layouts: List<LayoutTree>) {
        if (layouts.isEmpty()) return

        for (node in layouts) {
            if (compareContain(keyword, node)) queryLayoutTree.add(node)
            if (node.children.isNotEmpty()) {
                queryChildren(keyword, node.children)
            }
        }
    }

    /// 上一个匹配结果
    fun previousResult() {
        queryNowIndex.value -= 1

        if (queryNowIndex.value < 0) {
            queryNowIndex.value = 0
        }
    }

    /// 下一个匹配结果
    fun nextResult() {
        queryNowIndex.value += 1

        if (queryNowIndex.value >= queryLayoutTree.size) {
            queryNowIndex.value = queryLayoutTree.size - 1
        }
    }

    /// 比较
    private fun compareContain(keyword: String, node: LayoutTree): Boolean {
        return node.packageName.contains(keyword)
                || node.className.contains(keyword)
                || node.resourceId.contains(keyword)
                || node.text.contains(keyword)
                || node.contentDesc.contains(keyword)
                || node.bounds.contains(keyword)
    }
}