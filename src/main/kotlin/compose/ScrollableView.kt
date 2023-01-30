package compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable  //垂直滚动
fun VerScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(),
    scrollbarPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
    ) {
        Box(
            content = { content() },
            modifier = Modifier.verticalScroll(verticalScroll)
        )
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).padding(scrollbarPadding).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScroll)
        )
    }
}

@Composable  //水平滚动
fun HorScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(),
    scrollbarPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    val horizontalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
    ) {
        Box(
            content = { content() },
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
        )
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomCenter).padding(scrollbarPadding).fillMaxWidth(),
            adapter = rememberScrollbarAdapter(horizontalScroll)
        )
    }
}

@Composable  //滚动(包括垂直、水平)
fun ScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    val horizontalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
    ) {
        Box(
            content = { content() },
            modifier = Modifier
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll),
        )
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