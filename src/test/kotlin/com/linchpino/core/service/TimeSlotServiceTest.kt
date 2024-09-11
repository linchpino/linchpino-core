package com.linchpino.core.service

import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.TimeSlot
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TimeSlotServiceTest {

    @InjectMocks
    private lateinit var service: TimeSlotService

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var repository: MentorTimeSlotRepository


    @Test
    fun `test adding time slots`() {
        // Given
        val timeSlots = listOf(
            TimeSlot(ZonedDateTime.parse("2024-05-09T12:30:45+03:00"), ZonedDateTime.parse("2024-05-09T13:30:45+03:00")),
            TimeSlot(ZonedDateTime.parse("2024-05-10T12:30:45+03:00"), ZonedDateTime.parse("2024-05-10T13:30:45+03:00")),
        )
        val request = AddTimeSlotsRequest(1L, timeSlots)
        val captor: ArgumentCaptor<List<MentorTimeSlot>> = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<MentorTimeSlot>>
        val account = Account().apply { id = 1L }
        account.addRole(Role().apply { title = AccountTypeEnum.MENTOR })
        `when`(accountRepository.getReferenceById(1)).thenReturn(account)

        // When
        service.addTimeSlots(request)
        Mockito.verify(repository, times(1)).saveAll(captor.capture())
        val mentorTimeSlots = captor.value

        // Then
        assertThat(mentorTimeSlots.size).isEqualTo(2)
        assertThat(mentorTimeSlots.map { it.fromTime }).isEqualTo(timeSlots.map { it.startTime })
        assertThat(mentorTimeSlots.map { it.toTime }).containsExactly(timeSlots[0].endTime, timeSlots[1].endTime)
        assertThat(mentorTimeSlots.all { it.status == MentorTimeSlotEnum.AVAILABLE }).isTrue()
    }

    @Test
    fun `test adding time slots throws exception if account does not have MENTOR role`() {
        // Given
        val timeSlots = listOf(
            TimeSlot(ZonedDateTime.parse("2024-05-09T12:30:45+03:00"), ZonedDateTime.parse("2024-05-09T13:30:45+03:00")),
            TimeSlot(ZonedDateTime.parse("2024-05-10T12:30:45+03:00"), ZonedDateTime.parse("2024-05-10T13:30:45+03:00")),
        )
        val request = AddTimeSlotsRequest(1L, timeSlots)
        val account = Account().apply { id = 1L }

        `when`(accountRepository.getReferenceById(1)).thenReturn(account)

        // Then
        val ex = Assertions.assertThrows(LinchpinException::class.java) {
            service.addTimeSlots(request)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INVALID_ACCOUNT_ROLE)

    }
}
