package res

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object TextStyleRes {
    val bodyLarge = TextStyle(color = ColorRes.text, fontSize = SpRes.bodyLarge)
    val bodyMedium = TextStyle(color = ColorRes.text, fontSize = SpRes.bodyMedium)
    val bodySmall = TextStyle(color = ColorRes.text, fontSize = SpRes.bodySmall)

    val bodyMediumSurface = bodyMedium.copy(color = ColorRes.primary)
}