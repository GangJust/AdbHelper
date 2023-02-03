package compose


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import res.ColorRes
import res.TextStyleRes

@Composable
fun CustomTextField(
    value: MutableState<String>,
    onValueChange: (String) -> Unit,
    hintText: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = false,
    textStyle: TextStyle = TextStyleRes.bodyMedium,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
        content = {
            BasicTextField(
                value = value.value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                enabled = enabled,
                textStyle = textStyle,
                modifier = Modifier.padding(vertical = 4.dp).then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier),
            )

            if (value.value.isEmpty()) {
                Text(
                    text = hintText,
                    style = textStyle.copy(color = ColorRes.description),
                    modifier = Modifier,
                )
            }
        }
    )
}