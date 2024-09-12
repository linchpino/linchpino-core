package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.security.WithMockJwt
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

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
class AccountAdminControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test reset password for any account is successful`() {
        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
        }

        accountRepository.save(account)

        val request = ResetAccountPasswordRequest(1, "newPassword")

        mockMvc.perform(
            put("/api/admin/accounts/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

    @WithMockJwt(username = "admin@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test reset password for any account returns 404 if provided account id is wrong`() {
        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
        }

        accountRepository.save(account)

        val request = ResetAccountPasswordRequest(1000, "newPassword")

        mockMvc.perform(
            put("/api/admin/accounts/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @WithMockJwt(
        username = "admin@example.com",
        roles = [AccountTypeEnum.MENTOR, AccountTypeEnum.GUEST, AccountTypeEnum.JOB_SEEKER]
    )
    @Test
    fun `test only admin can call reset password`() {
        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
        }

        accountRepository.save(account)

        val request = ResetAccountPasswordRequest(1, "newPassword")

        mockMvc.perform(
            put("/api/admin/accounts/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }
}
