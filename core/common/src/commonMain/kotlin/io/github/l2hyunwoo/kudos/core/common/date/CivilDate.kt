package io.github.l2hyunwoo.kudos.core.common.date

// Howard Hinnant's civil-date / epoch-day conversions, the single source for the whole app. Pure
// integer math (no Composable/theme reads, no datetime dependency) so callers can compute dates
// outside composition and unit-test them. Feature modules layer their own Korean label formatting on
// top of these conversions.
//
// Algorithm: http://howardhinnant.github.io/date_algorithms.html
// The variable names (z/era/doe/yoe/doy/mp) are the canonical ones from that reference; kept verbatim
// so the math stays cross-checkable against the source.

// A civil (proleptic Gregorian) calendar date. Months and days are 1-based.
data class CivilDate(
    val year: Long,
    val month: Int,
    val day: Int,
)

// civil_from_days: epoch day count (days since 1970-01-01) -> civil (year, month, day).
fun civilFromDays(epochDay: Long): CivilDate {
    val z = epochDay + 719_468L
    val era = (if (z >= 0) z else z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1460L + doe / 36_524L - doe / 146_096L) / 365L
    val y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (if (mp < 10L) mp + 3L else mp - 9L).toInt()
    // March-based year (mp) rolls Jan/Feb into the prior calendar year, so add one back for them.
    val year = if (month <= 2) y + 1L else y
    return CivilDate(year, month, day)
}

// days_from_civil: civil (year, month, day) -> epoch day count. Inverse of [civilFromDays].
fun daysFromCivil(
    year: Long,
    month: Long,
    day: Long,
): Long {
    val y = if (month <= 2L) year - 1L else year
    val era = (if (y >= 0L) y else y - 399L) / 400L
    val yoe = y - era * 400L
    val doy = (153L * (if (month > 2L) month - 3L else month + 9L) + 2L) / 5L + day - 1L
    val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
    return era * 146_097L + doe - 719_468L
}

// epoch day -> "YYYY-MM-DD". Convenience over [civilFromDays] for callers that store/compare ISO dates.
fun isoFromEpochDay(epochDay: Long): String {
    val date = civilFromDays(epochDay)
    return "${pad4(date.year.toInt())}-${pad2(date.month)}-${pad2(date.day)}"
}

// epoch day -> (month, day-of-month), discarding the year. For headers that show only "M월 d일".
fun civilMonthDay(epochDay: Long): Pair<Int, Int> {
    val date = civilFromDays(epochDay)
    return date.month to date.day
}

private fun pad2(v: Int): String = if (v < 10) "0$v" else v.toString()

private fun pad4(v: Int): String = v.toString().padStart(4, '0')
