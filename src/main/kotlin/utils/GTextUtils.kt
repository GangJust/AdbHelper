package utils

/// 字符串工具类
object GTextUtils {
    /// 空判断
    fun <S : CharSequence?> isEmpty(text: S?): Boolean {
        return text == null || isEmpty(text.toString())
    }

    fun isEmpty(value: String?): Boolean {
        return value == null || value.trim().isEmpty() || value.trim() == "null"
    }

    /// 空数组判断
    fun <S : CharSequence?> isEmpties(vararg texts: S): Boolean {
        if (texts.isEmpty()) return true
        val strings = arrayOfNulls<String>(texts.size)
        for (i in texts.indices) {
            strings[i] = texts[i].toString()
        }
        return isEmpties(*strings)
    }

    fun isEmpties(vararg values: String?): Boolean {
        if (values.isEmpty()) return false
        val booleans = ArrayList<Boolean>() //Boolean列表, 当出现 false 后, 则表示这并不是一个空的字符数组
        for (value in values) {
            booleans.add(isEmpty(value))
        }
        return !booleans.contains(false) //当不包含false, 表示整个数组都是空的
    }

    /// 非空判断
    fun <S : CharSequence?> isNotEmpty(text: S): Boolean {
        return !isEmpty<S>(text)
    }

    fun isNotEmpty(value: String?): Boolean {
        return !isEmpty(value)
    }

    /// 非空数组判断
    fun <S : CharSequence?> isNotEmpties(vararg text: S): Boolean {
        return !isEmpties(*text)
    }

    fun isNotEmpties(vararg values: String?): Boolean {
        return !isEmpties(*values)
    }

    /// 字符包含比较, 与给定字符数组做匹配比较
    fun <S : CharSequence?> contains(text: S?, vararg comps: String?): Boolean {
        return if (text == null) false else contains(text.toString(), *comps)
    }

    fun contains(value: String?, vararg comps: String?): Boolean {
        if (value == null || comps.isEmpty()) return false
        val booleans = ArrayList<Boolean>() //Boolean列表, 当出现 false 后, 则表示这并不是完全匹配
        for (comp in comps) {
            booleans.add(value.contains(comp!!))
        }
        return !booleans.contains(false) //不包含false, 表示全部匹配
    }

    /// 去掉字符串前后的空白字符
    fun <S : CharSequence?> to(text: S): String {
        return if (isEmpty<S>(text)) "" else to(text.toString())
    }

    fun to(value: String): String {
        return if (isEmpty(value)) "" else value.trim()
    }

    /// 去掉字符串中的所有空白字符
    fun <S : CharSequence?> toAll(text: S): String {
        return if (isEmpty<S>(text)) "" else toAll(text.toString())
    }

    fun toAll(value: String): String {
        return if (isEmpty(value)) "" else value.trim().replace("\\s".toRegex(), "")
    }

    /// 判断某个字符串是否是全空白
    fun <S : CharSequence?> isSpace(text: S): Boolean {
        return if (isEmpty<S>(text)) true else isSpace(text.toString())
    }

    fun isSpace(value: String): Boolean {
        if (isEmpty(value)) return true
        val chars = value.toCharArray()
        val booleans = ArrayList<Boolean>()
        for (aChar in chars) {
            booleans.add(Character.isWhitespace(aChar))
        }
        return !booleans.contains(false) //不包含false
    }

    /// 字符转整型
    fun <S : CharSequence?> toInt(text: S): Int {
        return toInt(text.toString())
    }

    fun toInt(str: String): Int {
        return to(str).toInt(10)
    }

    fun toInt(str: String, radix: Int): Int {
        return str.toInt(radix)
    }

    /// 字符转单精度小数
    fun <S : CharSequence?> toFloat(text: S): Float {
        return toFloat(text.toString())
    }

    fun toFloat(str: String): Float {
        return to(str).toFloat()
    }

    /// 字符双精度小数
    fun <S : CharSequence?> toDouble(text: S): Double {
        return toDouble(text.toString())
    }

    fun toDouble(str: String): Double {
        return to(str).toDouble()
    }
}