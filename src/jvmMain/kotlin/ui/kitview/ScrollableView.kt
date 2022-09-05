package ui.kitview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 垂直滚动
 */
@Composable
fun VerScrollableContainer(
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.verticalScroll(verticalScroll)) {
            content()
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScroll)
        )
    }
}

/**
 * 水平滚动
 */
@Composable
fun HorScrollableContainer(
    content: @Composable () -> Unit,
) {
    val horizontalScroll = rememberScrollState(0)
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.horizontalScroll(horizontalScroll)) {
            content()
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth(),
            adapter = rememberScrollbarAdapter(horizontalScroll)
        )
    }
}

/**
 * 滚动(包括垂直、水平)
 */
@Composable
fun ScrollableContainer(
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    val horizontalScroll = rememberScrollState(0)
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.verticalScroll(verticalScroll).horizontalScroll(horizontalScroll)) {
            content()
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScroll)
        )
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            adapter = rememberScrollbarAdapter(horizontalScroll)
        )
    }
}