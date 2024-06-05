package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.security.WithMockJwt
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

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

        val mentorAcc = Account().apply {
            firstName = "Mentor_1"
            lastName = "Mentoriii"
            email = "john.smith@example.com"
            password = "password_Mentoriii"
        }
        mentorAcc.addRole(mentorRole)
        mentorAccRepo.save(mentorAcc)

        val position = JobPosition().apply {
            title = "Test Job"
        }
        jobPositionRepo.save(position)

        val typeInterview = InterviewType().apply {
            name = "Test Interview Type"
        }
        interviewTypeRepo.save(typeInterview)

        val mentorTimeSlot1 = MentorTimeSlot().apply {
            account = mentorAcc
            fromTime = ZonedDateTime.now().plusDays(1)
            toTime = ZonedDateTime.now().plusDays(1)
            status = MentorTimeSlotEnum.AVAILABLE
        }
        val mentorTimeSlot2 = MentorTimeSlot().apply {
            account = mentorAcc
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

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().first().id!!,
            mentorAccRepo.findAll().first { it.roles().map { role -> role.title }.contains(AccountTypeEnum.MENTOR) }.id!!,
            john.email
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(jsonPath("$.timeSlotId").value(request.timeSlotId))
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
    }

    @Test
    fun `test with wrong email address results in bad request`() {
        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().first().id!!,
            AccountTypeEnum.MENTOR.value.toLong(),
            "zsdvfzsxd"
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `test with not exist email address result in creating a silent account for job seeker`() {

        val interviewCaptor: ArgumentCaptor<Interview> = ArgumentCaptor.forClass(Interview::class.java)
        val mentorAccount = entityManager.createQuery(
            "select a from Account a where email = 'john.smith@example.com'",
            Account::class.java
        ).singleResult

        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().last().id!!,
            mentorAccount.id!!,
            "mahsa.saeedy@gmail.com"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(jsonPath("$.timeSlotId").value(request.timeSlotId))
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
    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test upcoming interviews returns page of result successfully for authenticated user`() {
        // get required data set in before each
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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        val interviews = listOf(interview1, interview2)

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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        listOf(interview1, interview2)

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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        listOf(interview1, interview2)

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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        var interviews = listOf(interview1, interview2)

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].intervieweeId").value(interviews[0].jobSeekerAccount?.id))
            .andExpect(jsonPath("$.content[0].intervieweeName").value("${interviews[0].jobSeekerAccount?.firstName} ${interviews[0].jobSeekerAccount?.lastName}"))
            .andExpect(jsonPath("$.content[0].interviewType").value(interviews[0].interviewType?.name))
    }

    @Test
    @WithMockJwt(username = "john.smith@example.com", roles = [AccountTypeEnum.MENTOR])
    fun `test past interviews returns empty page if mentor does not have more than one page of interviews`() {
        // get required data set in before each
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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        listOf(interview1, interview2)

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

        timeSlotRepo.save(mentorTimeSlot1)
        timeSlotRepo.save(mentorTimeSlot2)

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

        entityManager.persist(interview1)
        entityManager.persist(interview2)
        listOf(interview1, interview2)

        // When & Then
        mockMvc.perform(
            get("/api/interviews/mentors/past")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isForbidden)
    }
}
