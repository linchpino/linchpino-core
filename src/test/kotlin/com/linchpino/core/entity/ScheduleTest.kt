package com.linchpino.core.entity

import com.linchpino.core.enums.RecurrenceType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.ZonedDateTime

class ScheduleTest{


    @Test
    fun `test daily schedule returns valid window`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }

        val targetStart = start.plusDays(4)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window?.start).isEqualToIgnoringSeconds(targetStart)
        assertThat(window?.end).isEqualToIgnoringSeconds(targetEnd)
    }

    @Test
    fun `test daily schedule returns null when requested date does not match the interval`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }

        val targetStart = start.plusDays(3)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test daily schedule returns null when requested date matches the interval but time does not match`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }

        val offset = 5 // 5 min
        val targetStart = start.plusDays(6)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong().plus(offset))

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test weekly schedule returns valid window`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY,DayOfWeek.FRIDAY)
        }

        val targetStart = start.plusWeeks(4)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window?.start).isEqualToIgnoringSeconds(targetStart)
        assertThat(window?.end).isEqualToIgnoringSeconds(targetEnd)
    }

    @Test
    fun `test weekly schedule returns null when requested date does not match the interval`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY,DayOfWeek.FRIDAY)
        }

        // note: target starts 5 weeks after start =>  it does not match two-week intervals

        val targetStart = start.plusWeeks(5)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test weekly schedule returns null when requested date matches the interval but the day is not in configured week days`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY,DayOfWeek.FRIDAY)
        }

        // note: target starts 6 weeks after start =>  it does not match two-week intervals but the day is THURSDAY
        val targetStart = start.plusWeeks(6).plusDays(1) // THURSDAY
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test weekly schedule returns null when requested date matches the interval and correct day but time does not match`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY,DayOfWeek.FRIDAY)
        }

        val offset = 10 // 10 min offset from valid end
        // note: target starts 6 weeks after start =>  it does not match two-week intervals
        val targetStart = start.plusWeeks(6)
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong().plus(offset))

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test monthly schedule returns valid window`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(20,24)
        }

        val targetStart = ZonedDateTime.parse("2024-10-24T12:30:00+03:00")
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window?.start).isEqualToIgnoringSeconds(targetStart)
        assertThat(window?.end).isEqualToIgnoringSeconds(targetEnd)
    }

    @Test
    fun `test monthly schedule returns null when requested date does not match the interval`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(20,24)
        }

        val targetStart = ZonedDateTime.parse("2024-11-24T12:30:00+03:00") // The interval is
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test monthly schedule returns null when requested date matches the interval but the day is not in configured months days`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(20,24)
        }

        val targetStart = ZonedDateTime.parse("2024-11-21T12:30:00+03:00") // The interval is
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong())

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }

    @Test
    fun `test monthly schedule returns null when requested date matches the interval and correct day but time does not match`(){
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(20,24)
        }

        val offset = 15 // 15 min
        val targetStart = ZonedDateTime.parse("2024-11-24T12:30:00+03:00") // The interval is
        val targetEnd = targetStart.plusMinutes(schedule.duration.toLong().plus(offset))

        // When
        val window = schedule.timeSlot(targetStart, targetEnd)

        // Then
        assertThat(window).isNull()
    }
}
