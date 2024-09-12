package com.linchpino.core.controller

import com.linchpino.core.dto.AccountSummary
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddProfileImageResponse
import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestSchedule
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.RegisterMentorResult
import com.linchpino.core.dto.ResetPasswordRequest
import com.linchpino.core.dto.ScheduleRequest
import com.linchpino.core.dto.ScheduleResponse
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.service.AccountService
import com.linchpino.core.service.ScheduleService
import com.linchpino.core.service.TimeSlotService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime

@RestController
@RequestMapping("api/accounts")
class AccountController(
    private val accountService: AccountService,
    private val timeSlotService: TimeSlotService,
    private val scheduleService: ScheduleService
) {


    @Operation(summary = "Create a new account")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Account created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request body")
        ]
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createAccount(@Valid @RequestBody createAccountRequest: CreateAccountRequest): ResponseEntity<CreateAccountResult> {
        val result = accountService.createAccount(createAccountRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

/*
    @Operation(summary = "Search mentors with available timeslots based on date and interviewTypeId")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Found mentors with available timeslots for provided interviewTypeId and data"
            ),
            ApiResponse(responseCode = "400", description = "Invalid query parameter")
        ]
    )
    @Parameters(
        value = [
            Parameter(
                name = "interviewTypeId",
                description = "ID of interview type",
                `in` = ParameterIn.QUERY,
                required = true
            ),
            Parameter(
                name = "date",
                description = "zoned date time in ISO-8601 format YYYY-MM-ddTHH:mm:ssXXX, example 2024-03-26T00:00:00+03:30",
                `in` = ParameterIn.QUERY,
                required = true
            )
        ]
    )
    @GetMapping("/mentors/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findMentorsByInterviewTypeAndDate(
        @RequestParam(value = "interviewTypeId", required = true) interviewTypeId: Long,
        @RequestParam(value = "date", required = true) date: ZonedDateTime
    ): ResponseEntity<List<MentorWithClosestTimeSlot>> {
        val result = accountService.findMentorsWithClosestTimeSlotsBy(date, interviewTypeId)
        return ResponseEntity.ok(result)
    }
*/


    @Operation(summary = "Search mentors with available timeslots based on date and interviewTypeId")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Found mentors with available timeslots for provided interviewTypeId and data"
            ),
            ApiResponse(responseCode = "400", description = "Invalid query parameter")
        ]
    )
    @Parameters(
        value = [
            Parameter(
                name = "interviewTypeId",
                description = "ID of interview type",
                `in` = ParameterIn.QUERY,
                required = true
            ),
            Parameter(
                name = "date",
                description = "zoned date time in ISO-8601 format YYYY-MM-ddTHH:mm:ssXXX, example 2024-03-26T00:00:00+03:30",
                `in` = ParameterIn.QUERY,
                required = true
            )
        ]
    )
    @GetMapping("/mentors/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findMentorsByInterviewTypeAndDate(
        @RequestParam(value = "interviewTypeId", required = true) interviewTypeId: Long,
        @RequestParam(value = "date", required = true) date: ZonedDateTime
    ): ResponseEntity<List<MentorWithClosestSchedule>> {
        val result = accountService.findMentorsWithClosestScheduleBy(date, interviewTypeId)
        return ResponseEntity.ok(result)
    }



    @Operation(summary = "Activate Job Seeker Account", description = "Activates a job seeker account")
    @ApiResponse(
        responseCode = "200", description = "Successfully activated job seeker account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountSummary::class))]
    )
    @PutMapping(
        "/jobseeker/activation",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun activateJobSeekerAccount(@Valid @RequestBody request: ActivateJobSeekerAccountRequest): ResponseEntity<AccountSummary> {
        val result = accountService.activeJobSeekerAccount(request)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Register a new mentor")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Mentor registered successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request body")
        ]
    )
    @PostMapping(
        "/mentors",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun registerMentor(@Valid @RequestBody request: RegisterMentorRequest): ResponseEntity<RegisterMentorResult> {
        val result = accountService.registerMentor(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @Operation(summary = "Add timeslots for mentor")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Added TimeSlots for mentor successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request body")
        ]
    )
    @PostMapping(
        "/mentors/timeslots",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun addTimeSlotsForMentor(@Valid @RequestBody request: AddTimeSlotsRequest) {
        timeSlotService.addTimeSlots(request)
    }

    @Operation(summary = "Add schedule for mentor")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Added schedule for mentor successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request body"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Not authorized")
        ]
    )
    @PostMapping("/mentors/schedule")
    fun addScheduleForMentor(@Valid @RequestBody request: ScheduleRequest, auth: Authentication): ScheduleResponse {
        return scheduleService.addSchedule(request, auth)
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Not authorized")
        ]
    )
    fun searchAccounts(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) role: Int?
    ): List<SearchAccountResult> {
        return accountService.searchAccountByNameOrRole(name, role)
    }

    @Bean
    fun adminAccountRunner(
        @Value("\${admin.username}") username: String,
        @Value("\${admin.password}") password: String
    ) = ApplicationRunner {
        val request = CreateAccountRequest(
            "admin",
            "admin",
            username,
            password,
            AccountTypeEnum.ADMIN.value
        )
        val admins = accountService.searchAccountByNameOrRole(null, AccountTypeEnum.ADMIN.value)
        if (admins.isEmpty()) {
            createAccount(request)
        }
    }


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/image")
    fun uploadProfileImage(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): AddProfileImageResponse {
        val result = accountService.uploadProfileImage(file, authentication)
        return result
    }

    @GetMapping("/profile")
    fun profile(authentication: Authentication): AccountSummary {
        return accountService.profile(authentication)
    }

    @PutMapping("/profile/change-password")
    fun changePassword(authentication: Authentication, @Valid @RequestBody resetPassword: ResetPasswordRequest){
        accountService.changePassword(authentication,resetPassword)
    }
}
