package com.linchpino.core.controller

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.AccountSummary
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddProfileImageResponse
import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestSchedule
import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.RegisterMentorResult
import com.linchpino.core.dto.ResetPasswordRequest
import com.linchpino.core.dto.ScheduleRequest
import com.linchpino.core.dto.ScheduleResponse
import com.linchpino.core.dto.ScheduleUpdateRequest
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.dto.TimeSlot
import com.linchpino.core.dto.UpdateProfileRequest
import com.linchpino.core.dto.ValidWindow
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Schedule
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.enums.RecurrenceType
import com.linchpino.core.security.WithMockJwt
import com.linchpino.core.service.AccountService
import com.linchpino.core.service.ScheduleService
import com.linchpino.core.service.TimeSlotService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.time.DayOfWeek
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AccountControllerTest {

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var timeSlotService: TimeSlotService

    @InjectMocks
    private lateinit var accountController: AccountController

    @Mock
    private lateinit var scheduleService: ScheduleService

    @Test
    fun `test create account`() {
        // Given
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)
        val expectedResponse = CreateAccountResult(
            1,
            "John",
            "Doe",
            "john.doe@example.com",
            listOf(AccountTypeEnum.JOB_SEEKER)
        )

        `when`(accountService.createAccount(createAccountRequest)).thenReturn(expectedResponse)

        // When
        val result = accountController.createAccount(createAccountRequest)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }

    @Test
    fun `test search mentors by interviewTypeId and date`() {
        // Given
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

        val expectedResponse = listOf(
            MentorWithClosestSchedule(
                account1.id,
                account1.firstName,
                account1.lastName,
                ValidWindow(
                    ZonedDateTime.parse("2024-09-09T12:30:00+03:00"),
                    ZonedDateTime.parse("2024-09-09T12:30:00+03:00").plusMinutes(60)
                ),
                account1.email,
                account1.avatar
            ),
            MentorWithClosestSchedule(
                account2.id,
                account2.firstName,
                account2.lastName,
                ValidWindow(
                    ZonedDateTime.parse("2024-09-09T12:30:00+03:00"),
                    ZonedDateTime.parse("2024-09-09T12:30:00+03:00").plusMinutes(60)
                ),
                account2.email,
                account2.avatar
            ),
        )
        val idCaptor: ArgumentCaptor<Long> = ArgumentCaptor.forClass(Long::class.java)
        val dateCaptor: ArgumentCaptor<ZonedDateTime> = ArgumentCaptor.forClass(ZonedDateTime::class.java)
        `when`(
            accountService.findMentorsWithClosestScheduleBy(
                dateCaptor.captureNonNullable(),
                idCaptor.capture()
            )
        ).thenReturn(expectedResponse)

        // When
        val result = accountController.findMentorsByInterviewTypeAndDate(interviewTypeId, selectedDay)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(expectedResponse)
        assertThat(dateCaptor.value).isEqualTo(selectedDay)
        assertThat(idCaptor.value).isEqualTo(interviewTypeId)
        verify(accountService, times(1)).findMentorsWithClosestScheduleBy(selectedDay, interviewTypeId)
    }

    @Test
    fun `test activate job seeker account`() {
        // Given
        val request = ActivateJobSeekerAccountRequest(
            "externalId",
            "John",
            "Doe",
            "secret"
        )

        val expectedResponse = AccountSummary(
            1,
            "John",
            "Doe",
            "john.doe@example.com",
            listOf(AccountTypeEnum.JOB_SEEKER),
            AccountStatusEnum.ACTIVATED,
            "externalId"
        )


        val requestCaptor: ArgumentCaptor<ActivateJobSeekerAccountRequest> =
            ArgumentCaptor.forClass(ActivateJobSeekerAccountRequest::class.java)
        `when`(accountService.activeJobSeekerAccount(requestCaptor.captureNonNullable())).thenReturn(expectedResponse)

        // When
        val result = accountController.activateJobSeekerAccount(request)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(expectedResponse)
        assertThat(requestCaptor.value).isEqualTo(request)
        verify(accountService, times(1)).activeJobSeekerAccount(request)
    }

    @Test
    fun `test register new mentor`() {
        // Given
        val request = RegisterMentorRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            password = "password",
            interviewTypeIDs = listOf(1L, 2L),
            detailsOfExpertise = "Some expertise",
            linkedInUrl = "http://linkedin.com/johndoe",
            paymentMethodRequest = PaymentMethodRequest(PaymentMethodType.FREE),
            iban = "GB82 WEST 1234 5698 7654 32"
        )

        val expectedResponse = RegisterMentorResult(
            id = 1L,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            interviewTypeIDs = request.interviewTypeIDs,
            detailsOfExpertise = request.detailsOfExpertise,
            linkedInUrl = request.linkedInUrl,
            iban = request.iban?.replace(" ", "")
        )


        val requestCaptor: ArgumentCaptor<RegisterMentorRequest> =
            ArgumentCaptor.forClass(RegisterMentorRequest::class.java)
        `when`(accountService.registerMentor(requestCaptor.captureNonNullable())).thenReturn(expectedResponse)

        // When
        val result = accountController.registerMentor(request)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
        assertThat(requestCaptor.value).isEqualTo(request)
        verify(accountService, times(1)).registerMentor(request)
    }


    @Test
    fun `test add timeslots for mentor`() {
        // Given
        val timeSlots = listOf(
            TimeSlot(
                ZonedDateTime.parse("2024-05-09T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-09T13:30:45+03:00")
            ),
            TimeSlot(
                ZonedDateTime.parse("2024-05-10T12:30:45+03:00"),
                ZonedDateTime.parse("2024-05-10T13:30:45+03:00")
            ),
        )
        val request = AddTimeSlotsRequest(1000, timeSlots)
        val captor: ArgumentCaptor<AddTimeSlotsRequest> = ArgumentCaptor.forClass(AddTimeSlotsRequest::class.java)

        // When
        accountController.addTimeSlotsForMentor(request)

        // Then
        verify(timeSlotService, times(1)).addTimeSlots(captor.captureNonNullable())

        assertThat(captor.value).isEqualTo(request)
    }

    @Test
    fun `test search accounts by name and role`() {
        // Given
        val expectedResult = listOf(
            SearchAccountResult(
                100,
                "John",
                "Doe",
                listOf("MENTOR"),
                "johndoe@example.com",
                "avatar.png",
                AccountStatusEnum.ACTIVATED,
            )
        )

        val page = Pageable.ofSize(10)
        `when`(accountService.searchAccountByNameOrRole("john", 3, page)).thenReturn(
            PageImpl(expectedResult)
        )

        // When
        val result = accountController.searchAccounts("john", 3, page)

        // Then
        assertThat(result).isEqualTo(PageImpl(expectedResult))
        verify(accountService, times(1)).searchAccountByNameOrRole("john", 3, page)
    }


    @Test
    fun `test upload image calls account service`() {
        // Given
        val file = MockMultipartFile("file", "fileName", "image/jpeg", "test image content".toByteArray())
        val authentication = WithMockJwt.mockAuthentication()
        `when`(accountService.uploadProfileImage(file, authentication)).thenReturn(AddProfileImageResponse("fileName"))
        // When
        val result = accountController.uploadProfileImage(file, authentication)

        // Then
        verify(accountService).uploadProfileImage(file, authentication)
        assertThat(result.imageUrl).isEqualTo("fileName")
    }

    @Test
    fun `test profile returns successfully`() {
        val summary = AccountSummary(1, "john", "doe", "john@example.com", listOf(), AccountStatusEnum.ACTIVATED, null)
        val authentication = WithMockJwt.mockAuthentication()

        `when`(accountService.profile(authentication)).thenReturn(summary)
        val result = accountController.profile(authentication)
        verify(accountService, times(1)).profile(authentication)
        assertThat(result).isEqualTo(summary)
    }

    @Test
    fun `test adding schedule for mentors`() {
        // Given
        val scheduleRequest = ScheduleRequest(
            ZonedDateTime.parse("2024-08-28T12:30:45+03:00"),
            60,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )
        val authentication = WithMockJwt.mockAuthentication()

        val response = ScheduleResponse(
            1,
            ZonedDateTime.parse("2024-08-28T12:30:45+03:00"),
            60,
            1,
            RecurrenceType.WEEKLY,
            3,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )

        // When
        `when`(scheduleService.addSchedule(scheduleRequest, authentication)).thenReturn(response)

        val result = accountController.addScheduleForMentor(scheduleRequest, authentication)

        // Then
        verify(scheduleService, times(1)).addSchedule(scheduleRequest, authentication)
        assertThat(result).isEqualTo(response)
    }

    @Test
    fun `test reset password calls service with provided arguments`() {
        val authentication = WithMockJwt.mockAuthentication("john.doe@example.com")
        val request = ResetPasswordRequest("old", "new")
        accountController.changePassword(authentication, request)

        verify(accountService, times(1)).changePassword(authentication, request)
    }


    @Test
    fun `test update profile calls service with provided arguments`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication("john.doe@example.com")
        val request = UpdateProfileRequest(
            "firstName",
            "lastName",
            "detailsOfExpertise",
            "iban",
            "linkedInUrl",
            PaymentMethodRequest(PaymentMethodType.FREE)
        )

        // When
        accountController.updateAccount(authentication, request)

        // Then
        verify(accountService, times(1)).updateProfile(authentication, request)
    }

    @Test
    fun `test update schedule calls service with provided arguments`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication("john.doe@example.com")
        val request = ScheduleUpdateRequest(
            startTime = ZonedDateTime.parse("2024-09-28T12:30:45+03:00"),
            endTime = null,
            duration = null,
            recurrenceType = RecurrenceType.MONTHLY,
            interval = null,
            monthDays = listOf(1, 15)
        )
        val response = ScheduleResponse(
            1,
            ZonedDateTime.parse("2024-09-28T12:30:45+03:00"),
            40,
            1,
            RecurrenceType.MONTHLY,
            1,
            ZonedDateTime.parse("2024-12-30T13:30:45+03:00"),
        )

        `when`(scheduleService.updateSchedule(authentication, request)).thenReturn(response)

        // When
        val result = accountController.updateSchedule(authentication, request)

        // Then
        assertThat(result).isEqualTo(response)
        verify(scheduleService, times(1)).updateSchedule(authentication, request)
    }

    @Test
    fun `test delete schedule calls service with provided arguments`() {
        // Given
        val authentication = WithMockJwt.mockAuthentication("john.doe@example.com")

        // When
        accountController.deleteSchedule(authentication)

        // Then
        verify(scheduleService, times(1)).deleteSchedule(authentication)
    }
}
