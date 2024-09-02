package com.linchpino.core.entity

import com.linchpino.core.dto.ValidWindow
import com.linchpino.core.enums.RecurrenceType
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

    fun doesMatchesSelectedDay(selectedDay: ZonedDateTime): Boolean {
        return when (recurrenceType) {
            RecurrenceType.DAILY -> matchesDailySchedule(selectedDay)
            RecurrenceType.WEEKLY -> matchesWeeklySchedule(selectedDay)
            RecurrenceType.MONTHLY -> matchesMonthlySchedule(selectedDay)
            else -> false
        }
    }

    private fun matchesDailySchedule(
        selectedTime: ZonedDateTime,
    ): Boolean {
        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val daysBetween = ChronoUnit.DAYS.between(beginningOfStartTimeDay, selectedTime)
        val isValidDay = daysBetween % interval == 0L
        if (!isValidDay) return false
        val validStartTime = startTime?.plusDays(daysBetween)
        return validStartTime?.isAfter(selectedTime) ?: false
    }

    private fun timeSlotDaily(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime
    ): ValidWindow? {
        val daysBetween = ChronoUnit.DAYS.between(startTime, startTarget)
        val isValidDay = daysBetween % interval == 0L
        if (!isValidDay) return null
        val validStartTime = startTime?.plusDays(daysBetween)
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return validWindow(validStartTime, validEndTime, startTarget, endTarget)
    }

    private fun matchesWeeklySchedule(
        selectedTime: ZonedDateTime,
    ): Boolean {
        if (!weekDays.contains(selectedTime.dayOfWeek))
            return false

        val firstDayOfFirstWeek = this.startTime?.with(LocalTime.MIN)?.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val firstDayOfSelectedWeek = selectedTime.with(LocalTime.MIN).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val weeksBetween = ChronoUnit.WEEKS.between(firstDayOfFirstWeek, firstDayOfSelectedWeek)
        val isValidWeek = weeksBetween % interval == 0L
        if (!isValidWeek) return false

        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(beginningOfStartTimeDay, selectedTime))
        return validStartTime?.isAfter(selectedTime) ?: false
    }

    private fun timeSlotWeekly(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime
    ): ValidWindow? {
        if (!weekDays.contains(startTarget.dayOfWeek) || !weekDays.contains(endTarget.dayOfWeek))
            return null

        val firstDayOfFirstWeek = this.startTime?.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
        val firstDayOfSelectedWeek = startTarget.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))

        val weeksBetween = ChronoUnit.WEEKS.between(firstDayOfFirstWeek, firstDayOfSelectedWeek)
        val isValidWeek = weeksBetween % interval == 0L
        if (!isValidWeek) return null

        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(this.startTime, startTarget))
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return validWindow(validStartTime, validEndTime, startTarget, endTarget)
    }

    private fun matchesMonthlySchedule(
        selectedTime: ZonedDateTime,
    ): Boolean {
        if (!monthDays.contains(selectedTime.dayOfMonth))
            return false
        val firstDayOfFirstMonth = this.startTime?.withDayOfMonth(1)
        val firstDayOfSelectedMonth = selectedTime.withDayOfMonth(1)

        val monthsBetween = ChronoUnit.MONTHS.between(firstDayOfFirstMonth, firstDayOfSelectedMonth)
        val isValidMonth = monthsBetween % interval == 0L
        if (!isValidMonth)
            return false
        val beginningOfStartTimeDay = this.startTime?.with(LocalTime.MIN)
        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(beginningOfStartTimeDay, selectedTime))
        return validStartTime?.isAfter(selectedTime) ?: false
    }

    private fun timeSlotMonthly(
        startTarget: ZonedDateTime,
        endTarget: ZonedDateTime
    ): ValidWindow? {
        if (!monthDays.contains(endTarget.dayOfMonth) || !monthDays.contains(startTarget.dayOfMonth))
            return null
        val firstDayOfFirstMonth = this.startTime?.withDayOfMonth(1)
        val firstDayOfSelectedMonth = startTarget.withDayOfMonth(1)

        val monthsBetween = ChronoUnit.MONTHS.between(firstDayOfFirstMonth, firstDayOfSelectedMonth)
        val isValidMonth = monthsBetween % interval == 0L
        if (!isValidMonth)
            return null

        val validStartTime = this.startTime?.plusDays(ChronoUnit.DAYS.between(this.startTime, startTarget))
        val validEndTime = validStartTime?.plusMinutes(duration.toLong())

        return validWindow(validStartTime, validEndTime, startTarget, endTarget)
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
}
