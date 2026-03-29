package com.xommore.freelancerapp.data

expect fun currentTimeMillis(): Long

fun currentYear(): Int = getYear(currentTimeMillis())
fun currentMonth(): Int = getMonth(currentTimeMillis())

fun getYear(millis: Long): Int {
    val days = (millis / 86400000L).toInt() + 719468
    val era = (if (days >= 0) days else days - 146096) / 146097
    val doe = days - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    return yoe + era * 400
}

fun getMonth(millis: Long): Int {
    val days = (millis / 86400000L).toInt() + 719468
    val era = (if (days >= 0) days else days - 146096) / 146097
    val doe = days - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val m = mp + (if (mp < 10) 3 else -9)
    return m
}

fun getDay(millis: Long): Int {
    val days = (millis / 86400000L).toInt() + 719468
    val era = (if (days >= 0) days else days - 146096) / 146097
    val doe = days - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    return d
}