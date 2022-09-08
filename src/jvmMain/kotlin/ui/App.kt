package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val titleModifier = modifier.then(Modifier.widthIn(min = 120.dp))
    Column(
        modifier = modifier,
        content = {
            Text(
                modifier = titleModifier,
                text = title,
                style = style.copy(fontWeight = FontWeight.Bold),
            )
            Card(
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
                shape = AbsoluteCutCornerShape(4.dp),
                content = {
                    SelectionContainer {
                        Text(
                            text = value,
                            style = style,
                            modifier = modifier.then(
                                Modifier.heightIn(min = 34.dp)
                                    .wrapContentHeight(Alignment.CenterVertically, false),//最小高度垂直居中对齐
                            ),
                        )
                    }
                },
            )
        }
    )
}

@Composable
fun mScrollItemView(
    title: String,
    value: List<String>,
    modifier: Modifier,
    style: TextStyle,
) {
    val titleModifier = modifier.then(Modifier.widthIn(min = 120.dp))
    Column(
        modifier = modifier,
        content = {
            Text(
                modifier = titleModifier,
                text = title,
                style = style.copy(fontWeight = FontWeight.Bold),
            )
            Card(
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = AbsoluteCutCornerShape(4.dp),
                content = {
                    ScrollableContainer {
                        Column(modifier = modifier) {
                            value.forEach {
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                        text = it,
                                        style = style,
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    )
}


@Composable
@Preview
fun mApp() {
    val viewModel = MainViewModel()
    val paddingValues = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
    val modifier = Modifier.padding(paddingValues)
    val textStyle = TextStyle.Default.copy(fontSize = 16.sp)

    //首次加载
    viewModel.onRefresh()

    //脚手架构建
    Scaffold(
        backgroundColor = Color.White,
        content = {
            Column {
                mItemView("当前包名：", viewModel.getFinalData().mPackageName, modifier, textStyle)
                mItemView("当前活动：", viewModel.getFinalData().mResumedActivity, modifier, textStyle)
                mItemView("历史活动：", viewModel.getFinalData().mLastPausedActivity, modifier, textStyle)
                mScrollItemView("活动栈信息：", viewModel.getFinalData().mActivityRecordList, modifier, textStyle)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                content = {
                    Icon(Icons.Rounded.Refresh, "刷新")
                },
                onClick = {
                    viewModel.onRefresh()
                },
            )
        },
        isFloatingActionButtonDocked = true,
    )
}