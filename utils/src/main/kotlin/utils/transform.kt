package utils

import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Instant

fun LocalDateTime.toUtcInstant(): Instant = Instant.fromEpochMilliseconds(this.toInstant(ZoneOffset.UTC).toEpochMilli())
