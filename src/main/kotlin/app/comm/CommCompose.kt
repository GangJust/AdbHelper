package app.comm

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import res.ColorRes

@Composable
fun BaseScaffold(
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        backgroundColor = ColorRes.transparent,
        contentColor = ColorRes.transparent,
        isFloatingActionButtonDocked = true,
        floatingActionButton = floatingActionButton,
        topBar = topBar,
        bottomBar = {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(56.dp), //底部占位, 统一高度
                content = { bottomBar() },
            )
        },
        content = content
    )
}