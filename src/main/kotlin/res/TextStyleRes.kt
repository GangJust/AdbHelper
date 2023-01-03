package res

import androidx.compose.ui.text.TextStyle

object TextStyleRes {
    val bodyLarge = TextStyle(color = ColorRes.text, fontSize = SpRes.bodyLarge)
    val bodyMedium = TextStyle(color = ColorRes.text, fontSize = SpRes.bodyMedium)
    val bodySmall = TextStyle(color = ColorRes.text, fontSize = SpRes.bodySmall)

    val bodyMediumSurface = bodyMedium.copy(color = ColorRes.primary)
}