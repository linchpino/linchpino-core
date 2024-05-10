package com.linchpino.core.controller

import com.linchpino.core.dto.*
import com.linchpino.core.service.AccountService
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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@RestController
@RequestMapping("api/accounts")
class AccountController(private val accountService: AccountService,private val timeSlotService: TimeSlotService) {

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
    @PostMapping("/mentors",consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
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
    @PostMapping("/mentors/timeslots",consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addTimeSlotsForMentor(@Valid @RequestBody request: AddTimeSlotsRequest) {
        timeSlotService.addTimeSlots(request)
    }
}
