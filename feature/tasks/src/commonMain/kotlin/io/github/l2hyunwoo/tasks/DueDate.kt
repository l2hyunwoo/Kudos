package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.kudos.core.common.date.daysFromCivil

// Human-readable due-date labels (DESIGN_SYSTEM_LUNAR "2일 지남 · KUDOS-88" style), derived from the
// shared civil-date / epoch-day conversions in core.common.date so commonMain needs no datetime
// dependency. The helpers here are pure (no Composable/theme reads) so callers can compute labels
// outside composition.

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
fun formatDueLabel(
    dueDate: String,
    todayIso: String,
): String {
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
fun isOverdue(
    dueDate: String,
    todayIso: String,
): Boolean {
    val due = parseIsoDate(dueDate.dueDayPortion()) ?: return false
    val today = parseIsoDate(todayIso.dueDayPortion()) ?: return false
    return daysFromCivil(due.year, due.month, due.day) < daysFromCivil(today.year, today.month, today.day)
}

// Parsed civil date; private to this file (the public surface is the two String-based helpers above).
// Named distinctly from core.common.date.CivilDate, which it feeds into daysFromCivil().
private data class ParsedIsoDate(
    val year: Long,
    val month: Long,
    val day: Long,
)

// The "YYYY-MM-DD" prefix of an ISO-8601 string: first 10 chars when long enough, else the whole
// string. Mirrors TaskGroup.dueDay() (private there), kept local so this file stands alone.
private fun String.dueDayPortion(): String = if (length >= 10) substring(0, 10) else this

// Strictly parses a "YYYY-MM-DD" head: needs length >= 10, dashes at [4] and [7], and numeric Y/M/D.
// Returns null on any deviation so callers can fall back to the raw text instead of crashing.
private fun parseIsoDate(s: String): ParsedIsoDate? {
    if (s.length < 10 || s[4] != '-' || s[7] != '-') return null
    val year = s.substring(0, 4).toLongOrNull() ?: return null
    val month = s.substring(5, 7).toLongOrNull() ?: return null
    val day = s.substring(8, 10).toLongOrNull() ?: return null
    if (month !in 1L..12L || day !in 1L..31L) return null
    return ParsedIsoDate(year, month, day)
}
