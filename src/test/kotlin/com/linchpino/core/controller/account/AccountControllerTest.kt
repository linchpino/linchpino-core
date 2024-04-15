package com.linchpino.core.controller.account

import com.linchpino.core.captureNonNullable
import com.linchpino.core.controller.AccountController
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.service.AccountService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AccountControllerTest {

    @Mock
    private lateinit var accountService: AccountService

    @InjectMocks
    private lateinit var accountController: AccountController

    @Test
    fun `test create account`() {
        // Given
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)
        val expectedResponse = CreateAccountResult(
            1,
            "John",
            "Doe",
            "john.doe@example.com",
            AccountTypeEnum.JOB_SEEKER
        )

        `when`(accountService.createAccount(createAccountRequest)).thenReturn(expectedResponse)

        // When
        val result = accountController.createAccount(createAccountRequest)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }

    @Test
    fun `test search mentors by interviewTypeId and date`(){
        // Given
        val interviewTypeId = 5L
        val date = ZonedDateTime.parse("2024-03-29T12:30:45+00:00")

        val expectedResponse = listOf(
            MentorWithClosestTimeSlot(interviewTypeId,"John","Doe",3,ZonedDateTime.parse("2024-03-29T13:00:00+00:00"),ZonedDateTime.parse("2024-03-29T14:00:00+00:00"))
        )
        val idCaptor:ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        val dateCaptor:ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        `when`(accountService.findMentorsWithClosestTimeSlotsBy(dateCaptor.captureNonNullable(), idCaptor.capture())).thenReturn(expectedResponse)

        // When
        val result = accountController.findMentorsByInterviewTypeAndDate(interviewTypeId,date)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(expectedResponse)
        assertThat(dateCaptor.value).isEqualTo(date)
        assertThat(idCaptor.value).isEqualTo(interviewTypeId)
        verify(accountService, times(1)).findMentorsWithClosestTimeSlotsBy(date,interviewTypeId)
    }
}
