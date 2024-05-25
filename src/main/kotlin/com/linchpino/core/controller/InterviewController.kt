package com.linchpino.core.controller

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.service.InterviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/interviews")
class InterviewController(private val service: InterviewService) {

    @Operation(summary = "Create a new Interview")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        value = [ApiResponse(responseCode = "201", description = "Interview added successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request body")]
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun newInterview(@Valid @RequestBody request: CreateInterviewRequest): ResponseEntity<CreateInterviewResult> {
        val result = service.createInterview(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @Operation(summary = "Return page of upcoming interviews for authenticated mentor")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Upcoming interviews fetched successfully"),
            ApiResponse(responseCode = "401", description = "User is not authenticated"),
            ApiResponse(responseCode = "403", description = "Authenticated user is not MENTOR")
        ]
    )
    @GetMapping("/mentors/upcoming")
    fun upcomingInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        val result = service.upcomingInterviews(authentication, page)
        return result
    }

    @Operation(summary = "Return page of past interviews for authenticated mentor")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Past interviews fetched successfully"),
            ApiResponse(responseCode = "401", description = "User is not authenticated"),
            ApiResponse(responseCode = "403", description = "Authenticated user is not MENTOR")
        ]
    )
    @GetMapping("/mentors/past")
    fun pastInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        val result = service.pastInterviews(authentication, page)
        return result
    }
}
