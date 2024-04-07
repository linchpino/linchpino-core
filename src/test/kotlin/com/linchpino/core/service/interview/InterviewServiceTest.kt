package com.linchpino.core.service.interview

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.service.InterviewService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class InterviewServiceTest {
    @InjectMocks
    private lateinit var service: InterviewService

    @Mock
    private lateinit var interviewRepo: InterviewRepository

    @Mock
    private lateinit var accountRepo: AccountRepository

    @Mock
    private lateinit var jobPositionRepo: JobPositionRepository

    @Mock
    private lateinit var interviewTypeRepo: InterviewTypeRepository

    @Mock
    private lateinit var timeSlotRepo: MentorTimeSlotRepository

    @Test
    fun `test creating new interview`() {
        // Given
        val jobSeekerAcc = Account().apply {
            id = 1
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
            type = AccountTypeEnum.JOB_SEEKER
        }

        val mentorAcc = Account().apply {
            id = 1
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "Mentor.Mentoriii@example.com"
            password = "password_Mentoriii"
            type = AccountTypeEnum.MENTOR
        }

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 1
            this.account = account
            fromTime = ZonedDateTime.now()
            toTime = ZonedDateTime.now()
            status = MentorTimeSlotEnum.AVAILABLE
        }

        val position = JobPosition().apply {
            id = 1
            title = "Test Job"
        }

        val typeInterview = InterviewType().apply {
            id = 1
            name = "Test Interview Type"
        }

        val createInterviewRequest = CreateInterviewRequest(1, 1, 1, 1, "john.doe@example.com")
        val createInterviewResult = CreateInterviewResult(
            null, 1, 1, 1, 1, "john.doe@example.com"
        )

        val captor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        Mockito.`when`(accountRepo.findByEmail("john.doe@example.com")).thenReturn(jobSeekerAcc)
        Mockito.`when`(accountRepo.getReferenceById(1)).thenReturn(mentorAcc)
        Mockito.`when`(jobPositionRepo.getReferenceById(1)).thenReturn(position)
        Mockito.`when`(interviewTypeRepo.getReferenceById(1)).thenReturn(typeInterview)
        Mockito.`when`(timeSlotRepo.getReferenceById(1)).thenReturn(mentorTimeSlot)

        // When
        val result = service.createInterview(createInterviewRequest)

        // Then
        assertEquals(createInterviewResult, result)
        verify(interviewRepo, times(1)).save(captor.capture())
        val savedInterview = captor.value
        assertEquals("john.doe@example.com", savedInterview.jobSeekerAccount?.email)
        assertEquals("Mentor.Mentoriii@example.com", savedInterview.mentorAccount?.email)
        assertEquals(AccountStatusEnum.DEACTIVATED, savedInterview.jobSeekerAccount?.status)
    }
}
