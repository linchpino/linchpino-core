package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.RoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
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
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var interviewTypeRepository: InterviewTypeRepository

    @Mock
    private lateinit var roleRepository: RoleRepository

    @InjectMocks
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var emailService: EmailService

    @Test
    fun `test creating account`() {
        // Given
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 2)
        val account = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            password = "password123"
        }
        val jobSeekerRole = Role().apply { title = AccountTypeEnum.JOB_SEEKER }
        account.addRole(jobSeekerRole)

        val createAccountResult = CreateAccountResult(
            1,
            "John",
            "Doe",
            "john.doe@example.com",
            listOf(AccountTypeEnum.JOB_SEEKER)
        )
        val captor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)

        doAnswer {
            val a: Account = captor.value
            a.id = 1
            a
        }.`when`(repository).save(captor.capture())
        `when`(passwordEncoder.encode(createAccountRequest.password)).thenReturn("encodedPassword")
        `when`(roleRepository.findAll()).thenReturn(listOf(jobSeekerRole))

        // When
        val result = accountService.createAccount(createAccountRequest)

        // Then
        assertEquals(createAccountResult, result)
        val savedAccount = captor.value
        assertEquals("John", savedAccount.firstName)
        assertEquals("Doe", savedAccount.lastName)
        assertEquals("john.doe@example.com", savedAccount.email)
        assertEquals("encodedPassword", savedAccount.password)
        assertThat(savedAccount.roles()).containsExactly(jobSeekerRole)
        assertEquals(AccountStatusEnum.ACTIVATED, savedAccount.status)
        assertThat(savedAccount.roles()).containsExactly(jobSeekerRole)
    }

    @Test
    fun `test find mentors with closest time slots calls repository with correct arguments`() {
        // Given
        val date = ZonedDateTime.parse("2024-03-27T00:00:00+03:00")
        val expectedFrom = ZonedDateTime.parse("2024-03-26T21:00:00+00:00")
        val expectedTo = ZonedDateTime.parse("2024-03-27T21:00:00+00:00")
        val interviewTypeId = 5L
        val fromCaptor: ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        val toCaptor: ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        val idCaptor: ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        val accountTypeCaptor: ArgumentCaptor<AccountTypeEnum> = ArgumentCaptor.forClass(AccountTypeEnum::class.java)
        val timeSlotStatusCaptor: ArgumentCaptor<MentorTimeSlotEnum> =
            ArgumentCaptor.forClass(MentorTimeSlotEnum::class.java)
        // When
        accountService.findMentorsWithClosestTimeSlotsBy(date, interviewTypeId)

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
    fun `test activate job seeker account`() {
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

        account.addRole(Role().apply { title = AccountTypeEnum.JOB_SEEKER })

        `when`(repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)).thenReturn(account)
        `when`(passwordEncoder.encode(request.password)).thenReturn("encodePassword")
        val result = accountService.activeJobSeekerAccount(request)

        verify(repository, times(1)).save(account)

        assertThat(result.firstName).isEqualTo(result.firstName)
        assertThat(result.lastName).isEqualTo(result.lastName)
        assertThat(result.status).isEqualTo(AccountStatusEnum.ACTIVATED)
    }

    @Test
    fun `test activate job seeker account throws account not found exception when externalId is not valid`() {
        val request = ActivateJobSeekerAccountRequest(
            "externalId",
            "Jane",
            "Smith",
            "secret"
        )

        `when`(repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)).thenReturn(null)

        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            accountService.activeJobSeekerAccount(request)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `test activate job seeker account throws account already activated exception when account is activated`() {
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

        `when`(repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)).thenReturn(account)

        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            accountService.activeJobSeekerAccount(request)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.ACCOUNT_IS_ACTIVATED)
    }

    @Test
    fun `test register mentor`() {
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "password",
            interviewTypeIDs = listOf(1L, 2L),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "http://linkedin.com/johndoe"
        )

        val i1 = InterviewType().apply {
            id = 1
            name = "i1"
        }
        val i2 = InterviewType().apply {
            id = 2
            name = "i2"
        }
        val mentorRole = Role().apply {
            id = AccountTypeEnum.MENTOR.value
            title = AccountTypeEnum.MENTOR
        }
        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)
        `when`(interviewTypeRepository.findAllByIdIn(request.interviewTypeIDs)).thenReturn(listOf(i1, i2))
        `when`(passwordEncoder.encode(request.password)).thenReturn("encoded password")
        `when`(roleRepository.findAll()).thenReturn(listOf(mentorRole))

        val result = accountService.registerMentor(request)

        verify(repository, times(1)).save(accountCaptor.captureNonNullable())
        accountCaptor.value.firstName?.let { firstName ->
            accountCaptor.value.lastName?.let { lastName ->
                verify(emailService, times(1)).sendingWelcomeEmailToMentor(
                    firstName,
                    lastName,
                    accountCaptor.value.email
                )
            }
        }
        assertThat(result.firstName).isEqualTo(request.firstName)
        assertThat(result.lastName).isEqualTo(request.lastName)
        assertThat(result.email).isEqualTo(request.email)
        assertThat(result.detailsOfExpertise).isEqualTo(request.detailsOfExpertise)
        assertThat(result.linkedInUrl).isEqualTo(request.linkedInUrl)
        assertThat(result.linkedInUrl).isEqualTo(request.linkedInUrl)
        assertThat(result.interviewTypeIDs).isEqualTo(request.interviewTypeIDs)
        val savedAccount = accountCaptor.value
        assertThat(savedAccount.status).isEqualTo(AccountStatusEnum.ACTIVATED)
    }
}
