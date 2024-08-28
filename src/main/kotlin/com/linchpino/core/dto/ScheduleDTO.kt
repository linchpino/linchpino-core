package com.linchpino.core.dto

import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.RecurrenceType
import java.time.DayOfWeek
import java.time.ZonedDateTime

data class ValidWindow(val start: ZonedDateTime, val end: ZonedDateTime)

data class ScheduleRequest(
    val startTime: ZonedDateTime,
    val duration: Int,
    val recurrenceType: RecurrenceType,
    val interval: Int,
    val endTime: ZonedDateTime,
    val weekDays: List<DayOfWeek> = listOf(),
    val monthDays: List<Int> = listOf()
)

fun ScheduleRequest.toSchedule(account: Account): Schedule {
    val schedule = Schedule()
    schedule.startTime = this.startTime
    schedule.duration = this.duration
    schedule.recurrenceType = this.recurrenceType
    schedule.interval = this.interval
    schedule.endTime = this.endTime
    if (this.recurrenceType == RecurrenceType.WEEKLY){
        schedule.weekDays.addAll(this.weekDays)
    }
    if (this.recurrenceType == RecurrenceType.MONTHLY) {
        schedule.monthDays.addAll(this.monthDays)
    }

    schedule.account = account
    return schedule
}

data class ScheduleResponse(
    val id: Long?,
    val startTime: ZonedDateTime?,
    val duration: Int?,
    val accountId: Long?,
    val recurrenceType: RecurrenceType?,
    val interval: Int?,
    val endTime: ZonedDateTime?,
    val weekDays: MutableList<DayOfWeek> = mutableListOf(),
    val monthDays: MutableList<Int> = mutableListOf(),
)

fun Schedule.toResponse() = ScheduleResponse(
    id,
    startTime,
    duration,
    account?.id,
    recurrenceType,
    interval,
    endTime,
    weekDays,
    monthDays,
)
