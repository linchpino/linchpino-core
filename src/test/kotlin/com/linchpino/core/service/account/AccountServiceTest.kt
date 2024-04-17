package com.linchpino.core.service.account

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.service.AccountService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
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
import java.time.ZonedDateTime

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
        assertEquals(AccountStatusEnum.DEACTIVATED, savedAccount.status)
    }

    @Test
    fun `test find mentors with closest time slots calls repository with correct arguments`(){
        // Given
        val date = ZonedDateTime.parse("2024-03-27T00:00:00+03:00")
        val expectedFrom = ZonedDateTime.parse("2024-03-26T21:00:00+00:00")
        val expectedTo = ZonedDateTime.parse("2024-03-27T21:00:00+00:00")
        val interviewTypeId = 5L
        val fromCaptor:ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        val toCaptor:ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        val idCaptor:ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        val accountTypeCaptor:ArgumentCaptor<AccountTypeEnum> = ArgumentCaptor.forClass(AccountTypeEnum::class.java)
        val timeSlotStatusCaptor:ArgumentCaptor<MentorTimeSlotEnum> = ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)
        // When
        accountService.findMentorsWithClosestTimeSlotsBy(date,interviewTypeId)

        // Then
        verify(repository, times(1)).closestMentorTimeSlots(
            fromCaptor.captureNonNullable(),
            toCaptor.captureNonNullable(),
            idCaptor.capture(),
            accountTypeCaptor.captureNonNullable(),
            timeSlotStatusCaptor.captureNonNullable()
        )
        assertThat(fromCaptor.value).isEqualTo(expectedFrom)
        assertThat(toCaptor.value).isEqualTo(expectedTo)
        assertThat(idCaptor.value).isEqualTo(5L)
        assertThat(accountTypeCaptor.value).isEqualTo(AccountTypeEnum.MENTOR)
        assertThat(timeSlotStatusCaptor.value).isEqualTo(MentorTimeSlotEnum.AVAILABLE)
    }

    @Test
    fun `test activate job seeker account`(){
        val request = ActivateJobSeekerAccountRequest(
            "externalId",
            "Jane",
            "Smith",
            "secret"
        )
        val account = Account().apply {
            id = 5
            firstName = "john"
            lastName = "doe"
            email = "johndoe@example.com"
            status = AccountStatusEnum.DEACTIVATED
        }

        `when`(repository.findByExternalId(request.externalId,AccountTypeEnum.JOB_SEEKER)).thenReturn(account)
        val result = accountService.activeJobSeekerAccount(request)

        verify(repository, times(1)).save(account)

        assertThat(result.firstName).isEqualTo(result.firstName)
        assertThat(result.lastName).isEqualTo(result.lastName)
        assertThat(result.status).isEqualTo(AccountStatusEnum.ACTIVATED)
    }

    @Test
    fun `test activate job seeker account throws account not found exception when externalId is not valid`(){
        val request = ActivateJobSeekerAccountRequest(
            "externalId",
            "Jane",
            "Smith",
            "secret"
        )

        `when`(repository.findByExternalId(request.externalId,AccountTypeEnum.JOB_SEEKER)).thenReturn(null)

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            accountService.activeJobSeekerAccount(request)
        }

        assertThat(exception.message).isEqualTo("account not found")
    }

    @Test
    fun `test activate job seeker account throws account already activated exception when account is activated`(){
        val request = ActivateJobSeekerAccountRequest(
            "externalId",
            "Jane",
            "Smith",
            "secret"
        )
        val account = Account().apply {
            id = 5
            firstName = "john"
            lastName = "doe"
            email = "johndoe@example.com"
            status = AccountStatusEnum.ACTIVATED
        }

        `when`(repository.findByExternalId(request.externalId,AccountTypeEnum.JOB_SEEKER)).thenReturn(account)

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            accountService.activeJobSeekerAccount(request)
        }

        assertThat(exception.message).isEqualTo("account is already activated")
    }
}
