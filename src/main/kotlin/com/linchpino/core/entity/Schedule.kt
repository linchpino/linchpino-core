package com.linchpino.core.entity

import com.linchpino.core.dto.ScheduleUpdateRequest
import com.linchpino.core.dto.ValidWindow
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Entity
@Table(name = "schedule")
class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var startTime: ZonedDateTime? = null

    @Column(nullable = false)
    var duration: Int = 0

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var recurrenceType: RecurrenceType? = null

    @Column(nullable = false)
    var interval: Int = 0

    @Column(nullable = false)
    var endTime: ZonedDateTime? = null

    @ElementCollection
    @CollectionTable(name = "weekly_recurrence_days", joinColumns = [JoinColumn(name = "schedule_id")])
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    var weekDays: MutableList<DayOfWeek> = mutableListOf()

    @ElementCollection
    @CollectionTable(name = "monthly_recurrence_days", joinColumns = [JoinColumn(name = "schedule_id")])
    @Column(name = "day_of_month", nullable = false)
    var monthDays: MutableList<Int> = mutableListOf()

    fun timeSlot(
        startTime: ZonedDateTime,
        targetTime: ZonedDateTime
    ): ValidWindow? {
        if (targetTime > endTime)
            return null
        return when (recurrenceType) {
            RecurrenceType.DAILY -> timeSlotDaily(startTime, targetTime)
            RecurrenceType.WEEKLY -> timeSlotWeekly(startTime, targetTime)
            RecurrenceType.MONTHLY -> timeSlotMonthly(startTime, targetTime)
            else -> null
        }
    }

    fun doesMatchesSelectedDay(selectedDay: ZonedDateTime): ValidWindow? {
        return when (recurrenceType) {
            RecurrenceType.DAILY -> timeSlotDaily(selectedDay)
            RecurrenceType.WEEKLY -> timeSlotWeekly(selectedDay)
            RecurrenceType.MONTHLY -> timeSlotMonthly(selectedDay)
            else -> null
        }
    }

    private fun timeSlotDaily(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime? = null
    ): ValidWindow? {
        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val daysBetween = ChronoUnit.DAYS.between(beginningOfStartTimeDay, startTarget)
        val isValidDay = daysBetween % interval == 0L
        if (!isValidDay) return null
        val validStartTime = startTime?.plusDays(daysBetween)
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return when {
            endTarget != null -> {
                validWindow(validStartTime, validEndTime, startTarget, endTarget)
            }

            validStartTime != null && validStartTime.isAfter(startTarget) -> {
                ValidWindow(validStartTime, validEndTime!!)
            }

            else -> null
        }

    }

    private fun timeSlotWeekly(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime? = null
    ): ValidWindow? {
        if (!weekDays.contains(startTarget.dayOfWeek))
            return null
        if (endTarget != null && !weekDays.contains(endTarget.dayOfWeek))
            return null
        val firstDayOfFirstWeek =
            this.startTime?.with(LocalTime.MIN)?.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val firstDayOfSelectedWeek =
            startTarget.with(LocalTime.MIN).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val weeksBetween = ChronoUnit.WEEKS.between(firstDayOfFirstWeek, firstDayOfSelectedWeek)
        val isValidWeek = weeksBetween % interval == 0L
        if (!isValidWeek) return null

        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(beginningOfStartTimeDay, startTarget))
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return when {
            endTarget != null -> {
                validWindow(validStartTime, validEndTime, startTarget, endTarget)
            }

            validStartTime != null && validStartTime.isAfter(startTarget) -> {
                ValidWindow(validStartTime, validEndTime!!)
            }

            else -> {
                null
            }
        }
    }

    private fun timeSlotMonthly(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime? = null
    ): ValidWindow? {
        if (!monthDays.contains(startTarget.dayOfMonth))
            return null
        if (endTarget != null && !monthDays.contains(endTarget.dayOfMonth)) {
            return null
        }

        val firstDayOfFirstMonth = this.startTime?.withDayOfMonth(1)
        val firstDayOfSelectedMonth = startTarget.withDayOfMonth(1)

        val monthsBetween = ChronoUnit.MONTHS.between(firstDayOfFirstMonth, firstDayOfSelectedMonth)
        val isValidMonth = monthsBetween % interval == 0L
        if (!isValidMonth)
            return null

        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(beginningOfStartTimeDay, startTarget))
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return when {
            endTarget != null -> {
                validWindow(validStartTime, validEndTime, startTarget, endTarget)
            }

            validStartTime != null && validStartTime.isAfter(startTarget) -> {
                ValidWindow(validStartTime, validEndTime!!)
            }

            else -> {
                null
            }
        }
    }

    private fun validWindow(
        validStartTime: ZonedDateTime?,
        validEndTime: ZonedDateTime?,
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime
    ): ValidWindow? {
        if (validStartTime == null || validEndTime == null)
            return null

        if (startTarget.isBefore(validStartTime) || startTarget.isAfter(validEndTime)) {
            return null
        }
        if (endTarget.isAfter(validEndTime) || endTarget.isBefore(validStartTime)) {
            return null
        }
        return ValidWindow(validStartTime, validEndTime)
    }

    fun validate() {
        if (this.startTime == null || this.endTime == null)
            throw throw LinchpinException(
                ErrorCode.INVALID_STATE,
                "start time and end time must not be null",
                Schedule::class.java.simpleName,
                "start time and end time must not be null"
            )
        if (this.startTime!!.isAfter(this.endTime)) {
            throw LinchpinException(
                ErrorCode.INVALID_STATE,
                "startTime is after endTime",
                Schedule::class.java.simpleName,
                "start time is after endTime"
            )
        }

        when (this.recurrenceType) {
            RecurrenceType.WEEKLY -> {
                if (weekDays.isEmpty()) throw LinchpinException(
                    ErrorCode.INVALID_STATE,
                    "recurrence type is weekly but weekdays is empty",
                    Schedule::class.java.simpleName,
                    "weekDays is empty"
                )
            }

            RecurrenceType.MONTHLY -> {
                if (monthDays.isEmpty()) throw LinchpinException(
                    ErrorCode.INVALID_STATE,
                    "recurrence type is monthly but monthDays is empty",
                    Schedule::class.java.simpleName,
                    "monthDays is empty"
                )
            }

            RecurrenceType.DAILY -> {
                if (monthDays.isNotEmpty() || weekDays.isNotEmpty()) throw LinchpinException(
                    ErrorCode.INVALID_STATE,
                    "recurrence type is daily but monthDays or weekDays are not empty",
                    Schedule::class.java.simpleName,
                    "monthDays or weekDays are not empty"
                )
            }

            else -> throw LinchpinException(
                ErrorCode.INVALID_STATE,
                "recurrence type is null",
                Schedule::class.java.simpleName,
                "recurrence type must not be null"
            )
        }
    }

    fun update(request: ScheduleUpdateRequest): Schedule {
        request.startTime?.let { this.startTime = it }
        request.endTime?.let { this.endTime = it }
        request.duration?.let { this.duration = it }
        request.interval?.let { this.interval = it }

        request.recurrenceType?.let { type ->
            when (type) {
                RecurrenceType.DAILY -> {
                    this.weekDays.clear()
                    this.monthDays.clear()
                }
                RecurrenceType.WEEKLY -> this.monthDays.clear()
                RecurrenceType.MONTHLY -> this.weekDays.clear()
            }
            this.recurrenceType = type
        }

        if (request.weekDays.isNotEmpty() && request.recurrenceType == RecurrenceType.WEEKLY) {
            this.weekDays = request.weekDays.toMutableList()
        }

        if (request.monthDays.isNotEmpty() && request.recurrenceType == RecurrenceType.MONTHLY) {
            this.monthDays = request.monthDays.toMutableList()
        }

        validate()
        return this
    }

}
