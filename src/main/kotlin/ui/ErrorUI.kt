package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.ScrollableContainer

/**
 * @Author: Gang
 * @Date: 2022/10/16 11:55
 * @Description:
 */
@Composable
fun ErrorUI(exception: Exception?) {
    ScrollableContainer {
        SelectionContainer {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.Gray, fontSize = 16.sp)) {
                            append("啊哦, 出现异常了。\n")
                        }

                        val stackTraceString = String("${exception?.stackTraceToString()}".toByteArray())
                        val stackTrace = stackTraceString.replace("\t", "        ")
                        withStyle(SpanStyle(color = Color.Gray, fontSize = 14.sp)) {
                            append(stackTrace)
                        }
                    },
                )
            }
        }
    }
}