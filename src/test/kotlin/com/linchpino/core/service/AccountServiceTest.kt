package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.LinkedInUserInfoResponse
import com.linchpino.core.dto.MentorWithClosestSchedule
import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.ResetPasswordRequest
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.dto.ValidWindow
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.Role
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.RoleRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.security.email
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.web.multipart.MultipartFile


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

    @Mock
    private lateinit var storageService: StorageService

    @Mock
    private lateinit var linkedInService: LinkedInService

    @Mock
    private lateinit var paymentService: PaymentService

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

        verify(paymentService, times(1)).savePaymentMethod(PaymentMethodRequest(PaymentMethodType.FREE), savedAccount)
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

        val exception = assertThrows(LinchpinException::class.java) {
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

        val exception = assertThrows(LinchpinException::class.java) {
            accountService.activeJobSeekerAccount(request)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.ACCOUNT_IS_ACTIVATED)
    }

    @Test
    fun `test register mentor`() {
        val paymentMethodRequest = PaymentMethodRequest(
            type = PaymentMethodType.FIX_PRICE,
            fixRate = 10.0
        )
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "password",
            interviewTypeIDs = listOf(1L, 2L),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "http://linkedin.com/johndoe",
            paymentMethodRequest = paymentMethodRequest,
            iban = "GB82 WEST 1234 5698 7654 32"
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
        assertThat(savedAccount.iban).isEqualTo(request.iban?.trim()?.replace(" ", ""))
        verify(paymentService, times(1)).savePaymentMethod(paymentMethodRequest, savedAccount)
    }

    @Test
    fun `test search accounts by name or role`() {
        // Given
        val account = Account().apply {
            id = 1
            firstName = "John"
            lastName = "Doe"
            addRole(Role().apply {
                id = 2
                title = AccountTypeEnum.MENTOR
            })
            email = "johndoe@example.com"
            avatar = "avatar.png"
        }
        val page = Pageable.ofSize(10)
        `when`(
            repository.searchByNameOrRole(
                "john",
                AccountTypeEnum.MENTOR,
                page
            )
        ).thenReturn(PageImpl(listOf(account)))

        // When
        val result = accountService.searchAccountByNameOrRole("john", 3, page)

        // Then
        verify(repository, times(1)).searchByNameOrRole("john", AccountTypeEnum.MENTOR, page)
        assertThat(result).isEqualTo(
            PageImpl(
                listOf(
                    SearchAccountResult(
                        account.firstName,
                        account.lastName,
                        account.roles().map { it.title.name },
                        account.email,
                        account.avatar
                    )
                )
            )
        )

    }


    @Test
    fun `test uploadProfileImage success`() {
        // Give
        val fileName = "profile.jpg"
        val account = Account().apply {
            id = 1L
        }
        val file: MultipartFile = mock(MultipartFile::class.java)

        val authentication = WithMockJwt.mockAuthentication()
        `when`(repository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)
        `when`(storageService.uploadProfileImage(account, file)).thenReturn(fileName)

        // When
        val response = accountService.uploadProfileImage(file, authentication)

        // Then
        assertEquals(fileName, response.imageUrl)
        assertEquals(fileName, account.avatar)
        verify(repository).findByEmailIgnoreCase(authentication.email())
        verify(storageService).uploadProfileImage(account, file)
    }


    @Test
    fun `test profile returns account summary when token is jwt`() {
        val account = Account().apply {
            id = 1
            email = "johndoe@example.com"
            firstName = "John"
            lastName = "Doe"
        }

        val mockedAuth = WithMockJwt.mockAuthentication()
        `when`(repository.findByEmailIgnoreCase(mockedAuth.email())).thenReturn(account)

        val result = accountService.profile(mockedAuth)
        assertThat(result.email).isEqualTo(account.email)
        assertThat(result.firstName).isEqualTo(account.firstName)
        assertThat(result.lastName).isEqualTo(account.lastName)
    }

    @Test
    fun `test profile returns account summary when token is bearer token`() {
        val account = Account().apply {
            id = 1
            email = "johndoe@example.com"
            firstName = "John"
            lastName = "Doe"
        }

        val mockedAuth = BearerTokenAuthentication(
            OAuth2IntrospectionAuthenticatedPrincipal(
                account.email,
                mutableMapOf<String, Any?>("key" to "value"),
                mutableListOf<GrantedAuthority?>()
            ),
            OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, null),
            mutableListOf()
        )

        `when`(repository.findByEmailIgnoreCase(mockedAuth.email())).thenReturn(account)

        val result = accountService.profile(mockedAuth)
        assertThat(result.email).isEqualTo(account.email)
        assertThat(result.firstName).isEqualTo(account.firstName)
        assertThat(result.lastName).isEqualTo(account.lastName)
    }

    @Test
    fun `test profile saves account and returns summary when token is bearer token and user does not exist`() {
        // Given
        val account = LinkedInUserInfoResponse("john@example.com", "john", "doe")
        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)
        val mockedAuth = BearerTokenAuthentication(
            OAuth2IntrospectionAuthenticatedPrincipal(
                account.email,
                mutableMapOf<String, Any?>("key" to "value"),
                mutableListOf<GrantedAuthority?>()
            ),
            OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, null),
            mutableListOf()
        )
        `when`(linkedInService.userInfo("token")).thenReturn(account)
        `when`(repository.findByEmailIgnoreCase(mockedAuth.email())).thenReturn(null)
        `when`(roleRepository.findAll()).thenReturn(
            listOf(
                Role().apply { title = AccountTypeEnum.MENTOR },
                Role().apply { title = AccountTypeEnum.JOB_SEEKER },
                Role().apply { title = AccountTypeEnum.ADMIN })
        )
        // When
        val result = accountService.profile(mockedAuth)

        // Then
        assertThat(result.email).isEqualTo(account.email)
        assertThat(result.firstName).isEqualTo(account.firstName)
        assertThat(result.lastName).isEqualTo(account.lastName)

        verify(repository, times(1)).save(accountCaptor.captureNonNullable())
        val savedAccount = accountCaptor.value
        assertThat(savedAccount.email).isEqualTo(account.email)
        assertThat(savedAccount.firstName).isEqualTo(account.firstName)
        assertThat(savedAccount.lastName).isEqualTo(account.lastName)
        assertThat(savedAccount.roles().map { it.title }).isEqualTo(listOf(AccountTypeEnum.JOB_SEEKER))
    }

    @Test
    fun `test profile throws exception if account does not exist and authentication is jwt`() {

        val mockedAuth = WithMockJwt.mockAuthentication()

        `when`(repository.findByEmailIgnoreCase(mockedAuth.email())).thenReturn(null)

        val ex = assertThrows(LinchpinException::class.java) {
            accountService.profile(mockedAuth)
        }
        assertThat(ex.errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `mentors with closest schedule must return list of mentors`() {
        val start = ZonedDateTime.parse("2024-08-28T12:30:00+03:00")
        val end = ZonedDateTime.parse("2024-12-30T13:30:00+03:00")
        val schedule1 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }

        val schedule2 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.WEEKLY
            weekDays = mutableListOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        }

        val schedule3 = Schedule().apply {
            startTime = start
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.MONTHLY
            monthDays = mutableListOf(15, 25)
        }

        val schedule4 = Schedule().apply {
            startTime = start.plusDays(7)
            endTime = end
            interval = 2
            duration = 60
            recurrenceType = RecurrenceType.DAILY
        }
        val selectedDay = ZonedDateTime.parse("2024-09-09T10:00:00+03:00")
        val interviewTypeId = 1L
        val account1 = Account().apply {
            id = 1
            firstName = "john"
            lastName = "doe"
            schedule = schedule1
            email = "account1@example.com"
            avatar = "avatar1.png"
        }

        val account2 = Account().apply {
            id = 2
            firstName = "josh"
            lastName = "long"
            schedule = schedule2
            email = "account2@example.com"
            avatar = "avatar2.png"
        }

        val account3 = Account().apply {
            id = 3
            firstName = "jane"
            lastName = "smith"
            schedule = schedule3
            email = "account3@example.com"
            avatar = "avatar3.png"
        }

        val account4 = Account().apply {
            id = 4
            firstName = "kent"
            lastName = "beck"
            schedule = schedule4
            email = "account4@example.com"
            avatar = "avatar4.png"
        }

        val expected1 = MentorWithClosestSchedule(
            account1.id,
            account1.firstName,
            account1.lastName,
            ValidWindow(
                ZonedDateTime.parse("2024-09-09T12:30:00+03:00"),
                ZonedDateTime.parse("2024-09-09T12:30:00+03:00").plusMinutes(60)
            ),
            account1.email,
            account1.avatar
        )
        val expected2 = MentorWithClosestSchedule(
            account2.id,
            account2.firstName,
            account2.lastName,
            ValidWindow(
                ZonedDateTime.parse("2024-09-09T12:30:00+03:00"),
                ZonedDateTime.parse("2024-09-09T12:30:00+03:00").plusMinutes(60)
            ),
            account2.email,
            account2.avatar
        )

        `when`(
            repository.closestMentorSchedule(
                selectedDay.withZoneSameInstant(ZoneOffset.UTC),
                interviewTypeId
            )
        ).thenReturn(listOf(account1, account2, account3, account4))

        val result = accountService.findMentorsWithClosestScheduleBy(selectedDay, interviewTypeId)

        assertThat(result).containsExactly(expected1, expected2)
    }


    @Test
    fun `reset password update account password`() {
        val authentication = WithMockJwt.mockAuthentication(email = "john.doe@gmail.com")
        val request = ResetPasswordRequest("secret", "secret1")

        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = request.currentPassword
        }
        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)


        `when`(passwordEncoder.encode(request.newPassword)).thenReturn("newPasswordEncoded")
        `when`(passwordEncoder.matches(request.currentPassword, account.password)).thenReturn(true)
        `when`(repository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)

        accountService.changePassword(authentication, request)

        verify(repository, times(1)).save(accountCaptor.capture())
        val password = accountCaptor.value.password
        assertThat(password).isEqualTo("newPasswordEncoded")
    }


    @Test
    fun `reset password throws invalid password error if current password does not match requested current password`() {
        val authentication = WithMockJwt.mockAuthentication(email = "john.doe@gmail.com")
        val request = ResetPasswordRequest("secret", "secret1")

        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = request.currentPassword
        }


        `when`(passwordEncoder.matches(request.currentPassword, account.password)).thenReturn(false)
        `when`(repository.findByEmailIgnoreCase(authentication.email())).thenReturn(account)

        val ex = assertThrows(LinchpinException::class.java) {
            accountService.changePassword(authentication, request)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INVALID_PASSWORD)
    }

    @Test
    fun `test reset password by admin`() {
        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
        }
        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)

        val request = ResetAccountPasswordRequest(1, "newPassword")

        `when`(passwordEncoder.encode(request.newPassword)).thenReturn("encryptedNewPassword")
        `when`(repository.findReferenceById(1)).thenReturn(account)
        accountService.resetAccountPasswordByAdmin(request)

        verify(repository).save(accountCaptor.capture())
        val result = accountCaptor.value
        assertThat(result.password).isEqualTo("encryptedNewPassword")
    }


    @Test
    fun `test admin can update roles and status of any account`() {
        // Given
        val account = Account().apply {
            id = 1
            email = "john.doe@gmail.com"
            firstName = "john"
            lastName = "doe"
            password = "secret"
            status = AccountStatusEnum.ACTIVATED
        }
        val accountCaptor: ArgumentCaptor<Account> = ArgumentCaptor.forClass(Account::class.java)
        val roles = listOf(
            Role().apply { title = AccountTypeEnum.ADMIN },
            Role().apply { title = AccountTypeEnum.GUEST },
            Role().apply { title = AccountTypeEnum.MENTOR },
            Role().apply { title = AccountTypeEnum.JOB_SEEKER }
        )
        account.addRole(roles[2])

        val request = UpdateAccountRequestByAdmin(1, listOf(1, 2), 2)

        `when`(roleRepository.findAll()).thenReturn(roles)
        `when`(repository.findReferenceById(1)).thenReturn(account)

        // When
        accountService.updateAccountByAdmin(request)

        // Then
        verify(repository, times(1)).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assertThat(savedAccount.roles().map { it.title }).containsExactlyInAnyOrderElementsOf(listOf(AccountTypeEnum.GUEST, AccountTypeEnum.JOB_SEEKER))
        assertThat(savedAccount.status).isEqualTo(AccountStatusEnum.DEACTIVATED)
    }


}
