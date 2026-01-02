package neth.iecal.trease.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

fun Long.getDayLabel(): String {
    val date = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    // Returns "Mon", "Tue", etc.
    return date.dayOfWeek.name.take(3)
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

fun Long.getDate(): String {
    val date = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return date.format(
        LocalDateTime.Format {
            dayOfMonth(padding = Padding.ZERO)
            char('/')
            monthNumber(padding = Padding.ZERO)
            char('/')
            yearTwoDigits(baseYear = 2000)
        }
    )
}

fun getCurrentDate(): String {
    return Clock.System.now()
        .toEpochMilliseconds()
        .getDate()
}

fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

fun Long.getHourOfDay(): Int {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
}


fun getCurrentMonthYear(): String {
    val currentMoment = Clock.System.now()
    val datetime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

    val isoFormat = "${datetime.year}-${datetime.month.number.toString().padStart(2, '0')}"

    return isoFormat
}