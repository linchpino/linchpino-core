package com.linchpino.core.controller

import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.service.AccountService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
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
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)
        accountService.createAccount(createAccountRequest)
        val account = accountRepository.findByEmailIgnoreCase("john.doe@example.com")
        account?.status = AccountStatusEnum.ACTIVATED
        accountRepository.save(account!!)
    }

    @Test
    fun `test login with correct username and password generates token for user`() {

        mockMvc.perform(
            post("/login")
                .with(httpBasic("john.doe@example.com", "password123"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
    }

    @Test
    fun `test login with wrong username returns unauthorized response code`() {

        mockMvc.perform(
            post("/login")
                .with(httpBasic("wrongEmail", "password123"))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `test login with wrong password returns unauthorized response code`() {

        mockMvc.perform(
            post("/login")
                .with(httpBasic("john.doe@example.com", "wrongPassword"))
        )
            .andExpect(status().isUnauthorized)
    }
}