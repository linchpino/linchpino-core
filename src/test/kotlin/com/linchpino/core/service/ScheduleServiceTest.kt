package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.ScheduleRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.ScheduleRepository
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.security.email
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataIntegrityViolationException
import java.time.DayOfWeek
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class ScheduleServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @Mock
    private lateinit var mentorTimeSlotRepository: MentorTimeSlotRepository

    @InjectMocks
    private lateinit var scheduleService: ScheduleService

    @Test
    fun `test add schedule saves schedule`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication()
        val account = Account().apply {
            email = authentication.email()
            firstName = "John"
            lastName = "Doe"
        }
        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:45+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        val scheduleCaptor: ArgumentCaptor<Schedule> = ArgumentCaptor.forClass(Schedule::class.java)

        `when`(accountRepository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)

        // When
        val result = scheduleService.addSchedule(request, authentication)

        // Then
        verify(scheduleRepository).save(scheduleCaptor.captureNonNullable())
        val s = scheduleCaptor.value

        assertThat(s.startTime).isEqualTo(request.startTime)
        assertThat(s.duration).isEqualTo(request.duration)
        assertThat(s.endTime).isEqualTo(request.endTime)
        assertThat(s.recurrenceType).isEqualTo(request.recurrenceType)
        assertThat(s.weekDays).isEqualTo(request.weekDays)
        assertThat(s.interval).isEqualTo(request.interval)


        assertThat(result.startTime).isEqualTo(request.startTime)
        assertThat(result.duration).isEqualTo(request.duration)
        assertThat(result.endTime).isEqualTo(request.endTime)
        assertThat(result.recurrenceType).isEqualTo(request.recurrenceType)
        assertThat(result.weekDays).isEqualTo(request.weekDays)
        assertThat(result.interval).isEqualTo(request.interval)
    }

    @Test
    fun `test add schedule throws exception if account not found`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication()
        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:45+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        `when`(accountRepository.findByEmailIgnoreCase(authentication.email())).thenReturn(null)

        // When
        val ex = Assertions.assertThrows(LinchpinException::class.java) {
            scheduleService.addSchedule(request, authentication)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `test add schedule throws exception if requested timeslot is already booked`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication()
        val account = Account().apply {
            email = authentication.email()
            firstName = "John"
            lastName = "Doe"
        }
        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:45+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        `when`(accountRepository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)
        `when`(scheduleRepository.save(any())).thenThrow(DataIntegrityViolationException::class.java)

        // When & Then
        val ex = Assertions.assertThrows(LinchpinException::class.java) {
            scheduleService.addSchedule(request, authentication)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.DUPLICATE_SCHEDULE)
    }

    @Test
    fun `test available time slot saves and returns time slot`() {
        // Given
        val request = CreateInterviewRequest(
            1,
            1,
            ZonedDateTime.parse("2024-09-18T12:30:45+03:00"),
            ZonedDateTime.parse("2024-09-18T13:30:45+03:00"),
            1,
            "jane.smith@example.com"
        )
        // note: schedule interval is 3 weeks and requested time is on same day of the third week => it is valid
        val schedule = Schedule().apply {
            id = 1
            startTime = ZonedDateTime.parse("2024-08-28T12:30:45+03:00")
            endTime = ZonedDateTime.parse("2024-12-30T13:30:45+03:00")
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            interval = 3
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        }
        val account = Account().apply {
            email = "john.doe@example.com"
            firstName = "John"
            lastName = "Doe"
            this.schedule = schedule
        }
        val timeSlotCaptor:ArgumentCaptor<MentorTimeSlot> = ArgumentCaptor.forClass(MentorTimeSlot::class.java)
        `when`(mentorTimeSlotRepository.save(any())).thenReturn(MentorTimeSlot())

        // When
        scheduleService.availableTimeSlot(account, request)

        // Then
        verify(mentorTimeSlotRepository, times(1)).save(timeSlotCaptor.captureNonNullable())
        val timeSlot = timeSlotCaptor.value
        assertThat(timeSlot.account).isEqualTo(account)
        assertThat(timeSlot.fromTime).isEqualTo(request.startTime)
        assertThat(timeSlot.toTime).isEqualTo(request.endTime)
        assertThat(timeSlot.status).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
    }

    @Test
    fun `test available time slot throws exception if requested time does not lie on mentor schedule`() {
        // Given
        val request = CreateInterviewRequest(
            1,
            1,
            ZonedDateTime.parse("2024-09-11T12:30:45+03:00"),
            ZonedDateTime.parse("2024-09-11T13:30:45+03:00"),
            1,
            "jane.smith@example.com"
        )
        // note: schedule interval is 3 weeks and requested time is on same day of the 2nd week => it is NOT valid
        val schedule = Schedule().apply {
            id = 1
            startTime = ZonedDateTime.parse("2024-08-28T12:30:45+03:00")
            endTime = ZonedDateTime.parse("2024-12-30T13:30:45+03:00")
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            interval = 3
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        }
        val account = Account().apply {
            email = "john.doe@example.com"
            firstName = "John"
            lastName = "Doe"
            this.schedule = schedule
        }

        // When
        val ex = Assertions.assertThrows(LinchpinException::class.java){
            scheduleService.availableTimeSlot(account, request)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INVALID_TIMESLOT)
    }


    @Test
    fun `test available time slot throws exception if requested time slot is already booked`(){
        // Given
        val request = CreateInterviewRequest(
            1,
            1,
            ZonedDateTime.parse("2024-09-18T12:30:45+03:00"),
            ZonedDateTime.parse("2024-09-18T13:30:45+03:00"),
            1,
            "jane.smith@example.com"
        )
        val schedule = Schedule().apply {
            id = 1
            startTime = ZonedDateTime.parse("2024-08-28T12:30:45+03:00")
            endTime = ZonedDateTime.parse("2024-12-30T13:30:45+03:00")
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            interval = 3
            weekDays = mutableListOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        }
        val account = Account().apply {
            email = "john.doe@example.com"
            firstName = "John"
            lastName = "Doe"
            this.schedule = schedule
        }

        `when`(mentorTimeSlotRepository.numberOfOverlappingSlots(request.startTime,request.endTime)).thenReturn(1)

        // When & Then
        val ex = Assertions.assertThrows(LinchpinException::class.java){
            scheduleService.availableTimeSlot(account, request)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TIMESLOT_IS_BOOKED)
    }

}
