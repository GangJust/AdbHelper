package utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Compose各种单位(sp, dp等)解析工具
object UnitUtils {

    fun parseSp(sp: String?): TextUnit {
        sp ?: return 16.sp
        if ((sp.indexOf("sp") != 0) && (sp.indexOf("sp") != -1)) {
            val s = sp.substring(0, sp.indexOf("sp"))
            return s.toFloat().sp
        }
        return sp.toFloat().sp
    }

    fun parseDp(dp: String?): Dp {
        dp ?: return 16.dp
        if ((dp.indexOf("dp") != 0) && (dp.indexOf("dp") != -1)) {
            val s = dp.substring(0, dp.indexOf("dp"))
            return s.toFloat().dp
        }
        return dp.toFloat().dp
    }

    fun toStringSp(sp: TextUnit): String {
        return "${sp.value}sp"
    }

    fun toStringDp(dp: Dp): String {
        return "${dp.value}dp"
    }
}