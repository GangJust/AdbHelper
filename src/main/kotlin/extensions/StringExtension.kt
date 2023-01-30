package extensions

import java.lang.IndexOutOfBoundsException
import java.nio.charset.Charset

/// 获取某个字符串中间的文本, 失败返回空字符串
fun String.middle(start: String, end: String, reverse: Boolean = false): String {
    if (this.isEmpty() || !this.contains(start) || !this.contains(end)) return ""

    val startIndex = this.indexOf(start) + start.length
    val endIndex = if (reverse) this.lastIndexOf(end) else this.indexOf(end)
    if (end.isEmpty()) {
        return this.substring(startIndex)
    }
    return this.substring(startIndex, endIndex)
}

fun String.left(end: String): String {
    if (this.isEmpty() || !this.contains(end)) return ""

    val endIndex = this.indexOf(end)
    return this.substring(0, endIndex)
}

fun String.right(start: String, reverse: Boolean = false): String {
    if (this.isEmpty() || !this.contains(start)) return ""

    val startIndex = (if (reverse) this.lastIndexOf(start) else this.indexOf(start)) + start.length
    return this.substring(startIndex)
}

// index == -1 表示取最后一个
fun String.regexFind(regex: Regex, index: Int): String {
    val find = this.regexFind(regex)
    if (find.isEmpty()) return ""
    if (find.size <= index) throw IndexOutOfBoundsException("groupValues.size = ${find.size}, index = $index; indexOutOfBounds!!")
    if (index == -1) return find[find.size - 1]
    return find[index]
}

fun String.regexFind(regex: Regex): Array<String> {
    val find = regex.find(this) ?: return emptyArray()
    return find.groupValues.toTypedArray()
}

fun String.encodingTo(charset: Charset): String {
    return String(this.toByteArray(), charset)
}