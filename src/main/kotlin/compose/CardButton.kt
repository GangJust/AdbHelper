package compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            modifier = modifier,
            contentAlignment = Alignment.Center,
            content = { content() }
        )
    }
}


@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.clip(shape).clickable { onClick.invoke() }.then(modifier),
        contentAlignment = Alignment.Center,
        content = {
            Box(
                modifier = Modifier.padding(contentPadding),
                content = { content() }
            )
        }
    )
}