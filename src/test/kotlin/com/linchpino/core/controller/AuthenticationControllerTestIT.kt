package com.linchpino.core.controller

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.TokenResponse
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.security.RSAKeys
import com.linchpino.core.service.AccountService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // Ensure rollback after each test
@Import(PostgresContainerConfig::class)
class AuthenticationControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @BeforeEach
    fun init() {
        // create an account and activate it
        val createAccountRequest = CreateAccountRequest(
            "John", "Doe", "john.doe@example.com", "password123", AccountTypeEnum.GUEST.value
        )
        accountService.createAccount(createAccountRequest)
        val account = accountRepository.findByEmailIgnoreCase("john.doe@example.com")
        account?.status = AccountStatusEnum.ACTIVATED
        accountRepository.save(account!!)

        // account with two roles
        val firstRole = Role().apply { roleName = AccountTypeEnum.JOB_SEEKER }
        val secondRole = Role().apply { roleName = AccountTypeEnum.MENTOR }
        val accountRoles = listOf(account)
        accountRoles.first { it.email == "john.doe@example.com" }.apply {
            addRole(firstRole)
            addRole(secondRole)
        }
        accountRepository.saveAll(accountRoles)

        val createAccountRequestInactive = CreateAccountRequest(
            "Jane", "Smith", "jane.smith@example.com", "password123", AccountTypeEnum.GUEST.value
        )
        accountService.createAccount(createAccountRequestInactive)
        val inactiveAccount = accountRepository.findByEmailIgnoreCase("jane.smith@example.com")
        inactiveAccount?.status = AccountStatusEnum.DEACTIVATED
        accountRepository.save(inactiveAccount!!)
    }

    private fun jwtDecoder(keys: RSAKeys): NimbusJwtDecoder = NimbusJwtDecoder.withPublicKey(keys.publicKey).build()
    private fun generateKeyPair(): RSAKeys {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.generateKeyPair()
        return RSAKeys(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
    }

    @Test
    fun `test login with correct username and password generates token for user`() {

        val responseBody = mockMvc.perform(
            post("/login")
                .with(httpBasic("john.doe@example.com", "password123"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
            .andReturn()

        // to parse java Instance
        val mapper = jacksonObjectMapper().apply {
            registerModules(JavaTimeModule())
        }

        val tokenResponse: TokenResponse = mapper.readValue(responseBody.response.contentAsString)
        assertThat(tokenResponse.expiresAt).isCloseTo(
            Instant.now().plus(60, ChronoUnit.MINUTES), TemporalUnitWithinOffset(10, ChronoUnit.SECONDS)
        )
    }

    @Test
    fun `test login with two roles and generates token for user`() {
        val responseBody = mockMvc.perform(
            post("/login")
                .with(httpBasic("john.doe@example.com", "password123"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
            .andReturn()

        // to parse java Instance
        val mapper = jacksonObjectMapper().apply {
            registerModules(JavaTimeModule())
        }

        val keyPair = generateKeyPair()
        val jwtDecoder = jwtDecoder(keyPair)
        val tokenResponse: TokenResponse = mapper.readValue(responseBody.response.contentAsString)
        assertThat(tokenResponse.expiresAt).isCloseTo(
            Instant.now().plus(60, ChronoUnit.MINUTES), TemporalUnitWithinOffset(10, ChronoUnit.SECONDS)
        )
        assertThat(jwtDecoder.decode(tokenResponse.token).getClaim("scope") as String).isEqualTo("JOB_SEEKER MENTOR")
    }

    @Test
    fun `test login ignores case for username`() {

        mockMvc.perform(
            post("/login").with(httpBasic("JoHn.Doe@Example.COM", "password123"))
        ).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
    }


    @Test
    fun `test login with wrong username returns unauthorized response code`() {

        mockMvc.perform(
            post("/login").with(httpBasic("wrongEmail", "password123"))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `test login with wrong password returns unauthorized response code`() {

        mockMvc.perform(
            post("/login").with(httpBasic("john.doe@example.com", "wrongPassword"))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `test login with returns unauthorized response code when account is inactive`() {

        mockMvc.perform(
            post("/login").with(httpBasic("jane.smith@example.com", "password123"))
        ).andExpect(status().isUnauthorized)
    }
}
