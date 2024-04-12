package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@SpringBootTest
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

    @BeforeEach
    fun init() {
        val jobSeekerAcc = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
            type = AccountTypeEnum.JOB_SEEKER
        }
        jobSeekerAccRepo.save(jobSeekerAcc)

        val mentorAcc = Account().apply {
            firstName = "Mentor_1"
            lastName = "Mentoriii"
            email = "Mentor_1.Mentoriii@example.com"
            password = "password_Mentoriii"
            type = AccountTypeEnum.MENTOR
        }
        mentorAccRepo.save(mentorAcc)

        val position = JobPosition().apply {
            title = "Test Job"
        }
        jobPositionRepo.save(position)

        val typeInterview = InterviewType().apply {
            name = "Test Interview Type"
        }
        interviewTypeRepo.save(typeInterview)

        val mentorTimeSlot = MentorTimeSlot().apply {
            account = mentorAcc
            fromTime = ZonedDateTime.now()
            toTime = ZonedDateTime.now()
            status = MentorTimeSlotEnum.AVAILABLE
        }
        timeSlotRepo.save(mentorTimeSlot)
    }

    @Test
    fun `test with existed email address result in creating a new interview for job seeker`() {
        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().first().id!!,
            mentorAccRepo.findAll().first { it.type == AccountTypeEnum.MENTOR }.id!!,
            "john.doe@example.com"
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeSlotId").value(request.timeSlotId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.mentorAccountId").value(request.mentorAccId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobSeekerEmail").value("john.doe@example.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewId").isNumber)
    }

    @Test
    fun `test with wrong email address results in bad request`() {
        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().first().id!!,
            mentorAccRepo.findAll().first { it.type == AccountTypeEnum.MENTOR }.id!!,
            "zsdvfzsxd"
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `test with not exist email address result in creating a silent account for job seeker`() {
        val request = CreateInterviewRequest(
            jobPositionRepo.findAll().first().id!!,
            interviewTypeRepo.findAll().first().id!!,
            timeSlotRepo.findAll().first().id!!,
            mentorAccRepo.findAll().first { it.type == AccountTypeEnum.MENTOR }.id!!,
            "test@gmail.com"
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobPositionId").value(request.jobPositionId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewTypeId").value(request.interviewTypeId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeSlotId").value(request.timeSlotId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.mentorAccountId").value(request.mentorAccId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobSeekerEmail").value("test@gmail.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewId").isNumber)
    }
}
