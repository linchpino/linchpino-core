package com.linchpino.core.controller

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.service.InterviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
}
