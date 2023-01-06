package compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import res.ColorRes

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    color: Color = ColorRes.transparent,
    contentColor: Color = ColorRes.transparent,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        color = color,
        contentColor = contentColor,
        shape = shape,
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = modifier,
            content = {
                content()
            }
        )
    }
}