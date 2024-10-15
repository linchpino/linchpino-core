package com.linchpino.core.controller

import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Payment
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.PaymentStatus
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import java.time.ZonedDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@Import(PostgresContainerConfig::class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTestIT {


    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @BeforeEach
    fun setup() {
        val jobPosition = createJobPosition("job position")
        val interviewTypes = createInterviewTypes(jobPosition, 3)
        val mentor = createAccount("john", "doe", "john.doe@example.com")
        val jobSeeker = createAccount("jane", "smith", "jane.smith@example.com")
        val timeSlots = createMentorTimeSlots(mentor, 3)
        val interviews = createInterviews(mentor, jobPosition, timeSlots, interviewTypes, jobSeeker)
        interviews.forEach { entityManager.persist(it) }
    }

    @Test
    fun `create payment`() {
        val id = entityManager.createQuery("from Interview", Interview::class.java).resultList.firstOrNull()?.id

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                    "interviewId": ${id},
                    "refNumber": "123"
                }
            """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name))
            .andExpect(jsonPath("$.refNumber").value("123"))
    }

    @Test
    fun `can not create payment with duplicate refNumber`() {
        val interview = entityManager.createQuery("from Interview", Interview::class.java).resultList.firstOrNull()
        Payment().apply {
            refNumber = "123"
            this.interview = interview
        }.also {
            entityManager.persist(it)
        }

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                    "interviewId": ${interview?.id},
                    "refNumber": "123"
                }
            """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Unique refNumber violated for Payment"))
    }

    private fun createJobPosition(title: String): JobPosition {
        return JobPosition().apply {
            this.title = title
        }.also {
            entityManager.persist(it)
        }
    }

    private fun createInterviewTypes(jobPosition: JobPosition, count: Int): List<InterviewType> {
        return (1..count).map {
            InterviewType().apply {
                this.jobPositions.add(jobPosition)
                this.name = "interview" + Random().nextInt(100).toString()
            }.also {
                entityManager.persist(it)
            }
        }
    }

    private fun createAccount(firstName: String, lastName: String, email: String): Account {
        return Account().apply {
            this.firstName = firstName
            this.lastName = lastName
            this.email = email
        }.also {
            entityManager.persist(it)
        }
    }

    private fun createMentorTimeSlots(mentor: Account, count: Int): List<MentorTimeSlot> {
        return (1..count).map {
            MentorTimeSlot().apply {
                this.account = mentor
                this.fromTime = ZonedDateTime.now()
                this.toTime = ZonedDateTime.now().plusMinutes(60)
                this.status = MentorTimeSlotEnum.AVAILABLE
            }.also {
                entityManager.persist(it)
            }
        }
    }

    private fun createInterviews(
        mentor: Account,
        jobPosition: JobPosition,
        timeSlots: List<MentorTimeSlot>,
        interviewTypes: List<InterviewType>,
        jobSeeker: Account
    ): List<Interview> {
        return (0..2).map { i ->
            Interview().apply {
                this.mentorAccount = mentor
                this.jobPosition = jobPosition
                this.timeSlot = timeSlots[i]
                this.interviewType = interviewTypes[i]
                this.jobSeekerAccount = jobSeeker
            }.also {
                entityManager.persist(it)
            }
        }
    }
}
