package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.InterviewFeedBackRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Role
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewLogRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.ScheduleRepository
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.service.CalendarService
import com.linchpino.core.service.EmailService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Import(PostgresContainerConfig::class)
class InterviewControllerTestIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jobSeekerAccRepo: AccountRepository

    @Autowired
    private lateinit var mentorAccRepo: AccountRepository

    @Autowired
    private lateinit var jobPositionRepo: JobPositionRepository

    @Autowired
    private lateinit var interviewTypeRepo: InterviewTypeRepository

    @Autowired
    private lateinit var timeSlotRepo: MentorTimeSlotRepository

    @MockBean
    private lateinit var mailService: EmailService

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @MockBean
    private lateinit var calendarService: CalendarService

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Autowired
    private lateinit var logRepository: InterviewLogRepository


    @BeforeEach
    fun init() {
        val mentorRole = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)
        val jobSeekerRole = entityManager.find(Role::class.java, AccountTypeEnum.JOB_SEEKER.value)

        val jobSeeker1 = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
        }
        val jobSeeker2 = Account().apply {
            firstName = "Jane"
            lastName = "Smith"
            email = "jane.smith@example.com"
            password = "password123"
        }
        jobSeeker1.addRole(jobSeekerRole)
        jobSeeker2.addRole(jobSeekerRole)
        jobSeekerAccRepo.save(jobSeeker1)
        jobSeekerAccRepo.save(jobSeeker2)

        val mentorAccount = Account().apply {
            firstName = "Mentor_1"
            lastName = "Mentoriii"
            email = "john.smith@example.com"
            password = "password_Mentoriii"
        }

        mentorAccount.addRole(mentorRole)

        val startTime = ZonedDateTime.parse("2024-08-28T12:00:00.000+03:30")
        val endTime = startTime.plusDays(60)

        val schedule = Schedule()
        schedule.startTime = startTime
        schedule.endTime = endTime
        schedule.interval = 1
        schedule.duration = 60
        schedule.recurrenceType = RecurrenceType.WEEKLY
        schedule.weekDays = mutableListOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        schedule.account = mentorAccount

        mentorAccount.schedule = schedule
        mentorAccRepo.save(mentorAccount)

        scheduleRepository.save(schedule)

        val position = JobPosition().apply {
            title = "Test Job"
        }
        jobPositionRepo.save(position)

        val typeInterview = InterviewType().apply {
            name = "Test Interview Type"
        }
        interviewTypeRepo.save(typeInterview)


        val mentorTimeSlot1 = MentorTimeSlot().apply {
            account = mentorAccount
            fromTime = ZonedDateTime.now().plusDays(1)
            toTime = ZonedDateTime.now().plusDays(1)
            status = MentorTimeSlotEnum.AVAILABLE
        }
        val mentorTimeSlot2 = MentorTimeSlot().apply {
            account = mentorAccount
            fromTime = ZonedDateTime.now()
            toTime = ZonedDateTime.now()
            status = MentorTimeSlotEnum.AVAILABLE
        }
        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)
    }

    @Test
    fun `test with existed email address result in creating a new interview for job seeker`() {
        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)

        val john = entityManager.createQuery(
            "select a from Account a where a.email = 'john.doe@example.com'",
            Account::class.java
        ).singleResult

        val startTime = ZonedDateTime.parse("2024-08-28T12:00:00.000+03:30")
        val endTime = startTime.plusMinutes(60)

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            mentorAccRepo.findAll()
                .first { it.roles().map { role -> role.title }.contains(AccountTypeEnum.MENTOR) }.id!!,
            john.email
        )
        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(jsonPath("$.timeSlotId").isNumber)
            .andExpect(jsonPath("$.mentorAccountId").value(request.mentorAccountId))
            .andExpect(jsonPath("$.jobSeekerEmail").value("john.doe@example.com"))
            .andExpect(jsonPath("$.interviewId").exists())
            .andExpect(jsonPath("$.interviewId").isNumber)

        verify(
            mailService,
            times(1)
        ).sendingInterviewInvitationEmailToJobSeeker(interviewCaptor.captureNonNullable())

        val interview = interviewCaptor.value
        assertThat(interview.id).isNotNull()
        assertThat(interview.jobSeekerAccount?.email).isEqualTo("john.doe@example.com")
        assertThat(interview.mentorAccount?.email).isEqualTo("john.smith@example.com")
        assertThat(interview.jobPosition?.title).isEqualTo("Test Job")
        assertThat(interview.interviewType?.name).isEqualTo("Test Interview Type")

        val logs = logRepository.findAll()
        assertThat(logs.count()).isEqualTo(1)
        assertThat(logs[0].type).isEqualTo(InterviewLogType.CREATED)
    }

    @Test
    fun `test with wrong email address results in bad request`() {
        val startTime = ZonedDateTime.parse("2024-08-28T12:00:00.000+03:30")
        val endTime = startTime.plusMinutes(60)

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            AccountTypeEnum.MENTOR.value.toLong(),
            "zsdvfzsxd"
        )

        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `test if request start and end does not lies on mentor scheduled days throws exception`() {
        val startTime = ZonedDateTime.parse("2024-09-19T12:00:00.000+03:30") // THURSDAY
        val endTime = startTime.plusMinutes(60)

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            mentorAccRepo.findAll()
                .first { it.roles().map { role -> role.title }.contains(AccountTypeEnum.MENTOR) }.id!!,
            "john.doe@example.com"
        )

        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Timeslot is invalid"))
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `test if request start and end does not lies on mentor scheduled hours throws exception`() {
        val startTime = ZonedDateTime.parse("2024-09-18T12:30:00.000+03:30") // WEDNESDAY 12:30 to 13:30 should fail => 13:30 is past 13:00
        val endTime = startTime.plusMinutes(60)

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            mentorAccRepo.findAll()
                .first { it.roles().map { role -> role.title }.contains(AccountTypeEnum.MENTOR) }.id!!,
            "john.doe@example.com"
        )

        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Timeslot is invalid"))
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `test if requested time already booked throws exception`() {
        val startTime = ZonedDateTime.parse("2024-09-18T12:00:00.000+03:30") // WEDNESDAY
        val endTime = startTime.plusMinutes(60)

        val mentor = mentorAccRepo.findAll()
            .first { it.roles().map { role -> role.title }.contains(AccountTypeEnum.MENTOR) }

        val mentorTimeSlot1 = MentorTimeSlot().apply {
            account = mentor
            fromTime = startTime
            toTime = endTime
            status = MentorTimeSlotEnum.AVAILABLE
        }
        timeSlotRepo.save(mentorTimeSlot1)



        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            mentor.id!!,
            "john.doe@example.com"
        )

        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Selected time slot is already booked"))
            .andExpect(jsonPath("$.status").value(400))
    }




    @Test
    fun `test with not exist email address result in creating a silent account for job seeker`() {

        val startTime = ZonedDateTime.parse("2024-08-28T12:00:00.000+03:30")
        val endTime = startTime.plusMinutes(60)

        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)
        val mentorAccount = entityManager.createQuery(
            "select a from Account a where email = 'john.smith@example.com'",
            Account::class.java
        ).singleResult

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            startTime,
            endTime,
            mentorAccount.id!!,
            "mahsa.saeedy@gmail.com"
        )

        mockMvc.perform(
            post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(jsonPath("$.timeSlotId").isNumber)
            .andExpect(jsonPath("$.mentorAccountId").value(request.mentorAccountId))
            .andExpect(jsonPath("$.jobSeekerEmail").value(request.jobSeekerEmail))
            .andExpect(jsonPath("$.interviewId").exists())
            .andExpect(jsonPath("$.interviewId").isNumber)

        verify(
            mailService,
            times(1)
        ).sendingInterviewInvitationEmailToJobSeeker(interviewCaptor.captureNonNullable())

        val interview = interviewCaptor.value
        assertThat(interview.id).isNotNull()
        assertThat(interview.jobSeekerAccount?.email).isEqualTo(request.jobSeekerEmail)
        assertThat(interview.mentorAccount?.email).isEqualTo("john.smith@example.com")
        assertThat(interview.jobPosition?.title).isEqualTo("Test Job")
        assertThat(interview.interviewType?.name).isEqualTo("Test Interview Type")

        val logs = logRepository.findAll()
        assertThat(logs.count()).isEqualTo(1)
        assertThat(logs[0].type).isEqualTo(InterviewLogType.CREATED)
    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test upcoming interviews returns page of result successfully for authenticated user`() {
        // get required data set in before each
        val interviews = saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/upcoming")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].intervieweeId").value(interviews[1].jobSeekerAccount?.id))
            .andExpect(jsonPath("$.content[0].intervieweeName").value("${interviews[1].jobSeekerAccount?.firstName} ${interviews[1].jobSeekerAccount?.lastName}"))
            .andExpect(jsonPath("$.content[0].interviewType").value(interviews[1].interviewType?.name))

    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test upcoming interviews returns empty page if mentor does not have more than one page of interviews`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/upcoming")
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isEmpty)

    }

    @Test
    @WithMockJwt(
        username = "john.smith@example.com",
        roles = [AccountTypeEnum.GUEST, AccountTypeEnum.ADMIN, AccountTypeEnum.JOB_SEEKER]
    )
    fun `test upcoming interviews returns 403 if authenticated user is not mentor`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/upcoming")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test past interviews returns page of result successfully for authenticated user`() {
        // get required data set in before each
        val interviews = saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(2))
            .andExpect(jsonPath("$.content[0].intervieweeId").value(interviews[0].jobSeekerAccount?.id))
            .andExpect(jsonPath("$.content[0].intervieweeName").value("${interviews[0].jobSeekerAccount?.firstName} ${interviews[0].jobSeekerAccount?.lastName}"))
            .andExpect(jsonPath("$.content[0].interviewType").value(interviews[0].interviewType?.name))
    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test past interviews returns empty page if mentor does not have more than one page of interviews`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/past")
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isEmpty)

    }

    @Test
    @WithMockJwt(
        username = "john.smith@example.com",
        roles = [AccountTypeEnum.GUEST, AccountTypeEnum.ADMIN, AccountTypeEnum.JOB_SEEKER]
    )
    fun `test past interviews returns 403 if authenticated user is not mentor`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockJwt(
        username = "john.doe@example.com",
        roles = [AccountTypeEnum.JOB_SEEKER]
    )
    fun `test interview validity returns valid interview response for job seeker if the interview starts within 5 minutes`() {
        // get required data set in before each
        val interviews = saveInterviewData()
        val interview = interviews[2]
        val id = interview.id

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX")

        // When & Then
        mockMvc.perform(
            get("/api/interviews/$id/validity")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.interviewDateTimeStart").value(interview.timeSlot?.fromTime?.format(formatter)))
            .andExpect(jsonPath("$.interviewDateTimeEnd").value(interview.timeSlot?.toTime?.format(formatter)))
            .andExpect(jsonPath("$.verifyStatus").value(true))
            .andExpect(jsonPath("$.link").value("https://meet.google.com/abc-efg-hij"))

        val logs = logRepository.findAll()
        assertThat(logs.count()).isEqualTo(1)
        assertThat(logs[0].type).isEqualTo(InterviewLogType.JOINED)
    }

    @Test
    @WithMockJwt(
        username = "jane.smith@example.com",
        roles = [AccountTypeEnum.JOB_SEEKER]
    )
    fun `test interview validity returns invalid interview response for job seeker if the interview does not start within 5 minutes`() {
        // get required data set in before each
        val interviews = saveInterviewData()
        val interview = interviews[1]
        val id = interview.id

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX")


        // When & Then
        mockMvc.perform(
            get("/api/interviews/$id/validity")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.interviewDateTimeStart").value(interview.timeSlot?.fromTime?.format(formatter)))
            .andExpect(jsonPath("$.interviewDateTimeEnd").value(interview.timeSlot?.toTime?.format(formatter)))
            .andExpect(jsonPath("$.verifyStatus").value(false))
            .andExpect(jsonPath("$.link").value(""))

        val logs = logRepository.findAll()
        assertThat(logs.count()).isEqualTo(0)
    }

    @Test
    @WithMockJwt(
        username = "jane.smith@example.com",
        roles = [AccountTypeEnum.JOB_SEEKER]
    )
    fun `test interview validity returns 404 if job seeker sends an interview id that is not belong to himself`() {
        // get required data set in before each
        val interviews = saveInterviewData()
        val interview =
            interviews[0] // interview belongs to john.doe@example.com but authenticated user is jane.smith@example.com
        val id = interview.id


        // When & Then
        mockMvc.perform(
            get("/api/interviews/$id/validity")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Interview entity not found"))
    }

    @Test
    @WithMockJwt(
        username = "jane.smith@example.com",
        roles = [AccountTypeEnum.JOB_SEEKER]
    )
    fun `test interview feedback`() {
        // Given
        val feedback = InterviewFeedBackRequest(2, "content")
        val interviews = saveInterviewData()
        val id = interviews.filter { it.jobSeekerAccount?.email == "jane.smith@example.com" }.map { it.id }.first()
        // When & Then
        mockMvc.perform(
            post("/api/interviews/$id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/interviews/$id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback))
        )
            .andExpect(status().isCreated)
    }

    @Test
    @WithMockJwt(
        username = "jane.smith@example.com",
        roles = [AccountTypeEnum.MENTOR]
    )
    fun `test interview feedback throws 403 if authenticated user is not job seeker`() {
        // Given
        val feedback = InterviewFeedBackRequest(2, "content")
        val interviews = saveInterviewData()
        val id = interviews.filter { it.jobSeekerAccount?.email == "jane.smith@example.com" }.map { it.id }.first()

        // When & Then
        mockMvc.perform(
            post("/api/interviews/$id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `test interview feedback throws 401 if user is not authenticated`() {
        // Given
        val feedback = InterviewFeedBackRequest(2, "content")
        val interviews = saveInterviewData()
        val id = interviews.filter { it.jobSeekerAccount?.email == "jane.smith@example.com" }.map { it.id }.first()

        // When & Then
        mockMvc.perform(
            post("/api/interviews/$id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockJwt(
        username = "jane.smith@example.com",
        roles = [AccountTypeEnum.JOB_SEEKER]
    )
    fun `test interview feedback throws fails with bad request if request is not valid`() {
        // Given
        val feedback1 = InterviewFeedBackRequest(6, "content")
        val feedback2 = InterviewFeedBackRequest(0, "content")
        val feedback3 = InterviewFeedBackRequest(3, "")

        // When & Then
        mockMvc.perform(
            post("/api/interviews/1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback1))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("status"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("must be less than or equal to 5"))

        mockMvc.perform(
            post("/api/interviews/1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback2))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("status"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("must be greater than or equal to 1"))

        mockMvc.perform(
            post("/api/interviews/1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(feedback3))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("content"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("must not be blank"))
    }


    fun saveInterviewData(): List<Interview> {
        val jobSeeker1 = entityManager.createQuery(
            "select a from Account a where a.email = 'john.doe@example.com'",
            Account::class.java
        ).singleResult
        val jobSeeker2 = entityManager.createQuery(
            "select a from Account a where a.email = 'jane.smith@example.com'",
            Account::class.java
        ).singleResult
        val mentor = entityManager.createQuery(
            "select a from Account a where a.email = 'john.smith@example.com'",
            Account::class.java
        ).singleResult
        val jobPosition = jobPositionRepo.findAll().first()
        val interviewType = interviewTypeRepo.findAll().first()

        // create two timeslots for mentor on passed one ahead of now
        val mentorTimeSlot1 = MentorTimeSlot().apply {
            account = mentor
            fromTime = ZonedDateTime.now().minusDays(2)
            toTime = ZonedDateTime.now().minusDays(2).plusMinutes(30)
            status = MentorTimeSlotEnum.ALLOCATED
        }
        val mentorTimeSlot2 = MentorTimeSlot().apply {
            account = mentor
            fromTime = ZonedDateTime.now().plusDays(2)
            toTime = ZonedDateTime.now().plusDays(2).plusMinutes(30)
            status = MentorTimeSlotEnum.ALLOCATED
        }

        val mentorTimeSlot3 = MentorTimeSlot().apply {
            account = mentor
            fromTime = ZonedDateTime.now().plusMinutes(2)
            toTime = ZonedDateTime.now().plusMinutes(60)
            status = MentorTimeSlotEnum.ALLOCATED
        }

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)
        timeSlotRepo.save(mentorTimeSlot3)

        // create two interviews for that mentor based on timeslots from previous step
        val interview1 = Interview().apply {
            this.jobPosition = jobPosition
            this.interviewType = interviewType
            this.jobSeekerAccount = jobSeeker1
            this.mentorAccount = mentor
            this.timeSlot = mentorTimeSlot1
        }

        val interview2 = Interview().apply {
            this.jobPosition = jobPosition
            this.interviewType = interviewType
            this.jobSeekerAccount = jobSeeker2
            this.mentorAccount = mentor
            this.timeSlot = mentorTimeSlot2
        }

        val interview3 = Interview().apply {
            this.jobPosition = jobPosition
            this.interviewType = interviewType
            this.jobSeekerAccount = jobSeeker1
            this.mentorAccount = mentor
            this.timeSlot = mentorTimeSlot3
            this.meetCode = "abc-efg-hij"
        }
        entityManager.persist(interview1)
        entityManager.persist(interview2)
        entityManager.persist(interview3)
        entityManager.flush()
        return listOf(interview1, interview2, interview3)
    }

    @Test
    @WithMockJwt(username = "jane.smith@example.com", roles = [AccountTypeEnum.JOB_SEEKER])
    fun `test upcoming interviews for job seeker returns page of result successfully for authenticated user`() {
        // get required data set in before each
        val interviews = saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/upcoming")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].intervieweeId").value(interviews[1].mentorAccount?.id))
            .andExpect(jsonPath("$.content[0].intervieweeName").value("${interviews[1].mentorAccount?.firstName} ${interviews[1].mentorAccount?.lastName}"))
            .andExpect(jsonPath("$.content[0].interviewType").value(interviews[1].interviewType?.name))

    }


    @Test
    @WithMockJwt(username = "jane.smith@example.com", roles = [AccountTypeEnum.JOB_SEEKER])
    fun `test upcoming interviews for job seeker returns empty page if job seeker does not have more than one page of interviews`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/upcoming")
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isEmpty)

    }


    @Test
    @WithMockJwt(
        username = "john.smith@example.com",
        roles = [AccountTypeEnum.GUEST, AccountTypeEnum.ADMIN, AccountTypeEnum.MENTOR]
    )
    fun `test upcoming interviews for job seeker returns 403 if authenticated user is not job seeker`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/upcoming")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.JOB_SEEKER])
    fun `test past interview for job seeker returns page of result successfully for authenticated user`() {
        // get required data set in before each
        val interviews = saveInterviewData()
        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(2))
            .andExpect(jsonPath("$.content[0].intervieweeId").value(interviews[0].mentorAccount?.id))
            .andExpect(jsonPath("$.content[0].intervieweeName").value("${interviews[0].mentorAccount?.firstName} ${interviews[0].mentorAccount?.lastName}"))
            .andExpect(jsonPath("$.content[0].interviewType").value(interviews[0].interviewType?.name))
            .andExpect(jsonPath("$.content[1].intervieweeId").value(interviews[1].mentorAccount?.id))
            .andExpect(jsonPath("$.content[1].intervieweeName").value("${interviews[1].mentorAccount?.firstName} ${interviews[1].mentorAccount?.lastName}"))
            .andExpect(jsonPath("$.content[1].interviewType").value(interviews[1].interviewType?.name))
    }


    @Test
    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.JOB_SEEKER])
    fun `test past interviews returns empty page if job seeker does not have more than one page of interviews`() {
        // get required data set in before each
        saveInterviewData()

        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/past")
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isEmpty)

    }


    @Test
    @WithMockJwt(
        username = "john.smith@example.com",
        roles = [AccountTypeEnum.GUEST, AccountTypeEnum.ADMIN, AccountTypeEnum.MENTOR]
    )
    fun `test past interviews for job seeker returns 403 if authenticated user is not job seeker`() {
        // get required data set in before each
        saveInterviewData()
        // When & Then
        mockMvc.perform(
            get("/api/interviews/jobseekers/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }
}
