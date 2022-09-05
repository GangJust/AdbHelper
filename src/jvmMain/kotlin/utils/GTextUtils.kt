package utils

/// 字符串工具类
object GTextUtils {
    /// 空判断
    fun <S : CharSequence?> isEmpty(text: S?): Boolean {
        return text == null || isEmpty(text.toString())
    }

    fun isEmpty(value: String?): Boolean {
        return value == null || to(value).isEmpty() || to(value) == "null"
    }

    /// 空数组判断
    fun <S : CharSequence?> isEmpties(vararg texts: S): Boolean {
        var isEmpty = false
        for (text in texts) {
            isEmpty = isEmpty<S>(text)
        }
        return isEmpty
    }

    fun isEmpties(vararg values: String?): Boolean {
        var isEmpty = false
        for (value in values) {
            isEmpty = isEmpty(value)
        }
        return isEmpty
    }

    /// 去掉所有空白字符
    fun <S : CharSequence?> to(text: S?): String {
        return if (text == null) "" else to(text.toString())
    }

    fun to(value: String?): String {
        return value?.trim { it <= ' ' } ?: ""
    }

    /// 字符比较
    fun <S : CharSequence?> equals(text: S?, vararg comps: String?): Boolean {
        return if (text == null) false else equals(text.toString(), *comps)
    }

    fun equals(value: String?, vararg comps: String): Boolean {
        if (value == null || comps.isEmpty()) return false
        var equals = false
        for (comp in comps) {
            equals = value == comp
        }
        return equals
    }

    /// 字符转整型
    fun <S : CharSequence?> toInt(text: S): Int {
        return toInt(text.toString())
    }

    fun toInt(str: String?): Int {
        return to(str).toInt(10)
    }

    fun toInt(str: String, radix: Int): Int {
        return str.toInt(radix)
    }

    /// 字符转单精度小数
    fun <S : CharSequence?> toFloat(text: S): Float {
        return toFloat(text.toString())
    }

    fun toFloat(str: String?): Float {
        return to(str).toFloat()
    }

    /// 字符双精度小数
    fun <S : CharSequence?> toDouble(text: S): Double {
        return toDouble(text.toString())
    }

    fun toDouble(str: String?): Double {
        return to(str).toDouble()
    }
}