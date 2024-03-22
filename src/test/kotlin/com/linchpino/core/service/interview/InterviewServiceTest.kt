package com.linchpino.core.service.interview

import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.InterviewResult
import com.linchpino.core.dto.SilenceAccountRequest
import com.linchpino.core.dto.SilenceAccountResult
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.service.InterviewService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class InterviewServiceTest {
    @Mock
    private lateinit var service: InterviewService

    @Mock
    private lateinit var repository: InterviewRepository

    @Test
    fun `test creating new interview`() {
        // Given
        val silenceAccountRequest = SilenceAccountRequest("john.doe@example.com", 1)
        val silenceAccountResult = SilenceAccountResult(
            "john.doe@example.com",
            AccountStatus.DEACTIVATED
        )
        val interviewRequest = InterviewRequest(1, 1, 2, silenceAccountRequest)
        val interviewResult = InterviewResult(
            1,
            1,
            1,
            1,
            silenceAccountResult
        )

        val captor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        // When
        val result = service.newInterview(interviewRequest)

        // Then
        assertEquals(interviewResult, result)
        verify(repository, times(1)).save(captor.capture())
        val savedInterview = captor.value
        assertEquals(1, savedInterview.jobPosition?.id)
        assertEquals(1, savedInterview.interviewType?.id)
        assertEquals(2, savedInterview.timeSlot?.id)
        assertEquals("john.doe@example.com", savedInterview.jobSeekerAccount?.email)
        assertEquals(AccountStatus.DEACTIVATED, savedInterview.jobSeekerAccount?.status)
    }
}
