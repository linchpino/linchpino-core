package com.linchpino.core.controller

import com.linchpino.core.dto.AccountSummary
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddProfileImageResponse
import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.RegisterMentorResult
import com.linchpino.core.service.AccountService
import com.linchpino.core.service.StorageService
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
class AccountController(private val accountService: AccountService, private val timeSlotService: TimeSlotService) {

    @Autowired
    lateinit var storageService: StorageService

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


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/image")
    fun uploadProfileImage(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): AddProfileImageResponse {
        val result = accountService.uploadProfileImage(id, file)
        return result
    }
}
