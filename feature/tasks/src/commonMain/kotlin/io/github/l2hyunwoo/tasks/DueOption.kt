package io.github.l2hyunwoo.tasks

// Due-date presets for the Create-Task sheet. Pure logic (no Compose/theme reads) so the mapping to a
// stored ISO date can be computed and unit-tested outside composition. Carries its own civil-date /
// epoch-day conversions (Howard Hinnant's algorithm) so commonMain still needs no datetime dependency
// and this stays self-contained.
enum class DueOption { TODAY, THIS_WEEK, PICK, NONE }

// Maps a selected [DueOption] to the ISO "YYYY-MM-DD" date stored on CreateTaskRequest.dueDate.
//  - NONE      -> null (no due date)
//  - TODAY     -> today's civil date
//  - THIS_WEEK -> the upcoming Sunday (end of a Monday-start week; today if today is already Sunday)
//  - PICK      -> the user-picked date ([pickedIso]); null until one is chosen
//
// [today] defaults to todayIso() but is injectable for testing.
fun dueOptionToIso(
    option: DueOption,
    pickedIso: String?,
    today: String = todayIso(),
): String? = when (option) {
    DueOption.NONE -> null
    DueOption.TODAY -> today
    DueOption.THIS_WEEK -> upcomingSundayIso(today)
    DueOption.PICK -> pickedIso
}

// Converts an epoch day (days since 1970-01-01) to an ISO "YYYY-MM-DD" date. internal so the sheet can
// reuse it for the Material DatePicker result (selectedDateMillis -> epoch day -> ISO), keeping one
// copy of this conversion across the due-date feature.
internal fun isoFromEpochDayDue(epochDay: Long): String {
    val z = epochDay + 719_468L
    val era = (if (z >= 0) z else z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1460L + doe / 36_524L - doe / 146_096L) / 365L
    val year = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (if (mp < 10L) mp + 3L else mp - 9L).toInt()
    val civilYear = (if (month <= 2) year + 1L else year).toInt()
    return "${pad4(civilYear)}-${pad2(month)}-${pad2(day)}"
}

// The ISO date of the Sunday that ends the current (Monday-start) week, given today as "YYYY-MM-DD".
// Falls back to the input on a malformed date so a bad value never throws here.
private fun upcomingSundayIso(today: String): String {
    val day = if (today.length >= 10) today.substring(0, 10) else today
    if (day.length < 10 || day[4] != '-' || day[7] != '-') return today
    val year = day.substring(0, 4).toLongOrNull() ?: return today
    val month = day.substring(5, 7).toLongOrNull() ?: return today
    val date = day.substring(8, 10).toLongOrNull() ?: return today

    val epochDay = epochDayFromCivil(year, month, date)
    // Monday-start day-of-week index (Mon=0 .. Sun=6). 1970-01-01 (epoch day 0) was a Thursday, which
    // is Monday-index 3: ((0 % 7) + 10) % 7 == 3. Sunday is index 6, so daysUntilSunday = 6 - dow
    // (0 when today is already Sunday => returns today).
    val dow = ((epochDay % 7L) + 10L) % 7L
    val daysUntilSunday = 6L - dow
    return isoFromEpochDayDue(epochDay + daysUntilSunday)
}

// Howard Hinnant's days_from_civil: (year, month, day) -> days since 1970-01-01. Inverse of
// isoFromEpochDayDue. Local copy so this file is independent of the label helpers in DueDate.kt.
private fun epochDayFromCivil(year: Long, month: Long, day: Long): Long {
    val y = if (month <= 2L) year - 1L else year
    val era = (if (y >= 0L) y else y - 399L) / 400L
    val yoe = y - era * 400L
    val doy = (153L * (if (month > 2L) month - 3L else month + 9L) + 2L) / 5L + day - 1L
    val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
    return era * 146_097L + doe - 719_468L
}

private fun pad2(v: Int): String = if (v < 10) "0$v" else v.toString()

private fun pad4(v: Int): String = v.toString().padStart(4, '0')
