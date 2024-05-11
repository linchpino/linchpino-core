package com.linchpino.core.service

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.entity.*
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
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

    @Mock
    private lateinit var roleRepository: RoleRepository

    @Test
    fun `test create new interview when account exists`() {
        // Given
        val jobSeekerAccount = Account().apply {
            id = 2
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
        }

        val mentorAcc = Account().apply {
            id = 1
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "Mentor.Mentoriii@example.com"
            password = "password_Mentoriii"
        }

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 1
            account = mentorAcc
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

        Mockito.`when`(accountRepo.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(jobSeekerAccount)
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

    @Test
    fun `test create new interview when account not exists`() {
        val jobSeekerRole = Role().apply { title = AccountTypeEnum.JOB_SEEKER }

        val jobSeekerAccount = Account().apply {
            id = 2
            firstName = "test"
            lastName = "test"
            email = "test@example.com"
            password = "password123"
        }
        val mentorAcc = Account().apply {
            id = 1
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "Mentor.Mentoriii@example.com"
            password = "password_Mentoriii"
        }

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 1
            account = mentorAcc
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

        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)
        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        Mockito.`when`(accountRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(null)
        Mockito.`when`(accountRepo.save(any())).thenReturn(jobSeekerAccount)
        Mockito.`when`(accountRepo.getReferenceById(1)).thenReturn(mentorAcc)
        Mockito.`when`(jobPositionRepo.getReferenceById(1)).thenReturn(position)
        Mockito.`when`(interviewTypeRepo.getReferenceById(1)).thenReturn(typeInterview)
        Mockito.`when`(timeSlotRepo.getReferenceById(1)).thenReturn(mentorTimeSlot)
        Mockito.`when`(roleRepository.getReferenceById(AccountTypeEnum.JOB_SEEKER.value)).thenReturn(jobSeekerRole)

        val createInterviewRequest = CreateInterviewRequest(1, 1, 1, 1, "test@example.com")
        service.createInterview(createInterviewRequest)

        verify(interviewRepo, times(1)).save(interviewCaptor.capture())
        verify(accountRepo, times(1)).save(accountCaptor.capture())

        val newAccount = accountCaptor.value
        assertEquals("test@example.com", newAccount.email)
        assertThat(newAccount.roles()).containsExactly(jobSeekerRole)
        assertEquals(AccountStatusEnum.DEACTIVATED, newAccount.status)

        val interview = interviewCaptor.value
        assertEquals(jobSeekerAccount, interview.jobSeekerAccount)
        assertEquals(mentorAcc, interview.mentorAccount)
        assertEquals(mentorTimeSlot, interview.timeSlot)
        assertEquals(position, interview.jobPosition)
    }
}
