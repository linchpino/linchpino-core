package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.service.InterviewTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/admin/interviewtypes")
class InterviewTypeAdminController(private val service: InterviewTypeService) {


    @Operation(summary = "Add a new interview type", description = "Creates a new interview type in the system.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Interview type created", content = [Content(schema = Schema(implementation = InterviewTypeCreateRequest::class))]),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
    ])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addInterviewType(@Valid @RequestBody request: InterviewTypeCreateRequest):InterviewTypeSearchResponse{
        return service.createInterviewType(request)
    }
}
