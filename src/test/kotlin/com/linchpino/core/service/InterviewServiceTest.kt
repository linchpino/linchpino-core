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
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.security.email
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
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
import java.time.DayOfWeek
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
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var calendarService: CalendarService

    @Mock
    private lateinit var scheduleService: ScheduleService

    private val startTime = ZonedDateTime.now()
    private val endTime = startTime.plusMinutes(45)

    @Mock
    private lateinit var interviewLogService: InterviewLogService

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

        val mentorAccount = Account().apply {
            id = 2
            firstName = "Mentor"
            lastName = "Mentoriii"
            email = "Mentor.Mentoriii@example.com"
            password = "password_Mentoriii"
        }
        val mentorRole = Role().apply { title = AccountTypeEnum.MENTOR }
        mentorAccount.addRole(mentorRole)

        val mentorTimeSlot = MentorTimeSlot().apply {
            id = 1
            account = mentorAccount
            fromTime = ZonedDateTime.parse("2018-12-25T12:30:00.000+01:00")
            toTime = ZonedDateTime.parse("2018-12-25T12:30:00.000+01:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }

        val schedule = Schedule().apply {
            id = 1
            startTime = ZonedDateTime.parse("2018-12-25T12:30:00.000+01:00")
            endTime = ZonedDateTime.parse("2018-12-25T12:30:00.000+01:00")
            recurrenceType = RecurrenceType.WEEKLY
            interval = 1
            weekDays = mutableListOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            account = mentorAccount
        }

        mentorAccount.schedule = schedule

        val position = JobPosition().apply {
            id = 1
            title = "Test Job"
        }

        val typeInterview = InterviewType().apply {
            id = 1
            name = "Test Interview Type"
        }
        jobSeekerAccount.addInterviewType(typeInterview)
        mentorAccount.addInterviewType(typeInterview)
        position.addInterviewType(typeInterview)


        val createInterviewRequest = CreateInterviewRequest(
            1, 1, startTime, endTime, 2, "john.doe@example.com"
        )
        val createInterviewResult = CreateInterviewResult(
            null, 1, 1, 1, 2, "john.doe@example.com"
        )

        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        val attendeeCaptor: ArgumentCaptor<List<String>> =
            ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<String>>
        val titleCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val timeCaptor: ArgumentCaptor<Pair<ZonedDateTime, ZonedDateTime>> =
            ArgumentCaptor.forClass(Pair::class.java) as ArgumentCaptor<Pair<ZonedDateTime, ZonedDateTime>>

        val logTypeCaptor: ArgumentCaptor<InterviewLogType> = ArgumentCaptor.forClass(InterviewLogType::class.java)
        val idCaptor: ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)

        `when`(accountRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(jobSeekerAccount)
        `when`(accountRepository.getReferenceById(2)).thenReturn(mentorAccount)
        `when`(jobPositionRepository.getReferenceById(1)).thenReturn(position)
        `when`(interviewTypeRepository.getReferenceById(1)).thenReturn(typeInterview)
        `when`(scheduleService.availableTimeSlot(mentorAccount, createInterviewRequest)).thenReturn(mentorTimeSlot)
        `when`(
            calendarService.googleMeetCode(
                attendeeCaptor.captureNonNullable(),
                titleCaptor.captureNonNullable(),
                timeCaptor.captureNonNullable()
            )
        ).thenReturn("fake-meet-code")

        val result = service.createInterview(createInterviewRequest)

        verify(interviewRepository, times(1)).save(interviewCaptor.capture())

        verify(emailService, times(1)).sendingInterviewInvitationEmailToJobSeeker(interviewCaptor.value)
        verify(interviewLogService, times(1)).save(logTypeCaptor.captureNonNullable(), idCaptor.captureNonNullable())

        assertEquals(createInterviewResult, result)
        val savedInterview = interviewCaptor.value
        assertEquals("john.doe@example.com", savedInterview.jobSeekerAccount?.email)
        assertEquals("Mentor.Mentoriii@example.com", savedInterview.mentorAccount?.email)
        assertEquals(AccountStatusEnum.ACTIVATED, savedInterview.jobSeekerAccount?.status)

        assertThat(savedInterview.meetCode).isEqualTo("fake-meet-code")

        val attendees = attendeeCaptor.value
        assertThat(attendees).isEqualTo(listOf(mentorAccount.email, jobSeekerAccount.email))

        val meetTitle = titleCaptor.value
        assertThat(meetTitle).isEqualTo("${typeInterview.name} with ${mentorAccount.firstName} and ${jobSeekerAccount.firstName}")

        val times = timeCaptor.value
        assertThat(times).isEqualTo(Pair(mentorTimeSlot.fromTime, mentorTimeSlot.toTime))
        assertThat(times).isEqualTo(Pair(mentorTimeSlot.fromTime, mentorTimeSlot.toTime))

        val id = idCaptor.value
        val logType = logTypeCaptor.value

        assertThat(id).isEqualTo(1)
        assertThat(logType).isEqualTo(InterviewLogType.CREATED)
    }

    @Test
    fun `test create new interview when account not exists`() {
        val jobSeekerRole = Role().apply { title = AccountTypeEnum.JOB_SEEKER }

        val jobSeekerAccount = Account().apply {
            id = 1
            email = "test@example.com"
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

        val attendeeCaptor: ArgumentCaptor<List<String>> =
            ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<String>>
        val titleCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val timeCaptor: ArgumentCaptor<Pair<ZonedDateTime, ZonedDateTime>> =
            ArgumentCaptor.forClass(Pair::class.java) as ArgumentCaptor<Pair<ZonedDateTime, ZonedDateTime>>
        val logTypeCaptor: ArgumentCaptor<InterviewLogType> = ArgumentCaptor.forClass(InterviewLogType::class.java)
        val idCaptor: ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        val createInterviewRequest = CreateInterviewRequest(
            position.id!!,
            typeInterview.id!!,
            startTime,
            endTime,
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
        `when`(scheduleService.availableTimeSlot(mentorAccount, createInterviewRequest)).thenReturn(mentorTimeSlot)

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
        `when`(
            calendarService.googleMeetCode(
                attendeeCaptor.captureNonNullable(),
                titleCaptor.captureNonNullable(),
                timeCaptor.captureNonNullable()
            )
        ).thenReturn("fake-meet-code")

        service.createInterview(createInterviewRequest)

        verify(interviewRepository, times(1)).save(interviewCaptor.capture())
        verify(emailService, times(1)).sendingInterviewInvitationEmailToJobSeeker(interviewCaptor.value)
        verify(interviewLogService, times(1)).save(logTypeCaptor.captureNonNullable(), idCaptor.captureNonNullable())

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


        val attendees = attendeeCaptor.value
        assertThat(attendees).isEqualTo(listOf(mentorAccount.email, jobSeekerAccount.email))

        val meetTitle = titleCaptor.value
        assertThat(meetTitle).isEqualTo("${typeInterview.name} with ${mentorAccount.firstName} and jobseeker")

        val times = timeCaptor.value
        assertThat(times).isEqualTo(Pair(mentorTimeSlot.fromTime, mentorTimeSlot.toTime))
        val id = idCaptor.value
        val logType = logTypeCaptor.value

        assertThat(id).isEqualTo(1)
        assertThat(logType).isEqualTo(InterviewLogType.CREATED)
    }

    @Test
    fun `test upcoming interviews`() {
        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, 1L,"John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
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
            startTime,
            endTime,
            mentorAcc.id!!,
            "john.doe@example.com"
        )

        `when`(accountRepository.findByEmailIgnoreCase(jobSeekerAccount.email)).thenReturn(jobSeekerAccount)
        `when`(accountRepository.findReferenceById(createInterviewRequest.mentorAccountId)).thenReturn(mentorAcc)
        `when`(jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)).thenReturn(position)
        `when`(interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)).thenReturn(
            typeInterview
        )
        `when`(scheduleService.availableTimeSlot(mentorAcc, createInterviewRequest)).thenThrow(
            LinchpinException(
                ErrorCode.TIMESLOT_IS_BOOKED,
                ""
            )
        )


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
                InterviewListResponse(1L, 1L,"John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
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
    fun `test interview validity throws not found exception if there is no interview with provided id or account email`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication()

        `when`(interviewRepository.findByInterviewIdAndAccountEmail(anyLong(), anyString())).thenReturn(null)

        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            service.checkValidity(5, authentication)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun `test interview validity returns status true and google meet link, also a log for join event is saved`() {

        // Given
        val account = Account().apply {
            id = 1
            email = "john.doe@example.com"
            firstName = "John"
            lastName = "Doe"
        }
        val id = 1L
        val email = "john.doe@example.com"
        val meetCode = "abc-efg-hij"
        val authentication = WithMockJwt.mockAuthentication(email)

        val timeSlot = MentorTimeSlot().apply {
            fromTime = ZonedDateTime.now().plusMinutes(2)
            toTime = ZonedDateTime.now().plusMinutes(60)
            status = MentorTimeSlotEnum.ALLOCATED
        }

        val interview = Interview().apply {
            this.timeSlot = timeSlot
            this.meetCode = meetCode
        }
        `when`(accountRepository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)
        `when`(interviewRepository.findByInterviewIdAndAccountEmail(id, email)).thenReturn(interview)

        // When
        val response = service.checkValidity(id, authentication)

        // Then
        assertThat(response.interviewDateTimeStart).isEqualTo(interview.timeSlot?.fromTime)
        assertThat(response.interviewDateTimeEnd).isEqualTo(interview.timeSlot?.toTime)
        assertThat(response.verifyStatus).isEqualTo(true)
        assertThat(response.link).isEqualTo("https://meet.google.com/$meetCode")

        verify(interviewLogService, times(1)).save(InterviewLogType.JOINED, account.id)
    }

    @Test
    fun `test interview validity returns false status when timeslot starts more than 5 min from now`() {

        // Given
        val id = 1L
        val email = "john.doe@example.com"
        val meetCode = "abc-efg-hij"
        val authentication = WithMockJwt.mockAuthentication(email)

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
        val response = service.checkValidity(id, authentication)

        // Then
        assertThat(response.interviewDateTimeStart).isEqualTo(interview.timeSlot?.fromTime)
        assertThat(response.interviewDateTimeEnd).isEqualTo(interview.timeSlot?.toTime)
        assertThat(response.verifyStatus).isEqualTo(false)
        assertThat(response.link).isEqualTo("")
    }

    @Test
    fun `test upcoming interviews for job seeker`() {
        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, 1L,"John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
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
        `when`(service.jobSeekerUpcomingInterviews(authentication, Pageable.unpaged())).thenReturn(expected)

        val response = service.jobSeekerUpcomingInterviews(authentication, Pageable.unpaged())

        verify(interviewRepository, times(1)).findJobSeekerUpcomingInterviews(
            emailCaptor.captureNonNullable(),
            pageCaptor.captureNonNullable(),
            mentorTimeSlotCaptor.captureNonNullable()
        )
        assertThat(emailCaptor.value).isEqualTo("john.doe@example.com")
        assertThat(mentorTimeSlotCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `test past interviews for job seeker`() {
        // Given
        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, 1L,"John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
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
        `when`(service.jobSeekerPastInterviews(authentication, Pageable.unpaged())).thenReturn(expected)
        // When
        val response = service.jobSeekerPastInterviews(authentication, Pageable.unpaged())

        verify(interviewRepository, times(1)).findJobSeekerPastInterviews(
            emailCaptor.captureNonNullable(),
            pageCaptor.captureNonNullable(),
            mentorTimeSlotCaptor.captureNonNullable()
        )
        assertThat(emailCaptor.value).isEqualTo("john.doe@example.com")
        assertThat(mentorTimeSlotCaptor.value).isEqualTo(MentorTimeSlotEnum.ALLOCATED)
        assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `test reminder`() {
        val fromCaptor: ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        val toCaptor: ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)

        val from = ZonedDateTime.now()
        val to = from.plusMinutes(30)
        `when`(
            interviewRepository.findInterviewsWithin(
                fromCaptor.captureNonNullable(),
                toCaptor.captureNonNullable()
            )
        ).thenReturn(
            listOf(Interview().apply {
                mentorAccount = Account().apply {
                    firstName = "john"
                    lastName = "doe"
                    email = "john.doe@example.com"
                }
                jobSeekerAccount = Account().apply {
                    firstName = "jane"
                    lastName = "smith"
                    email = "jane.smith@example.com"
                }
                timeSlot = MentorTimeSlot().apply {
                    fromTime = from
                    toTime = to
                }
            })
        )

        service.remindInterviewParties()

        verify(emailService, times(1)).sendEmail(
            "jane.smith@example.com",
            "Reminder: Your Upcoming Interview on Linchpino",
            "interviewee-reminder.html",
            mapOf(
                "fullName" to "jane smith",
                "date" to from.toLocalDate(),
                "time" to from.toLocalTime(),
                "timezone" to from.zone,
            )
        )


        verify(emailService, times(1)).sendEmail(
            "john.doe@example.com",
            "Reminder: Interview Scheduled on Linchpino",
            "interviewer-reminder.html",
            mapOf(
                "fullName" to "john doe",
                "intervieweeName" to "jane smith",
                "date" to from.toLocalDate(),
                "time" to from.toLocalTime(),
                "timezone" to from.zone,
            )
        )

    }
}
