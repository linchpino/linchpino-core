package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.LinkedInUserInfoResponse
import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.ResetPasswordRequest
import com.linchpino.core.dto.ScheduleRequest
import com.linchpino.core.dto.ScheduleUpdateRequest
import com.linchpino.core.dto.TimeSlot
import com.linchpino.core.dto.UpdateProfileRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.PaymentMethod
import com.linchpino.core.entity.Role
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.security.WithMockBearerToken
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.service.EmailService
import com.linchpino.core.service.LinkedInService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // Ensure rollback after each test
@Import(PostgresContainerConfig::class)
@TestPropertySource(
    properties = [
        "admin.username=testAdmin",
        "admin.password=testPassword"
    ]
)
class AccountControllerTestIT {

    @Autowired
    private lateinit var accountController: AccountController

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mailService: EmailService

    @MockBean
    private lateinit var linkedInService: LinkedInService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var interviewTypeRepository: InterviewTypeRepository

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Test
    fun `test creating jobSeeker account`() {
        val createAccountRequest =
            CreateAccountRequest("John", "Doe", "john.doe@example.com", "@1secret", AccountTypeEnum.JOB_SEEKER.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(createAccountRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.type").value("JOB_SEEKER"))
            .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVATED.name))

        // verify payment method
        val paymentMethod = entityManager.find(
            PaymentMethod::class.java,
            accountRepository.findByEmailIgnoreCase(createAccountRequest.email)!!.id
        )

        assertThat(paymentMethod.account?.email).isEqualTo(createAccountRequest.email)
        assertThat(paymentMethod.type).isEqualTo(PaymentMethodType.FREE)
    }

    @Test
    fun `test creating account with blank firstName results in bad request`() {
        val invalidRequest =
            CreateAccountRequest("", "Doe", "john.doe@example.com", "@1secret", AccountTypeEnum.JOB_SEEKER.value)
        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap", hasSize<Int>(1)))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("firstName"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("firstname is required"))
    }

    @Test
    fun `test creating account with blank lastName results in bad request`() {
        val invalidRequest =
            CreateAccountRequest("John", "", "john.doe@example.com", "@1secret", AccountTypeEnum.JOB_SEEKER.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap", hasSize<Int>(1)))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("lastName"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("lastname is required"))
    }

    @Test
    fun `test creating account with invalid email results in bad request`() {
        val invalidRequest =
            CreateAccountRequest("John", "Doe", "john.doe_example.com", "@1secret", AccountTypeEnum.JOB_SEEKER.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap", hasSize<Int>(1)))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("email"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("email is not valid"))
    }

    @Test
    fun `test creating account with a password that does not match password policy results in bad request`() {
        val invalidRequest =
            CreateAccountRequest("John", "Doe", "john.doe@example.com", "secre", AccountTypeEnum.JOB_SEEKER.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap", hasSize<Int>(1)))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("password"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("Password must be at least 6 character containing alpha-numeric and special characters"))
    }

    @Test
    fun `test creating account with multiple invalid fields results in bad request`() {
        val invalidRequest =
            CreateAccountRequest("", "Doe", "john.doe_example.com", "secret", AccountTypeEnum.GUEST.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap").isArray)
            .andExpect(jsonPath("$.validationErrorMap[*].field", hasItem("email")))
            .andExpect(jsonPath("$.validationErrorMap[*].message", hasItem("email is not valid")))
            .andExpect(jsonPath("$.validationErrorMap[*].field", hasItem("firstName")))
            .andExpect(jsonPath("$.validationErrorMap[*].message", hasItem("firstname is required")))
    }

    @Test
    fun `test creating account with duplicate email results in bad request`() {
        val createAccountRequest =
            CreateAccountRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "@password123",
                AccountTypeEnum.JOB_SEEKER.value
            )

        val createAccountRequestWithDuplicateEmail =
            CreateAccountRequest("Jane", "Doe", "john.doe@example.com", "@password123", AccountTypeEnum.MENTOR.value)

        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(createAccountRequest))
        )
            .andExpect(status().isCreated)


        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(createAccountRequestWithDuplicateEmail))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Unique email violation"))
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `test search for mentors by date and interviewType returns mentors with matched schedule`() {
        saveFakeMentorsWithSchedule()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'System Design'",
            Long::class.java
        ).singleResult

        mockMvc.perform(
            get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-09-09T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].mentorFirstName").value("john"))
            .andExpect(jsonPath("$.[0].mentorLastName").value("doe"))
            .andExpect(jsonPath("$.[1].mentorFirstName").value("josh"))
            .andExpect(jsonPath("$.[1].mentorLastName").value("long"))
    }

