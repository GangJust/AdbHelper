package app.view.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import base.mvvm.IView
import res.ColorRes
import res.TextStyleRes

/// 未知页面
class UnknownPage constructor(
    private val message: String = "这个城市又多了个迷路的人...",
) : IView {
    @Composable
    override fun viewCompose() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource("image/ic_unknown.png"),
                contentDescription = "UnknownPage",
                modifier = Modifier.size(220.dp)
            )
            Spacer(Modifier.padding(vertical = 12.dp))
            Text(
                text = message,
                style = TextStyleRes.bodyLarge.copy(color = ColorRes.secondaryText),
            )
        }
    }
}