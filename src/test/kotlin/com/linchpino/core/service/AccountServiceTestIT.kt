package com.linchpino.core.service

import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Import(PostgresContainerConfig::class)
class AccountServiceTestIT {


	@Autowired
	private lateinit var accountService:AccountService

	@Autowired
	private lateinit var accountRepository: AccountRepository


	@Test
	fun `test creating account`() {
		// Given
		val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)

		// When
		val result = accountService.createAccount(createAccountRequest)

		// Then
		val persistedAccount = accountRepository.findById(result.id).orElse(null)
		assertEquals("John", persistedAccount.firstName)
		assertEquals("Doe", persistedAccount.lastName)
		assertEquals("john.doe@example.com", persistedAccount.email)
		assertEquals(AccountTypeEnum.JOBSEEKER, persistedAccount.type)
	}
}
