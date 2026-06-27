package io.github.l2hyunwoo.tasks

// Human-readable due-date labels (DESIGN_SYSTEM_LUNAR "2일 지남 · KUDOS-88" style), derived from the
// civil-date / epoch-day machinery in TaskGroup.kt so commonMain needs no datetime dependency. The
// helpers here are pure (no Composable/theme reads) so callers can compute labels outside composition.

// Korean weekday-agnostic relative/absolute label for a raw due date against today (both ISO strings).
//
// dueDate is a free-text ISO-8601 timestamp ("2026-06-29T07:34:43.81981+00:00") or a date-only
// "YYYY-MM-DD"; only the first 10 chars (the civil date) drive the label. todayIso() supplies today.
//
// Relative window favors the short forms users actually scan for; everything outside ±1/+6 days falls
// back to an absolute "M월 d일" (with year when it differs from today).
//   delta == 0  -> "오늘"
//   delta == 1  -> "내일"
//   delta == -1 -> "어제"
//   delta < -1  -> "${-delta}일 지남"   (overdue)
//   delta 2..6  -> "${delta}일 후"
//   otherwise   -> "M월 d일"  (or "yyyy년 M월 d일" across years)
//
// Malformed input (length < 10 or a non-numeric date portion) degrades to the first 10 chars rather
// than throwing, so a bad timestamp never crashes the list.
fun formatDueLabel(dueDate: String, todayIso: String): String {
    val dueDay = dueDate.dueDayPortion()
    val due = parseIsoDate(dueDay) ?: return dueDay
    val today = parseIsoDate(todayIso.dueDayPortion()) ?: return dueDay

    val delta = daysFromCivil(due.year, due.month, due.day) - daysFromCivil(today.year, today.month, today.day)
    return when {
        delta == 0L -> "오늘"
        delta == 1L -> "내일"
        delta == -1L -> "어제"
        delta < -1L -> "${-delta}일 지남"
        delta in 2L..6L -> "${delta}일 후"
        due.year == today.year -> "${due.month}월 ${due.day}일"
        else -> "${due.year}년 ${due.month}월 ${due.day}일"
    }
}

// True when the due day is strictly before today (used by callers to tint overdue labels red). Equal
// or non-parseable dates are not overdue.
fun isOverdue(dueDate: String, todayIso: String): Boolean {
    val due = parseIsoDate(dueDate.dueDayPortion()) ?: return false
    val today = parseIsoDate(todayIso.dueDayPortion()) ?: return false
    return daysFromCivil(due.year, due.month, due.day) < daysFromCivil(today.year, today.month, today.day)
}

// Parsed civil date; private to this file (the public surface is the two String-based helpers above).
private data class CivilDate(val year: Long, val month: Long, val day: Long)

// The "YYYY-MM-DD" prefix of an ISO-8601 string: first 10 chars when long enough, else the whole
// string. Mirrors TaskGroup.dueDay() (private there), kept local so this file stands alone.
private fun String.dueDayPortion(): String = if (length >= 10) substring(0, 10) else this

// Strictly parses a "YYYY-MM-DD" head: needs length >= 10, dashes at [4] and [7], and numeric Y/M/D.
// Returns null on any deviation so callers can fall back to the raw text instead of crashing.
private fun parseIsoDate(s: String): CivilDate? {
    if (s.length < 10 || s[4] != '-' || s[7] != '-') return null
    val year = s.substring(0, 4).toLongOrNull() ?: return null
    val month = s.substring(5, 7).toLongOrNull() ?: return null
    val day = s.substring(8, 10).toLongOrNull() ?: return null
    if (month !in 1L..12L || day !in 1L..31L) return null
    return CivilDate(year, month, day)
}

// Howard Hinnant's days_from_civil: (year, month, day) -> days since the Unix epoch (inverse of the
// civil_from_days used by todayIso()/civilMonthDay()). Lets us take an exact signed day delta for the
// "N일 지남"/"N일 후" labels instead of only a lexicographic comparison.
private fun daysFromCivil(year: Long, month: Long, day: Long): Long {
    val y = if (month <= 2L) year - 1L else year
    val era = (if (y >= 0L) y else y - 399L) / 400L
    val yoe = y - era * 400L
    val doy = (153L * (if (month > 2L) month - 3L else month + 9L) + 2L) / 5L + day - 1L
    val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
    return era * 146_097L + doe - 719_468L
}
