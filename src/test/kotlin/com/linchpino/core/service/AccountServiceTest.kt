package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AccountServiceTest {

    @Mock
    private lateinit var repository: AccountRepository

    @Mock
    private lateinit var mapper: AccountMapper

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var accountService: AccountService

    @Test
    fun `test creating account`() {
        // Given
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)
        val account = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
            type = AccountTypeEnum.JOB_SEEKER
        }
        val createAccountResult = CreateAccountResult(
            1,
            "John",
            "Doe",
            "john.doe@example.com",
            AccountTypeEnum.JOB_SEEKER
        )

        val captor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)

        // Mock behavior
        `when`(mapper.accountDtoToAccount(createAccountRequest)).thenReturn(account)
        `when`(passwordEncoder.encode(createAccountRequest.password)).thenReturn("encodedPassword")
        `when`(repository.save(account)).thenReturn(account)
        `when`(mapper.entityToResultDto(account)).thenReturn(createAccountResult)

        // When
        val result = accountService.createAccount(createAccountRequest)

        // Then
        assertEquals(createAccountResult, result)
        verify(repository, times(1)).save(captor.capture())
        val savedAccount = captor.value
        assertEquals("John", savedAccount.firstName)
        assertEquals("Doe", savedAccount.lastName)
        assertEquals("john.doe@example.com", savedAccount.email)
        assertEquals("encodedPassword", savedAccount.password)
        assertEquals(AccountTypeEnum.JOB_SEEKER, savedAccount.type)
        assertEquals(AccountStatus.DEACTIVATED, savedAccount.status)
    }

    @Test
    fun `test find mentors with closest time slots calls repository with correct arguments`(){
        // Given
        val date = LocalDate.parse("2024-03-27")
        val interviewTypeId = 5L
        val dateCaptor:ArgumentCaptor<LocalDate> = ArgumentCaptor.forClass(LocalDate::class.java)
        val idCaptor:ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        // When
        accountService.findMentorsWithClosestTimeSlotsBy(date,interviewTypeId)

        // Then
        verify(repository, times(1)).closestMentorTimeSlots(dateCaptor.captureNonNullable(),idCaptor.capture())
        assertThat(dateCaptor.value).isEqualTo(date)
        assertThat(idCaptor.value).isEqualTo(5L)
    }
}
