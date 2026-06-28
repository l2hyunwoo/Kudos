package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.kudos.core.common.date.daysFromCivil
import io.github.l2hyunwoo.kudos.core.common.date.isoFromEpochDay

// Due-date presets for the Create-Task sheet. Pure logic (no Compose/theme reads) so the mapping to a
// stored ISO date can be computed and unit-tested outside composition. Reuses the shared civil-date /
// epoch-day conversions in core.common.date (daysFromCivil + isoFromEpochDay) so commonMain still
// needs no datetime dependency and the conversion math has one copy.
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

// The ISO date of the Sunday that ends the current (Monday-start) week, given today as "YYYY-MM-DD".
// Falls back to the input on a malformed date so a bad value never throws here.
private fun upcomingSundayIso(today: String): String {
    val day = if (today.length >= 10) today.substring(0, 10) else today
    if (day.length < 10 || day[4] != '-' || day[7] != '-') return today
    val year = day.substring(0, 4).toLongOrNull() ?: return today
    val month = day.substring(5, 7).toLongOrNull() ?: return today
    val date = day.substring(8, 10).toLongOrNull() ?: return today

    val epochDay = daysFromCivil(year, month, date)
    // Monday-start day-of-week index (Mon=0 .. Sun=6). 1970-01-01 (epoch day 0) was a Thursday, which
    // is Monday-index 3: ((0 % 7) + 10) % 7 == 3. Sunday is index 6, so daysUntilSunday = 6 - dow
    // (0 when today is already Sunday => returns today).
    val dow = ((epochDay % 7L) + 10L) % 7L
    val daysUntilSunday = 6L - dow
    return isoFromEpochDay(epochDay + daysUntilSunday)
}
