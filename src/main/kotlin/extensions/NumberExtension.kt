package extensions

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

fun Int.forIndex(block: (i: Int) -> Unit) {
    if (this == 0) return

    if (this < 0) {
        for (m in 0..this.absoluteValue) {
            block.invoke(-m)
        }
    } else {
        for (m in 0..this.absoluteValue) {
            block.invoke(m)
        }
    }
}

fun Long.toFileLength(): String {
    val kb = this / 1024.0f
    val mb = kb / 1024.0f
    val gb = mb / 1024.0f
    val tb = gb / 1024.0f

    val format = DecimalFormat("0.##")
    format.roundingMode = RoundingMode.HALF_UP

    return if (this < 0) {
        "-1"
    } else if (kb.toInt() == 0) {
        format.format(this).plus("B")
    } else if (mb.toInt() == 0) {
        format.format(kb).plus("KB")
    } else if (gb.toInt() == 0) {
        format.format(mb).plus("MB")
    } else if (tb.toInt() == 0) {
        format.format(gb).plus("GB")
    } else {
        format.format(tb).plus("TB")
    }
}