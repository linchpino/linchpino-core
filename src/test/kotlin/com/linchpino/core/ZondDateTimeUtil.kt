package com.linchpino.core

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun String.withZone(defaultZone:String):String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.of(defaultZone))
    return ZonedDateTime.parse(this).format(formatter)
}

fun ZonedDateTime.withZone(defaultZone:String):String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.of(defaultZone))
    return this.format(formatter)
}
