package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import compose.HorScrollableContainer
import compose.ScrollableContainer
import state.AppState
import state.HomeState

/**
 * @Author: Gang
 * @Date: 2022/10/7 00:07
 * @Description:
 */

@Composable
fun HomeUI(state: HomeState) {
    val textSize = state.config.themeConfig.textSize
    Column(modifier = Modifier.padding(12.dp)) {
        SelectionItem(textSize, "当前包名：", state.currentPackage.value)
        SelectionItem(textSize, "当前活动：", state.currentActivity.value)
        SelectionItem(textSize, "上次活动：", state.lastActivity.value)
        SelectionScrollItem(textSize, "活动栈信息：", state.historyActivity.value)
    }
}

@Composable //可 Selection 项
fun SelectionItem(textSize: TextUnit, title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            title,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            elevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
        ) {
            HorScrollableContainer {
                SelectionContainer {
                    Text(
                        value,
                        fontSize = textSize,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable //可 Selection 滚动项
fun SelectionScrollItem(textSize: TextUnit, title: String, values: List<String>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            title,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            elevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
        ) {
            ScrollableContainer(maxHeight = 220.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    for (value in values) {
                        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                value,
                                fontSize = textSize,
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}