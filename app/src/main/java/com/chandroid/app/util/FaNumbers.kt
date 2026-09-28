package com.chandroid.app.util

val FA_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toFaDigits(): String = map { c ->
    if (c in '0'..'9') FA_DIGITS[c - '0'] else c
}.joinToString("")

/** 125000 -> ۱۲۵٬۰۰۰ */
fun Long.toFaToman(): String {
    val s = this.toString()
    val negative = s.startsWith("-")
    val digits = if (negative) s.drop(1) else s
    val buf = StringBuilder()
    var count = 0
    for (i in digits.length - 1 downTo 0) {
        buf.append(digits[i])
        count++
        if (count % 3 == 0 && i != 0) buf.append('٬')
    }
    if (negative) buf.append('-')
    return buf.reverse().toString().toFaDigits()
}

/** 2.07 -> ٪۲٫۰۷ */
fun Double.toFaPercent(): String {
    val rounded = kotlin.math.round(kotlin.math.abs(this) * 100) / 100.0
    val s = rounded.toString().trimEnd('0').trimEnd('.')
    return "٪" + s.map { c ->
        when {
            c in '0'..'9' -> FA_DIGITS[c - '0']
            c == '.' -> '٫'
            else -> c
        }
    }.joinToString("")
}

/** برای نتایج تبدیل: اعداد بزرگ با جداکننده، اعداد کوچک با اعشار */
fun Double.toFaSmart(): String {
    if (this >= 1000.0 || this == 0.0) return this.toLong().toFaToman()
    val s = "%.4f".format(java.util.Locale.US, this).trimEnd('0').trimEnd('.')
    return s.toFaDigits().replace('.', '٫')
}
