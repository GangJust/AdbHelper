package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.kitview.ScrollableContainer
import ui.model.MainViewModel

@Composable
fun mItemView(
    title: String,
    value: String,
    modifier: Modifier,
    style: TextStyle,
) {
    Box(
        modifier = modifier,
        content = {
            Card(
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4),
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier,
                        content = {
                            Text(
                                modifier = modifier,
                                text = title,
                                style = style,
                            )
                            Text(
                                modifier = modifier,
                                text = value,
                                style = style,
                            )
                        },
                    )
                },
            )
        },
    )
}

@Composable
fun mScrollItemView(
    title: String,
    value: String,
    modifier: Modifier,
    style: TextStyle,
) {
    Box(
        modifier = modifier,
        content = {
            Card(
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(4),
                content = {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = modifier,
                        content = {
                            Text(
                                modifier = modifier,
                                text = title,
                                style = style,
                            )
                            ScrollableContainer {
                                SelectionContainer {
                                    Text(
                                        modifier = modifier.then(Modifier.fillMaxWidth()),
                                        text = value,
                                        style = style,
                                    )
                                }
                            }
                        },
                    )
                },
            )
        },
    )
}


@Composable
@Preview
fun App() {
    val viewModel = MainViewModel()
    val paddingValues = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
    val modifier = Modifier.padding(paddingValues)
    val textStyle = TextStyle.Default.copy(fontSize = 16.sp)

    Scaffold(
        content = {
            Column {
                mItemView("PackageName：", viewModel.getFinalData().mPackageName, modifier, textStyle)
                mItemView("CurrentActivity：", viewModel.getFinalData().mResumedActivity, modifier, textStyle)
                mItemView("LastActivity：", viewModel.getFinalData().mLastPausedActivity, modifier, textStyle)
                mScrollItemView("Activity Stack：", viewModel.getFinalData().mActivityRecordList, modifier, textStyle)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                content = {
                    Icon(Icons.Rounded.Refresh, "重新加载")
                },
                onClick = {
                    viewModel.onRefresh()
                },
            )
        },
    )
}