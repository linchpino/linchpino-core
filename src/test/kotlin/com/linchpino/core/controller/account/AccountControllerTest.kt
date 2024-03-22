package com.linchpino.core.controller.account

import com.linchpino.core.controller.AccountController
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.service.AccountService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
class AccountControllerTest {

    @Mock
    private lateinit var accountService: AccountService

    @InjectMocks
    private lateinit var accountController: AccountController

    @Test
    fun `test create account`() {
        // Given
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)
        val expectedResponse = CreateAccountResult(
			1,
			"John",
			"Doe",
			"john.doe@example.com",
			AccountTypeEnum.JOB_SEEKER
		)

		`when`(accountService.createAccount(createAccountRequest)).thenReturn(expectedResponse)

        // When
        val result = accountController.createAccount(createAccountRequest)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }
}
