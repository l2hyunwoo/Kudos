package io.github.l2hyunwoo.main

import io.github.l2hyunwoo.kudos.core.common.date.civilMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

// Pure date/greeting labels for the header — no Composable or theme reads, so they can be computed
// outside composition and unit-tested. The civil-date conversion (core.common.date) lets commonMain
// avoid an extra datetime dependency for the date portion.

// "6월 27일 (금)". Greeting-only header, so the UTC-based day is acceptable.
internal fun todayLabel(): String {
    val epochDays = Clock.System.now().epochSeconds.floorDiv(86_400L)
    val (month, day) = civilMonthDay(epochDays)
    // 1970-01-01 was a Thursday (index 3 in Mon=0 weekday list).
    val weekday = KoreanWeekdays[(epochDays + 3).mod(7L).toInt()]
    return "${month}월 ${day}일 ($weekday)"
}

// Time-of-day greeting from the LOCAL hour. Unlike todayLabel()'s date, this needs the device time
// zone — a UTC hour would greet the wrong part of the day.
internal fun greetingLabel(): String {
    val hour = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
    return when (hour) {
        in 5..10 -> "좋은 아침"
        in 11..13 -> "좋은 낮"
        in 14..17 -> "좋은 오후"
        in 18..22 -> "좋은 저녁"
        else -> "좋은 밤" // 23:00–04:59
    }
}

// Monday-first to match a Korean calendar header.
private val KoreanWeekdays = listOf("월", "화", "수", "목", "금", "토", "일")
