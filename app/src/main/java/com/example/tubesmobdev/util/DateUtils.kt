package com.example.tubesmobdev.util

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatMonthYear(input: String): String {
    return YearMonth.parse(input)
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
}
fun extractMonthAndYear(input: String): Pair<String, String> {
    val yearMonth = YearMonth.parse(input)
    val month = yearMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))
    val year = yearMonth.format(DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH))
    return month to year
}