    @Test
    fun `test search for mentors by date and interviewType returns empty list with matched schedule and wrong interviewType`() {
        saveFakeMentorsWithSchedule()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'Kotlin Dev'",
            Long::class.java
        ).singleResult

        mockMvc.perform(
            get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-09-09T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(hasSize<Int>(0)))
    }

    @Test
    fun `test search for mentors by date and interviewType returns empty list with matched interviewType but selected date is not schedule`() {
        saveFakeMentorsWithSchedule()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'System Design'",
            Long::class.java
        ).singleResult

        mockMvc.perform(
            get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-09-10T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(hasSize<Int>(0)))
    }


    @Test
    fun `test search for mentors by date and interviewType returns bad request when interviewTypeId not provided`() {

        mockMvc.perform(
            get("/api/accounts/mentors/search")
                .param("date", "2024-03-28T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("interviewTypeId"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("Required request parameter 'interviewTypeId' for method parameter type long is not present"))
    }

    @Test
    fun `test search for mentors by date and interviewType returns bad request when date not provided`() {

        mockMvc.perform(
            get("/api/accounts/mentors/search")
                .param("interviewTypeId", 5L.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("date"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("Required request parameter 'date' for method parameter type ZonedDateTime is not present"))
    }


    @Test
    fun `test activating job seeker account successfully`() {
        // Given
        val externalId = UUID.randomUUID().toString()
        saveFakeJobSeekerAccount(externalId, AccountStatusEnum.DEACTIVATED)
        val activationRequest =
            ActivateJobSeekerAccountRequest(externalId, "updated firstname", "updated last name", "1@secret")
        //
        mockMvc.perform(
            put("/api/accounts/jobseeker/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(activationRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.firstName").value("updated firstname"))
            .andExpect(jsonPath("$.lastName").value("updated last name"))
            .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVATED.name))
    }

    @Test
    fun `test activating job seeker account throws exception when account is already activated`() {
        // Given
        val externalId = UUID.randomUUID().toString()
        saveFakeJobSeekerAccount(externalId, AccountStatusEnum.ACTIVATED)
        val activationRequest =
            ActivateJobSeekerAccountRequest(externalId, "updated firstname", "updated last name", "secure password")
        //
        mockMvc.perform(
            put("/api/accounts/jobseeker/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(activationRequest))
        )
            // todo assert against real exception after exception handling configured
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test activating job seeker account throws exception when account is not found`() {
        // Given
        val externalId = UUID.randomUUID().toString()
        saveFakeJobSeekerAccount(externalId, AccountStatusEnum.DEACTIVATED)
        val activationRequest = ActivateJobSeekerAccountRequest(
            externalId + "change",
            "updated firstname",
            "updated last name",
            "@secret1"
        )
        //
        mockMvc.perform(
            put("/api/accounts/jobseeker/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(activationRequest))
        )
            // todo assert against real exception after exception handling configured
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test register new mentor`() {
        // Given
        val it1 = InterviewType().apply { name = "type1" }
        val it2 = InterviewType().apply { name = "type2" }
        val interviewTypes = interviewTypeRepository.saveAll(listOf(it1, it2))
        val paymentMethodRequest = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = 10.0,
            maxPayment = 25.0
        )
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "@secret1",
            interviewTypeIDs = interviewTypes.map { it.id!! }.toList(),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "https://www.linkedin.com/in/johndoe",
            paymentMethodRequest = paymentMethodRequest,
            iban = "GB82 WEST 1234 5698 7654 32"
        )
        val expectedIBAN = request.iban?.trim()?.replace(" ", "")?.uppercase()

        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.interviewTypeIDs").isArray)
            .andExpect(jsonPath("$.detailsOfExpertise").value("Some expertise"))
            .andExpect(jsonPath("$.linkedInUrl").value("https://www.linkedin.com/in/johndoe"))

        verify(
            mailService,
            times(1)
        ).sendingWelcomeEmailToMentor(
            request.firstName,
            request.lastName,
            request.email
        )

        // verify payment method
        val savedAccount = accountRepository.findByEmailIgnoreCase(request.email)
        val paymentMethod = entityManager.find(
            PaymentMethod::class.java,
            savedAccount!!.id
        )

        assertThat(paymentMethod.account?.email).isEqualTo(request.email)
        assertThat(paymentMethod.type).isEqualTo(PaymentMethodType.PAY_AS_YOU_GO)
        assertThat(paymentMethod.minPayment).isEqualTo(paymentMethodRequest.minPayment)
        assertThat(paymentMethod.maxPayment).isEqualTo(paymentMethodRequest.maxPayment)
        assertThat(savedAccount.iban).isEqualTo(expectedIBAN)
    }

    @Test
    fun `test register new mentor throws exception when no interviewTypeIDs found in database`() {
        // Given
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "@secret1",
            interviewTypeIDs = listOf(1L, 2L),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "https://www.linkedin.com/in/johndoe",
            paymentMethodRequest = PaymentMethodRequest(PaymentMethodType.FREE),
            iban = "GB82 WEST 1234 5698 7654 32"
        )
        //
        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Interview type not found"))
    }

    @Test
    fun `test register mentor with invalid request data`() {
        val invalidRequest = RegisterMentorRequest(
            firstName = "", // Empty first name
            lastName = "Doe",
            email = "john@example.com",
            password = "password",
            interviewTypeIDs = listOf(),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "https://linkedin.com/johndoe",
            paymentMethodRequest = PaymentMethodRequest(PaymentMethodType.FREE),
            iban = "GB82 WEST 1234 5698 7654 33"
        )

        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap[*].field", hasItem("firstName")))
            .andExpect(jsonPath("$.validationErrorMap[*].message", hasItem("firstname is required")))
            .andExpect(jsonPath("$.validationErrorMap[*].field", hasItem("linkedInUrl")))
            .andExpect(jsonPath("$.validationErrorMap[*].message", hasItem("Invalid LinkedIn URL")))
            .andExpect(jsonPath("$.validationErrorMap[*].field", hasItem("interviewTypeIDs")))
            .andExpect(jsonPath("$.validationErrorMap[*].message", hasItem("interviewTypeIDs are required")))

    }

    @Test
    fun `test add timeslots for mentor`() {
        // Given
        val mentor = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)

        val account = Account().apply {
            email = "john.doe@example.com"
        }

        account.addRole(mentor)

        val id = accountRepository.save(account).id!!

        val timeSlots = listOf(
            TimeSlot(
                ZonedDateTime.parse("2024-05-09T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-09T13:30:45+03:00")
            ),
            TimeSlot(
                ZonedDateTime.parse("2024-05-10T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-10T13:30:45+03:00")
            ),
        )
        val request = AddTimeSlotsRequest(id, timeSlots)

        // Then
        mockMvc.perform(
            post("/api/accounts/mentors/timeslots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `test add timeslots for mentor fails if there is no mentor with provided id in database`() {
        // Given
        val timeSlots = listOf(
            TimeSlot(
                ZonedDateTime.parse("2024-05-09T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-09T13:30:45+03:00")
            ),
            TimeSlot(
                ZonedDateTime.parse("2024-05-10T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-10T13:30:45+03:00")
            ),
        )
        val request = AddTimeSlotsRequest(1000, timeSlots)

        // Then
        mockMvc.perform(
            post("/api/accounts/mentors/timeslots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Account entity not found"))

    }

    @Test
    fun `test add timeslots for mentor fails if provided account id does not have MENTOR role`() {
        val jobSeeker = entityManager.find(Role::class.java, AccountTypeEnum.JOB_SEEKER.value)

        val account = Account().apply {
            email = "john.doe@example.com"
        }

        account.addRole(jobSeeker)

        val id = accountRepository.save(account).id!!

        val timeSlots = listOf(
            TimeSlot(
                ZonedDateTime.parse("2024-05-09T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-09T13:30:45+03:00")
            ),
            TimeSlot(
                ZonedDateTime.parse("2024-05-10T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-10T13:30:45+03:00")
            ),
        )
        val request = AddTimeSlotsRequest(id, timeSlots)

        // Then
        mockMvc.perform(
            post("/api/accounts/mentors/timeslots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Account role is invalid"))
    }

    @Test
    fun `test add timeslots for mentor fails if provided timeslot starts after it ends`() {
        val jobSeeker = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)

        val account = Account().apply {
            email = "john.doe@example.com"
        }

        account.addRole(jobSeeker)

        val id = accountRepository.save(account).id!!

        val timeSlots = listOf(
            TimeSlot(
                ZonedDateTime.parse("2024-05-09T14:30:45+03:00"),
                ZonedDateTime.parse("2024-05-09T13:30:45+03:00")
            )
        )
        val request = AddTimeSlotsRequest(id, timeSlots)

        // Then
        mockMvc.perform(
            post("/api/accounts/mentors/timeslots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Timeslot is invalid"))
            .andExpect(jsonPath("$.status").value(400))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns list of matched accounts when lastName is provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "doe")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].roles[0]").value("MENTOR"))
            .andExpect(jsonPath("$.content[0].email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.content[0].avatar").value("avatar image id"))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns list of matched accounts when firstName is provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "john")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].roles[0]").value("MENTOR"))
            .andExpect(jsonPath("$.content[0].email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.content[0].avatar").value("avatar image id"))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns list of matched accounts when role is provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("role", "3")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].roles[0]").value("MENTOR"))
            .andExpect(jsonPath("$.content[0].email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.content[0].avatar").value("avatar image id"))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns list of matched accounts when role and name are provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "doe")
                .param("role", "3")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].roles[0]").value("MENTOR"))
            .andExpect(jsonPath("$.content[0].email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.content[0].avatar").value("avatar image id"))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns list of matched accounts when role and partial ame are provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "ohn")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].roles[0]").value("MENTOR"))
            .andExpect(jsonPath("$.content[0].email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.content[0].avatar").value("avatar image id"))
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns empty list if both role and name are provided and only one matches`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "smith")
                .param("role", "3")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test search account by role or name returns all accounts if no name and role provided`() {
        // Given
        saveAccountsWithRole()

        // When
        mockMvc.perform(
            get("/api/accounts/search")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].firstName").value(hasItems("Jane", "John")))
            .andExpect(jsonPath("$.content[*].lastName").value(hasItems("Smith", "Doe")))
            .andExpect(jsonPath("$.content[?(@.firstName == 'Jane')].roles[0]").value(hasItem("JOB_SEEKER")))
            .andExpect(jsonPath("$.content[?(@.firstName == 'John')].roles[0]").value(hasItem("MENTOR")))

    }

    @WithMockJwt(
        username = "john.doe@example.com",
        roles = [AccountTypeEnum.GUEST, AccountTypeEnum.MENTOR, AccountTypeEnum.JOB_SEEKER]
    )
    @Test
    fun `test search account by role or name throws 403 if user is not admin`() {
        // When
        mockMvc.perform(
            get("/api/accounts/search")
                .param("name", "smith")
                .param("role", "3")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `adminAccountRunner creates admin account`() {
        // The ApplicationContext will trigger the ApplicationRunner automatically

        val admin = accountRepository.findAll().firstOrNull { it.firstName == "admin" && it.lastName == "admin" }
        assertThat(admin).isNotNull
        assertThat(admin?.email).isEqualTo("testAdmin")
        assertThat(admin?.roles()?.map { it.title }).contains(AccountTypeEnum.ADMIN)

        accountController.adminAccountRunner("testAdmin2", "secret")
        val admins = accountRepository.searchByNameOrRole(null, AccountTypeEnum.ADMIN, Pageable.ofSize(10))
        assertThat(admins.size).isEqualTo(10)
        assertThat(admins.content.size).isEqualTo(1)
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test profile throws not found exception if logged in user is not from linkedin and is not present in database`() {
        // When & Then
        mockMvc.perform(
            get("/api/accounts/profile")
        )
            .andExpect(status().isNotFound)
    }

    @WithMockJwt(username = "johndoe@gmail.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test profile returns account summary if user is logged in with Linchpino jwt and exists in database`() {
        // Given
        saveAccountsWithRole()


        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:00+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:00+03:00"),
            listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)
        )

        // When & Then
        mockMvc.perform(
            post("/api/accounts/mentors/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )


        // When & Then
        mockMvc.perform(
            get("/api/accounts/profile")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("johndoe@gmail.com"))
            .andExpect(jsonPath("$.avatar").value("avatar image id"))
            .andExpect(jsonPath("$.detailsOfExpertise").value("test expertise"))
            .andExpect(jsonPath("$.linkedInUrl").value("https://linkedin.com/john"))
            .andExpect(jsonPath("$.iban").value("GB82WEST12345698765432"))
    }

    @WithMockBearerToken(username = "johndoe@gmail.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test profile returns account summary if user is logged in with LinkedIn and exists in database`() {
        // Given
        saveAccountsWithRole()

        `when`(linkedInService.userInfo("dummy token")).thenReturn(
            LinkedInUserInfoResponse(
                "johndoe@gmail.com",
                "John",
                "Doe"
            )
        )

        // When & Then
        mockMvc.perform(
            get("/api/accounts/profile")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("johndoe@gmail.com"))
    }

    @WithMockBearerToken(username = "johndoe@gmail.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test profile saves user and returns summary if user is logged in with LinkedIn and does not exist in database`() {
        // Given
        `when`(linkedInService.userInfo("dummy token")).thenReturn(
            LinkedInUserInfoResponse(
                "johndoe@gmail.com",
                "John",
                "Doe"
            )
        )

        // When & Then
        mockMvc.perform(
            get("/api/accounts/profile")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("johndoe@gmail.com"))

        val savedAccount = accountRepository.findByEmailIgnoreCase("johndoe@gmail.com")
        assertThat(savedAccount).isNotNull
    }

    @WithMockJwt(username = "johndoe@gmail.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test adding schedule for mentor`() {
        // Given
        saveAccountsWithRole()

        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:00+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:00+03:00"),
            listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)
        )

        // When & Then
        mockMvc.perform(
            post("/api/accounts/mentors/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.startTime").value("2024-08-28T09:30:00Z"))
            .andExpect(jsonPath("$.duration").value(60))
            .andExpect(jsonPath("$.accountId").isNumber)
            .andExpect(jsonPath("$.recurrenceType").value("WEEKLY"))
            .andExpect(jsonPath("$.interval").value(3))
            .andExpect(jsonPath("$.endTime").value("2024-12-30T10:30:00Z"))
            .andExpect(jsonPath("$.weekDays[0]").value("FRIDAY"))
            .andExpect(jsonPath("$.weekDays[1]").value("SUNDAY"))
    }

    @Test
    fun `test adding schedule throws 401 if user not authenticated`() {
        // Given
        saveAccountsWithRole()

        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:00+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:00+03:00"),
            listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)
        )

        // When & Then
        mockMvc.perform(
            post("/api/accounts/mentors/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @WithMockJwt(username = "janesmith@gmail.com", roles = [AccountTypeEnum.GUEST, AccountTypeEnum.JOB_SEEKER])
    @Test
    fun `test adding schedule throws 403 if user is not mentor`() {
        // Given
        saveAccountsWithRole()

        val request = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:00+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:00+03:00"),
            listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)
        )

        // When & Then
        mockMvc.perform(
            post("/api/accounts/mentors/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }


    @WithMockJwt(username = "johndoe@gmail.com")
    @Test
    fun `test reset password resets password successfully`() {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = BCryptPasswordEncoder().encode("secret")
        }
        accountRepository.save(john)

        val request = ResetPasswordRequest("secret", "secret2")

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)

    }

    @WithMockJwt(username = "johndoe@gmail.com")
    @Test
    fun `test reset password throws exception if new password does not meet password policy`() {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = BCryptPasswordEncoder().encode("secret")
        }
        accountRepository.save(john)

        val request = ResetPasswordRequest("secret", "s2")

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("newPassword"))
    }

    @WithMockJwt(username = "johndoe@gmail.com")
    @Test
    fun `test reset password throws exception if currentPassword does not match currentPassword in the request`() {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = BCryptPasswordEncoder().encode("secret")
        }
        accountRepository.save(john)

        val request = ResetPasswordRequest("wrongCurrentPassword", "secret2")

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid password"))
    }


    @Test
    fun `test reset password can not be called without authentication`() {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = BCryptPasswordEncoder().encode("secret")
        }
        accountRepository.save(john)

        val request = ResetPasswordRequest("wrongCurrentPassword", "secret2")

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @WithMockJwt(username = "john.doe@example.com")
    @Test
    fun `test update profile`() {
        // given
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            linkedInUrl = "linkedin.com/in/john"
            detailsOfExpertise = "john's details"
            iban = "iban"
        }
        accountRepository.save(john)
        val paymentMethod = PaymentMethod().apply {
            type = PaymentMethodType.FREE
            account = john
        }

        entityManager.persist(paymentMethod)

        val request = UpdateProfileRequest(
            "JohnUpdate",
            "DoeUpdate",
            "detailsUpdate",
            "GB82WEST12345698765432",
            "linkedin.com/in/johnUpdate",
            PaymentMethodRequest(
                PaymentMethodType.FIX_PRICE,
                fixRate = 50.0
            )
        )

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value(request.firstName))
            .andExpect(jsonPath("$.lastName").value(request.lastName))
            .andExpect(jsonPath("$.email").value(john.email))
            .andExpect(jsonPath("$.iban").value(request.iban))
            .andExpect(jsonPath("$.linkedInUrl").value(request.linkedInUrl))
            .andExpect(jsonPath("$.detailsOfExpertise").value(request.detailsOfExpertise))
            .andExpect(jsonPath("$.paymentMethod.type").value(request.paymentMethodRequest?.type?.name))
            .andExpect(jsonPath("$.paymentMethod.fixRate").value(request.paymentMethodRequest?.fixRate))

    }

    @Test
    fun `update profile needs authentication`() {
        // Given
        val request = UpdateProfileRequest(
            "JohnUpdate",
            "DoeUpdate",
            "detailsUpdate",
            "GB82WEST12345698765432",
            "linkedin.com/in/johnUpdate",
            PaymentMethodRequest(
                PaymentMethodType.FIX_PRICE,
                fixRate = 50.0
            )
        )

        // When & Then
        mockMvc.perform(
            put("/api/accounts/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `should update schedule`() {
        saveFakeMentorsWithSchedule()
        val request = ScheduleUpdateRequest(
            startTime = ZonedDateTime.parse("2024-09-28T12:30:45+03:00"),
            endTime = null,
            duration = null,
            recurrenceType = RecurrenceType.MONTHLY,
            interval = null,
            monthDays = listOf(1, 15)
        )

        mockMvc.perform(
            put("/api/accounts/mentors/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerModules(JavaTimeModule()).writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.startTime").value("2024-09-28T09:30:45Z"))
            .andExpect(jsonPath("$.recurrenceType").value(request.recurrenceType?.name))
            .andExpect(jsonPath("$.monthDays[0]").value(request.monthDays[0]))
            .andExpect(jsonPath("$.monthDays[1]").value(request.monthDays[1]))
    }


    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `should delete schedule`() {
        saveFakeMentorsWithSchedule()

        mockMvc.perform(delete("/api/accounts/mentors/schedule"))
            .andExpect(status().isNoContent)

        val john = entityManager.createQuery("select a from Account a where a.email = :email", Account::class.java)
            .setParameter("email", "john.doe@example.com")
            .singleResult
        val johnSchedule = entityManager.createQuery("select s from Schedule s where s.account.id =:id")
            .setParameter("id", john.id)
            .resultList

        assertThat(johnSchedule).isEmpty()
    }

    private fun saveAccountsWithRole() {
        val mentorRole = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)
        val jobSeekerRole = entityManager.find(Role::class.java, AccountTypeEnum.JOB_SEEKER.value)

        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
            detailsOfExpertise = "test expertise"
            linkedInUrl = "https://linkedin.com/john"
            iban = "GB82WEST12345698765432"
            avatar = "avatar image id"
        }

        val jane = Account().apply {
            firstName = "Jane"
            lastName = "Smith"
            email = "janesmith@gmail.com"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }

        john.addRole(mentorRole)
        jane.addRole(jobSeekerRole)

        accountRepository.save(john)
        accountRepository.save(jane)
    }

    private fun saveFakeJobSeekerAccount(externalId: String, accountStatus: AccountStatusEnum) {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = "secret"
            status = accountStatus
            this.externalId = externalId
        }
        val jobSeekerRole = entityManager.find(Role::class.java, AccountTypeEnum.JOB_SEEKER.value)
        john.addRole(jobSeekerRole)
        accountRepository.save(john)
    }

    private fun saveFakeMentorsWithSchedule() {
        // Given
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule1 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }

        val schedule2 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        }
        val schedule3 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(15, 25)
        }

        val account1 = Account().apply {
            firstName = "john"
            lastName = "doe"
            email = "john.doe@example.com"
        }
        val systemDesign = InterviewType().apply {
            this.name = "System Design"
        }
        account1.addInterviewType(systemDesign)

        val account2 = Account().apply {
            firstName = "josh"
            lastName = "long"
            email = "jlong@example.com"
        }
        account2.addInterviewType(systemDesign)

        val account3 = Account().apply {
            firstName = "jane"
            lastName = "smith"
            email = "jane.smith@example.com"
        }
        account3.addInterviewType(InterviewType().apply {
            name = "Kotlin Dev"
        })
        val mentorRole = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)
        account1.addRole(mentorRole)
        account2.addRole(mentorRole)
        account3.addRole(mentorRole)

        accountRepository.save(account1).also {
            schedule1.account = it
            entityManager.persist(schedule1)
            it.schedule = schedule1
        }
        accountRepository.save(account2).also {
            schedule2.account = it
            entityManager.persist(schedule2)
            it.schedule = schedule2
        }
        accountRepository.save(account3).also {
            schedule3.account = it
            entityManager.persist(schedule3)
            it.schedule = schedule3
        }
    }

    private fun saveFakeMentorsWithInterviewTypeAndTimeSlots() {
        val john = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }
        val systemDesign = InterviewType().apply {
            this.name = "System Design"
        }
        john.addInterviewType(systemDesign)
        john.addInterviewType(InterviewType().apply {
            this.name = "Backend Engineering"
        })
        val mentorRole = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)
        john.addRole(mentorRole)
        accountRepository.save(john)

        val jane = Account().apply {
            firstName = "Jane"
            lastName = "Smith"
            email = "janesmith@gmail.com"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }
        jane.addRole(mentorRole)
        jane.addInterviewType(systemDesign)
        accountRepository.save(jane)

        val bob = Account().apply {
            firstName = "Bob"
            lastName = "Martin"
            email = "bob@gmail.com"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }
        bob.addInterviewType(InterviewType().apply {
            name = "Kotlin Dev"
        })
        bob.addRole(mentorRole)
        accountRepository.save(bob)

        MentorTimeSlot().apply {
            account = john
            fromTime = ZonedDateTime.parse("2024-03-26T16:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-26T17:00:00+00:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }.also {
            entityManager.persist(it)
        }

        MentorTimeSlot().apply {
            account = john
            fromTime = ZonedDateTime.parse("2024-03-26T13:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-26T14:00:00+00:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }.also {
            entityManager.persist(it)
        }

        MentorTimeSlot().apply {
            account = john
            fromTime = ZonedDateTime.parse("2024-03-26T09:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-26T10:00:00+00:00")
            status = MentorTimeSlotEnum.DRAFT
        }.also {
            entityManager.persist(it)
        }

        MentorTimeSlot().apply {
            account = jane
            fromTime = ZonedDateTime.parse("2024-03-26T09:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-26T10:00:00+00:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }.also {
            entityManager.persist(it)
        }

        MentorTimeSlot().apply {
            account = jane
            fromTime = ZonedDateTime.parse("2024-03-26T20:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-26T21:00:00+00:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }.also {
            entityManager.persist(it)
        }

        MentorTimeSlot().apply {
            account = bob
            fromTime = ZonedDateTime.parse("2024-03-27T07:00:00+00:00")
            toTime = ZonedDateTime.parse("2024-03-27T08:00:00+00:00")
            status = MentorTimeSlotEnum.AVAILABLE
        }.also {
            entityManager.persist(it)
        }

    }

    /*
    @WithMockJwt(username = "john.doe@example.com", roles = [AccountTypeEnum.MENTOR])
    @Test
    fun `test uploadProfileImage success`() {

        // Arrange
        val fileName = "profile.jpg"
        val account = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "johndoe@gmail.com"
            password = "secret"
        }

        accountRepository.save(account)

        val file = MockMultipartFile("file", fileName, "image/jpeg", "test image content".toByteArray())
        val response = AddProfileImageResponse(fileName)


        // Act & Assert
        mockMvc.perform(
            multipart("/api/accounts/image")
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().json(ObjectMapper().writeValueAsString(response)))

    }
*/
}
