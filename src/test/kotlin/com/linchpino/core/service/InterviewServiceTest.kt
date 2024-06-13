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
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.security.WithMockJwtSecurityContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
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
    private lateinit var timeSlotService: TimeSlotService

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var meetService: MeetService

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
            id = 2
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
            1, 1, 1, 2, "john.doe@example.com"
        )
        val createInterviewResult = CreateInterviewResult(
            null, 1, 1, 1, 2, "john.doe@example.com"
        )

        val captor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)
        val timeSlotCaptor: ArgumentCaptor<MentorTimeSlot> = ArgumentCaptor.forClass(MentorTimeSlot::class.java)
        val timeSlotStatusCaptor: ArgumentCaptor<MentorTimeSlotEnum> =
            ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)

        `when`(accountRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(jobSeekerAccount)
        `when`(accountRepository.getReferenceById(2)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.getReferenceById(1)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(1)).thenReturn(typeInterview)
        `when`(mentorTimeSlotRepository.getReferenceById(1)).thenReturn(mentorTimeSlot)
        `when`(meetService.createGoogleWorkSpace()).thenReturn("fake-meet-code")

        val result = service.createInterview(createInterviewRequest)

        verify(interviewRepository, times(1)).save(captor.capture())
        verify(timeSlotService, times(1)).updateTimeSlotStatus(
            timeSlotCaptor.captureNonNullable(),
            timeSlotStatusCaptor.captureNonNullable()
        )
        verify(emailService, times(1)).sendingInterviewInvitationEmailToJobSeeker(captor.value)

        assertEquals(createInterviewResult, result)
        verify(meetService, times(1)).createGoogleWorkSpace()
        val savedInterview = captor.value
        assertEquals("john.doe@example.com", savedInterview.jobSeekerAccount?.email)
        assertEquals("Mentor.Mentoriii@example.com", savedInterview.mentorAccount?.email)
        assertEquals(AccountStatusEnum.ACTIVATED, savedInterview.jobSeekerAccount?.status)

        assertThat(timeSlotStatusCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(timeSlotCaptor.value).isEqualTo(mentorTimeSlot)
        assertThat(savedInterview.meetCode).isEqualTo("fake-meet-code")
    }

    @Test
    fun `test create new interview when account not exists`() {
        val jobSeekerRole = Role().apply { title = AccountTypeEnum.JOB_SEEKER }

        val jobSeekerAccount = Account().apply {
            id = 1
            firstName = "test"
            lastName = "test"
            email = "test@example.com"
            password = "password123"
        }
        val mentorAccount = Account().apply {
            id = 2
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "John.Smith@example.com"
            password = "password_Mentoriii"
        }

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 1
            account = mentorAccount
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

        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)
        val createAccountRequestCaptor: ArgumentCaptor<CreateAccountRequest> =
            ArgumentCaptor.forClass(CreateAccountRequest::class.java)
        val timeSlotCaptor: ArgumentCaptor<MentorTimeSlot> = ArgumentCaptor.forClass(MentorTimeSlot::class.java)
        val timeSlotStatusCaptor: ArgumentCaptor<MentorTimeSlotEnum> =
            ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)

        val createInterviewRequest = CreateInterviewRequest(
            position.id!!,
            typeInterview.id!!,
            mentorTimeSlot.id!!,
            mentorAccount.id!!,
            jobSeekerAccount.email
        )

        `when`(accountRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(null)
        `when`(accountRepository.getReferenceById(jobSeekerAccount.id!!)).thenReturn(jobSeekerAccount)
        `when`(accountRepository.getReferenceById(createInterviewRequest.mentorAccountId)).thenReturn(mentorAccount)
        `when`(jobPositionRepository.getReferenceById(createInterviewRequest.jobPositionId)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(createInterviewRequest.interviewTypeId)).thenReturn(
            typeInterview
        )
        `when`(mentorTimeSlotRepository.getReferenceById(createInterviewRequest.timeSlotId)).thenReturn(mentorTimeSlot)
        `when`(
            accountService.createAccount(
                createAccountRequestCaptor.captureNonNullable()
            )
        ).thenReturn(
            CreateAccountResult(
                1, null, null, "test@example.com",
                listOf()
            )
        )
        `when`(meetService.createGoogleWorkSpace()).thenReturn("fake-meet-code")

        service.createInterview(createInterviewRequest)

        verify(meetService, times(1)).createGoogleWorkSpace()
        verify(interviewRepository, times(1)).save(interviewCaptor.capture())
        verify(timeSlotService, times(1)).updateTimeSlotStatus(
            timeSlotCaptor.captureNonNullable(),
            timeSlotStatusCaptor.captureNonNullable()
        )
        verify(emailService, times(1)).sendingInterviewInvitationEmailToJobSeeker(interviewCaptor.value)

        val newAccount = createAccountRequestCaptor.value
        assertThat(newAccount.email).isEqualTo("test@example.com")
        assertThat(newAccount.type).isEqualTo(jobSeekerRole.id)
        assertThat(newAccount.status).isEqualTo(AccountStatusEnum.DEACTIVATED)

        val interview = interviewCaptor.value
        assertThat(interview.jobSeekerAccount).isEqualTo(jobSeekerAccount)
        assertThat(interview.mentorAccount).isEqualTo(mentorAccount)
        assertThat(interview.timeSlot).isEqualTo(mentorTimeSlot)
        assertThat(interview.jobPosition).isEqualTo(position)
        assertThat(interview.meetCode).isEqualTo("fake-meet-code")

        assertThat(timeSlotStatusCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(timeSlotCaptor.value).isEqualTo(mentorTimeSlot)
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
            id = 2
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
            status = MentorTimeSlotEnum.ALLOCATED
        }

        val position = JobPosition().apply {
            id = 1
            title = "Test Job"
        }

        val typeInterview = InterviewType().apply {
            id = 1
            name = "Test Interview Type"
        }

        val createInterviewRequest = CreateInterviewRequest(
            position.id!!,
            typeInterview.id!!,
            mentorTimeSlot.id!!,
            mentorAcc.id!!,
            "john.doe@example.com"
        )

        `when`(accountRepository.findByEmailIgnoreCase(jobSeekerAccount.email)).thenReturn(jobSeekerAccount)
        `when`(accountRepository.findReferenceById(createInterviewRequest.mentorAccountId)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)).thenReturn(position)
        `when`(interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)).thenReturn(
            typeInterview
        )
        `when`(mentorTimeSlotRepository.findReferenceById(createInterviewRequest.timeSlotId)).thenReturn(mentorTimeSlot)


        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            service.createInterview(createInterviewRequest)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.TIMESLOT_IS_BOOKED)
    }

    @Test
    fun `test past interviews`() {
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
        val emailCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val pageCaptor: ArgumentCaptor<Pageable> = ArgumentCaptor.forClass(Pageable::class.java)
        val mentorTimeSlotCaptor: ArgumentCaptor<MentorTimeSlotEnum> =
            ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)
        `when`(service.pastInterviews(authentication, Pageable.unpaged())).thenReturn(expected)
        // When
        val response = service.pastInterviews(authentication, Pageable.unpaged())

        verify(interviewRepository, times(1)).findPastInterviews(
            emailCaptor.captureNonNullable(),
            pageCaptor.captureNonNullable(),
            mentorTimeSlotCaptor.captureNonNullable()
        )
        assertThat(emailCaptor.value).isEqualTo("john.doe@example.com")
        assertThat(mentorTimeSlotCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(response).isEqualTo(expected)
    }


    @Test
    fun `test interview validity returns status true and google meet link`() {

        // Given
        val id = 1L
        val email = "john.doe@example.com"
        val meetCode = "abc-efg-hij"
        SecurityContextHolder.getContextHolderStrategy().context =
            WithMockJwtSecurityContextFactory().createSecurityContext(WithMockJwt("john.doe@example.com"))

        val timeSlot = MentorTimeSlot().apply {
            fromTime = ZonedDateTime.now().plusMinutes(2)
            toTime = ZonedDateTime.now().plusMinutes(60)
            status = MentorTimeSlotEnum.ALLOCATED
        }

        val interview = Interview().apply {
            this.timeSlot = timeSlot
            this.meetCode = meetCode
        }
        `when`(interviewRepository.findByInterviewIdAndAccountEmail(id, email)).thenReturn(interview)

        // When
        val response = service.checkValidity(id)

        // Then
        assertThat(response.interviewDateTimeStart).isEqualTo(interview.timeSlot?.fromTime)
        assertThat(response.interviewDateTimeEnd).isEqualTo(interview.timeSlot?.toTime)
        assertThat(response.verifyStatus).isEqualTo(true)
        assertThat(response.link).isEqualTo("https://meet.google.com/$meetCode")
    }

    @Test
    fun `test interview validity returns false status when timeslot starts more than 5 min from now`() {

        // Given
        val id = 1L
        val email = "john.doe@example.com"
        val meetCode = "abc-efg-hij"
        SecurityContextHolder.getContextHolderStrategy().context =
            WithMockJwtSecurityContextFactory().createSecurityContext(WithMockJwt("john.doe@example.com"))

        val timeSlot = MentorTimeSlot().apply {
            fromTime = ZonedDateTime.now().plusMinutes(6)
            toTime = ZonedDateTime.now().plusMinutes(60)
            status = MentorTimeSlotEnum.ALLOCATED
        }

        val interview = Interview().apply {
            this.timeSlot = timeSlot
            this.meetCode = meetCode
        }
        `when`(interviewRepository.findByInterviewIdAndAccountEmail(id, email)).thenReturn(interview)

        // When
        val response = service.checkValidity(id)

        // Then
        assertThat(response.interviewDateTimeStart).isEqualTo(interview.timeSlot?.fromTime)
        assertThat(response.interviewDateTimeEnd).isEqualTo(interview.timeSlot?.toTime)
        assertThat(response.verifyStatus).isEqualTo(false)
        assertThat(response.link).isEqualTo("")
    }

    @Test
    fun `test interview validity throws not found exception if there is no interview with provided id or account email`() {
        // Given
        SecurityContextHolder.getContextHolderStrategy().context =
            WithMockJwtSecurityContextFactory().createSecurityContext(WithMockJwt("john.doe@example.com"))

        `when`(interviewRepository.findByInterviewIdAndAccountEmail(anyLong(), anyString())).thenReturn(null)

        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            service.checkValidity(5)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }
}
