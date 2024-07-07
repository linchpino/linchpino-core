package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.TimeSlot
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.service.EmailService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // Ensure rollback after each test
@Import(PostgresContainerConfig::class)
class AccountControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mailService: EmailService

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
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.type").value("JOB_SEEKER"))
            .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVATED.name))
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isCreated)


        mockMvc.perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(createAccountRequestWithDuplicateEmail))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Unique email violation"))
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `test search for mentors by date and interviewType returns only one time slot per matched mentor`() {
        // Given
        saveFakeMentorsWithInterviewTypeAndTimeSlots()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'System Design'",
            Long::class.java
        ).singleResult

        // Perform GET request and verify response
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-03-26T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].mentorFirstName").value("John"))
            .andExpect(jsonPath("$.[0].mentorLastName").value("Doe"))
            .andExpect(jsonPath("$.[0].from").value("2024-03-26T13:00:00Z"))
            .andExpect(jsonPath("$.[0].to").value("2024-03-26T14:00:00Z"))
            .andExpect(jsonPath("$.[1].mentorFirstName").value("Jane"))
            .andExpect(jsonPath("$.[1].mentorLastName").value("Smith"))
            .andExpect(jsonPath("$.[1].from").value("2024-03-26T09:00:00Z"))
            .andExpect(jsonPath("$.[1].to").value("2024-03-26T10:00:00Z"))
    }

    @Test
    fun `test search for mentors by date and interviewType returns only one time slot per matched mentor with timezone applied`() {
        // Given
        saveFakeMentorsWithInterviewTypeAndTimeSlots()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'System Design'",
            Long::class.java
        ).singleResult

        // Perform GET request and verify response
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-03-27T00:00:00+10:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").value(hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].mentorFirstName").value("John"))
            .andExpect(jsonPath("$.[0].mentorLastName").value("Doe"))
            .andExpect(jsonPath("$.[0].from").value("2024-03-26T16:00:00Z"))
            .andExpect(jsonPath("$.[0].to").value("2024-03-26T17:00:00Z"))
            .andExpect(jsonPath("$.[1].mentorFirstName").value("Jane"))
            .andExpect(jsonPath("$.[1].mentorLastName").value("Smith"))
            .andExpect(jsonPath("$.[1].from").value("2024-03-26T20:00:00Z"))
            .andExpect(jsonPath("$.[1].to").value("2024-03-26T21:00:00Z"))
    }

    @Test
    fun `test search for mentors by date and interviewType returns empty list when interviewType matches the provided interviewTypeId`() {
        // Given
        saveFakeMentorsWithInterviewTypeAndTimeSlots()

        // Perform GET request and verify response
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("interviewTypeId", (-1).toString())
                .param("date", "2024-03-26T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `test search for mentors by date and interviewType returns empty list when no mentor is available on the provided date`() {
        // Given
        saveFakeMentorsWithInterviewTypeAndTimeSlots()
        val id = entityManager.createQuery(
            "select id from InterviewType where name = 'System Design'",
            Long::class.java
        ).singleResult

        // Perform GET request and verify response
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("interviewTypeId", id.toString())
                .param("date", "2024-03-28T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `test search for mentors by date and interviewType returns bad request when interviewTypeId not provided`() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("date", "2024-03-28T00:00:00+00:00")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.validationErrorMap[0].field").value("interviewTypeId"))
            .andExpect(jsonPath("$.validationErrorMap[0].message").value("Required request parameter 'interviewTypeId' for method parameter type long is not present"))
    }

    @Test
    fun `test search for mentors by date and interviewType returns bad request when date not provided`() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/accounts/mentors/search")
                .param("interviewTypeId", 5L.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isOk)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `test register new mentor`() {
        // Given
        val it1 = InterviewType().apply { name = "type1" }
        val it2 = InterviewType().apply { name = "type2" }
        val interviewTypes = interviewTypeRepository.saveAll(listOf(it1, it2))
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "@secret1",
            interviewTypeIDs = interviewTypes.map { it.id!! }.toList(),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "https://www.linkedin.com/in/johndoe"
        )

        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
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
            linkedInUrl = "https://www.linkedin.com/in/johndoe"
        )
        //
        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
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
            linkedInUrl = "https://linkedin.com/johndoe"
        )

        mockMvc.perform(
            post("/api/accounts/mentors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isCreated)
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
            .andExpect(MockMvcResultMatchers.status().isNotFound)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Timeslot is invalid"))
            .andExpect(jsonPath("$.status").value(400))
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
            multipart("/api/accounts/${account.id}/image")
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().json(ObjectMapper().writeValueAsString(response)))

    }
*/
}
