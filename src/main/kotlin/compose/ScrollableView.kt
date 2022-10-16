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
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    Box(Modifier.widthIn(max = maxWidth).heightIn(max = maxHeight)) {
        Box(Modifier.verticalScroll(verticalScroll)) {
            content()
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScroll)
        )
    }
}

@Composable  //水平滚动
fun HorScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    content: @Composable () -> Unit,
) {
    val horizontalScroll = rememberScrollState(0)
    Box(Modifier.widthIn(max = maxWidth).heightIn(max = maxHeight)) {
        Box(Modifier.horizontalScroll(horizontalScroll)) {
            content()
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            adapter = rememberScrollbarAdapter(horizontalScroll)
        )
    }
}

@Composable  //滚动(包括垂直、水平)
fun ScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    val horizontalScroll = rememberScrollState(0)
    Box(Modifier.widthIn(max = maxWidth).heightIn(max = maxHeight)) {
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