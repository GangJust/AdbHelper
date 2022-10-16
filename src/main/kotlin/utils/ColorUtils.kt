package utils

import androidx.compose.ui.graphics.Color

object ColorUtils {

    fun parseColor(hexColor: String?): Color {
        hexColor ?: return Color.White
        if (hexColor.contains("#") && (hexColor.indexOf("#") == 0)) {
            val hex = hexColor.substring(1)
            if (hex.length == 6) return Color("ff$hex".toLong(16))
            else if (hex.length == 8) return Color(hex.toLong(16))
        }
        return Color.White
    }

    fun toHexColor(color: Color): String {
        val hex = color.value.toString(16).substring(0, 8)
        return "#$hex"
    }
}