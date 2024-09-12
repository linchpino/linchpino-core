package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.security.WithMockJwt
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
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

    @PersistenceContext
    lateinit var entityManager: EntityManager

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

    @WithMockJwt(
        username = "admin@example.com",
        roles = [AccountTypeEnum.ADMIN]
    )
    @Test
    fun `test admin can update roles and status of any account`() {
        val account = Account().apply {
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }

        val mentorRole = entityManager.find(Role::class.java, AccountTypeEnum.MENTOR.value)
        account.addRole(mentorRole)

        accountRepository.save(account)

        val request = UpdateAccountRequestByAdmin(account.id!!, listOf(1, 2), AccountStatusEnum.DEACTIVATED.value)

        mockMvc.perform(
            put("/api/admin/accounts/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)

        val savedAccount = accountRepository.findByIdOrNull(account.id)
        assertThat(savedAccount?.status).isEqualTo(AccountStatusEnum.DEACTIVATED)
        assertThat(
            savedAccount?.roles()?.map { it.title }).containsExactlyInAnyOrderElementsOf(
            listOf(
                AccountTypeEnum.GUEST,
                AccountTypeEnum.JOB_SEEKER
            )
        )
    }

    @WithMockJwt(
        username = "admin@example.com",
        roles = [AccountTypeEnum.MENTOR, AccountTypeEnum.GUEST, AccountTypeEnum.JOB_SEEKER]
    )
    @Test
    fun `test only admin can update roles and status of any account`() {

        val request = UpdateAccountRequestByAdmin(1, listOf(1, 2), AccountStatusEnum.DEACTIVATED.value)

        mockMvc.perform(
            put("/api/admin/accounts/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)

    }
}
