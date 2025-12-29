package neth.iecal.trease.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Long.getDayLabel(): String {
    val date = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    // Returns "Mon", "Tue", etc.
    return date.dayOfWeek.name.take(3)
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

fun Long.getHourOfDay(): Int {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
}