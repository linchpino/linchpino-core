package com.linchpino.core.controller

import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Payment
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.PaymentStatus
import com.linchpino.core.security.WithMockJwt
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import java.math.BigDecimal
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@Import(PostgresContainerConfig::class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentAdminControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    lateinit var entityManager: EntityManager

    private val payments = listOf(
        Payment().apply {
            refNumber = "ref_number_1"
            amount = BigDecimal("12.5")
            status = PaymentStatus.PENDING
        },
        Payment().apply {
            refNumber = "ref_number_2"
            amount = BigDecimal("13.5")
            status = PaymentStatus.VERIFIED
        },
        Payment().apply {
            refNumber = "ref_number_3"
            amount = BigDecimal("17.5")
            status = PaymentStatus.REJECTED
        },
    )

    @BeforeEach
    fun setup() {
        val jobPosition = createJobPosition("job position")
        val interviewTypes = createInterviewTypes(jobPosition, 3)
        val mentor = createAccount("john", "doe", "john.doe@example.com")
        val jobSeeker = createAccount("jane", "smith", "jane.smith@example.com")
        val timeSlots = createMentorTimeSlots(mentor, 3)
        val interviews = createInterviews(mentor, jobPosition, timeSlots, interviewTypes, jobSeeker)
        payments.forEachIndexed { index, payment ->
            payment.interview = interviews[index]
            entityManager.persist(payment)
        }
//        associatePaymentsWithInterviews(interviews)
    }


    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `search payments should return page of all payments when no query param is provided`() {
        mockMvc.perform(get("/api/admin/payments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].refNumber").value("ref_number_1"))
            .andExpect(jsonPath("$.content[0].amount").value("12.5"))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"))

            .andExpect(jsonPath("$.content[1].refNumber").value("ref_number_2"))
            .andExpect(jsonPath("$.content[1].amount").value("13.5"))
            .andExpect(jsonPath("$.content[1].status").value("VERIFIED"))

            .andExpect(jsonPath("$.content[2].refNumber").value("ref_number_3"))
            .andExpect(jsonPath("$.content[2].amount").value("17.5"))
            .andExpect(jsonPath("$.content[2].status").value("REJECTED"))

            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.numberOfElements").value(3))
    }

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `search payments should return only pending payments when status is provided`() {

        mockMvc.perform(get("/api/admin/payments").param("status", "PENDING"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].refNumber").value("ref_number_1"))
            .andExpect(jsonPath("$.content[0].amount").value("12.5"))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"))


            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.numberOfElements").value(1))
    }

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `search payments should return only the payment with the specified refNumber when provided as a query parameter`() {

        val expectedRefNumber = payments.first { it.refNumber == "ref_number_1" }.refNumber

        mockMvc.perform(get("/api/admin/payments").param("refNumber", expectedRefNumber))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].refNumber").value(expectedRefNumber))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.content[0].amount").value("12.5"))


            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.numberOfElements").value(1))
    }

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `search payments should return only the payment with the specified interviewId as a query parameter`() {

        val interviewId = payments.map { it.interview?.id }.firstOrNull()
        mockMvc.perform(get("/api/admin/payments").param("interviewId", "$interviewId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.content[0].interviewId").value(interviewId))


            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.numberOfElements").value(1))
    }


    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `verifyPayment should verify the payment with the correct id and amount`() {
        val payment = entityManager.createQuery("select p from Payment p where status = :status", Payment::class.java)
            .setParameter("status", PaymentStatus.PENDING)
            .resultList
            .first()

        mockMvc.perform(
            post("/api/admin/payments/{id}/verify", payment.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                    "amount": 12.50
                }
            """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(payment.id))
            .andExpect(jsonPath("$.interviewId").value(payment.interview?.id))
            .andExpect(jsonPath("$.refNumber").value(payment.refNumber))
            .andExpect(jsonPath("$.amount").value(payment.amount.toString()))
            .andExpect(jsonPath("$.status").value(PaymentStatus.VERIFIED.name))
    }

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `rejectPayment should reject the payment with the correct id and return PaymentResponse`() {
        val payment = entityManager.createQuery("select p from Payment p where status = :status", Payment::class.java)
            .setParameter("status", PaymentStatus.VERIFIED)
            .resultList
            .first()



        mockMvc.perform(
            post("/api/admin/payments/{id}/reject", payment.id)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(payment.id))
            .andExpect(jsonPath("$.interviewId").value(payment.interview?.id))
            .andExpect(jsonPath("$.refNumber").value(payment.refNumber))
            .andExpect(jsonPath("$.amount").value(payment.amount.toString()))
            .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.name))
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

    private fun associatePaymentsWithInterviews(interviews: List<Interview>) {
        interviews.forEachIndexed { i, interview ->
            payments[i].apply {
                this.interview = Interview().apply { id = interview.id }
            }.also {
                entityManager.persist(it)
            }
        }
    }
}
