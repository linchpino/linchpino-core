package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class InterviewServiceTest {
    @InjectMocks
    private lateinit var service: InterviewService

    @Mock
    private lateinit var interviewRepository: InterviewRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var jobPositionRepository: JobPositionRepository

    @Mock
    private lateinit var interviewTypeRepository: InterviewTypeRepository

    @Mock
    private lateinit var mentorTimeSlotRepository: MentorTimeSlotRepository

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var emailService: EmailService

    @Test
    fun `test create new interview when account exists`() {
        val jobSeekerAccount = Account().apply {
            id = 1
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
        }
        val jobSeekerRole = Role().apply { title = AccountTypeEnum.JOB_SEEKER }
        jobSeekerAccount.addRole(jobSeekerRole)

        val mentorAcc = Account().apply {
            id = 1
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "Mentor.Mentoriii@example.com"
            password = "password_Mentoriii"
        }
        val mentorRole = Role().apply { title = AccountTypeEnum.MENTOR }
        mentorAcc.addRole(mentorRole)

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
        jobSeekerAccount.addInterviewType(typeInterview)
        mentorAcc.addInterviewType(typeInterview)
        position.addInterviewType(typeInterview)

        val createInterviewRequest = CreateInterviewRequest(
            1, 1, 1, 1, "john.doe@example.com"
        )
        val createInterviewResult = CreateInterviewResult(
            null, 1, 1, 1, 1, "john.doe@example.com"
        )

        val captor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        `when`(accountRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(jobSeekerAccount)
        `when`(accountRepository.getReferenceById(1)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.getReferenceById(1)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(1)).thenReturn(typeInterview)
        `when`(interviewRepository.isTimeSlotBooked(mentorTimeSlot.id!!)).thenReturn(false)
        `when`(mentorTimeSlotRepository.getReferenceById(1)).thenReturn(mentorTimeSlot)

        val result = service.createInterview(createInterviewRequest)

        verify(interviewRepository, times(1)).save(captor.capture())
        assertEquals(createInterviewResult, result)
        val savedInterview = captor.value
        assertEquals("john.doe@example.com", savedInterview.jobSeekerAccount?.email)
        assertEquals("Mentor.Mentoriii@example.com", savedInterview.mentorAccount?.email)
        assertEquals(AccountStatusEnum.ACTIVATED, savedInterview.jobSeekerAccount?.status)
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
            id = 2
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "John.Smith@example.com"
            password = "password_Mentoriii"
        }

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 2
            account = mentorAcc
            fromTime = ZonedDateTime.now()
            toTime = ZonedDateTime.now()
            status = MentorTimeSlotEnum.AVAILABLE
        }

        val position = JobPosition().apply {
            id = 2
            title = "Test Job"
        }

        val typeInterview = InterviewType().apply {
            id = 2
            name = "Test Interview Type"
        }

        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)
        val createAccountRequestCaptor: ArgumentCaptor<CreateAccountRequest> =
            ArgumentCaptor.forClass(CreateAccountRequest::class.java)

        `when`(accountRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(null)
        `when`(accountRepository.getReferenceById(1)).thenReturn(jobSeekerAccount)
        `when`(accountRepository.getReferenceById(2)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.getReferenceById(2)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(2)).thenReturn(typeInterview)
        `when`(interviewRepository.isTimeSlotBooked(mentorTimeSlot.id!!)).thenReturn(false)
        `when`(mentorTimeSlotRepository.getReferenceById(2)).thenReturn(mentorTimeSlot)
        `when` (accountService.createAccount(
            createAccountRequestCaptor.captureNonNullable()
        )).thenReturn(
            CreateAccountResult(
                1, "", "", "test@example.com",
                listOf()
            )
        )

        val createInterviewRequest = CreateInterviewRequest(2, 2, 2, 2, "test@example.com")
        service.createInterview(createInterviewRequest)

        verify(interviewRepository, times(1)).save(interviewCaptor.capture())

        val newAccount = createAccountRequestCaptor.value
        assertThat(newAccount.email).isEqualTo("test@example.com")
        assertThat(newAccount.type).isEqualTo(jobSeekerRole.id)
        assertThat(newAccount.status).isEqualTo(AccountStatusEnum.DEACTIVATED)

        val interview = interviewCaptor.value
        assertThat(interview.jobSeekerAccount).isEqualTo(jobSeekerAccount)
        assertThat(interview.mentorAccount).isEqualTo(mentorAcc)
        assertThat(interview.timeSlot).isEqualTo(mentorTimeSlot)
        assertThat(interview.jobPosition).isEqualTo(position)
    }

    @Test
    fun `test upcoming interviews`() {
        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val authentication = JwtAuthenticationToken(jwt)
        val emailCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val pageCaptor: ArgumentCaptor<Pageable> = ArgumentCaptor.forClass(Pageable::class.java)
        val mentorTimeSlotCaptor: ArgumentCaptor<MentorTimeSlotEnum> =
            ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)
        `when`(service.upcomingInterviews(authentication, Pageable.unpaged())).thenReturn(expected)

        val response = service.upcomingInterviews(authentication, Pageable.unpaged())

        verify(interviewRepository, times(1)).findUpcomingInterviews(
            emailCaptor.captureNonNullable(),
            pageCaptor.captureNonNullable(),
            mentorTimeSlotCaptor.captureNonNullable()
        )
        assertThat(emailCaptor.value).isEqualTo("john.doe@example.com")
        assertThat(mentorTimeSlotCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `test create new interview when time slot is not available`() {
        val jobSeekerAccount = Account().apply {
            id = 1
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

        `when`(accountRepository.getReferenceById(1)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.getReferenceById(1)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(1)).thenReturn(typeInterview)
        `when`(interviewRepository.isTimeSlotBooked(mentorTimeSlot.id!!)).thenReturn(true)

        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            service.populateInterviewObject(createInterviewRequest, jobSeekerAccount)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.TIMESLOT_IS_BOOKED)
    }

    @Test
    fun `test past interviews`(){
        // Given
        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val authentication = JwtAuthenticationToken(jwt)
        val emailCaptor:ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val pageCaptor:ArgumentCaptor<Pageable> = ArgumentCaptor.forClass(Pageable::class.java)
        val mentorTimeSlotCaptor:ArgumentCaptor<MentorTimeSlotEnum> = ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)
        `when`(service.pastInterviews(authentication, Pageable.unpaged())).thenReturn(expected)
        // When
        val response = service.pastInterviews(authentication, Pageable.unpaged())

        verify(interviewRepo, times(1)).findPastInterviews(emailCaptor.captureNonNullable(), pageCaptor.captureNonNullable(),mentorTimeSlotCaptor.captureNonNullable())
        assertThat(emailCaptor.value).isEqualTo("john.doe@example.com")
        assertThat(mentorTimeSlotCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(response).isEqualTo(expected)
    }
}
