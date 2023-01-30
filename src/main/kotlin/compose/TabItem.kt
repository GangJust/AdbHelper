package compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import res.ColorRes
import res.TextStyleRes

@Composable
fun TabItem(
    index: Int,
    title: String,
    onSelect: (Int) -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier.padding(horizontal = 4.dp)
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        TextButton(
            modifier = Modifier.widthIn(min = 80.dp),
            shape = RoundedCornerShape(8.dp),
            onClick = { onSelect.invoke(index) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = ColorRes.onSurface,
                backgroundColor = if (selected) ColorRes.onSurface else ColorRes.transparent,
            ),
            content = {
                Text(
                    text = title,
                    style = if (selected) TextStyleRes.bodyMediumSurface else TextStyleRes.bodyMedium,
                )
            }
        )
    }
}