package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeResponse
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.service.InterviewTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import java.lang.Exception
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/admin/interviewtypes")
class InterviewTypeAdminController(private val service: InterviewTypeService) {


    @Operation(summary = "Add a new interview type", description = "Creates a new interview type in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Interview type created",
                content = [Content(schema = Schema(implementation = InterviewTypeCreateRequest::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addInterviewType(@Valid @RequestBody request: InterviewTypeCreateRequest): InterviewTypeSearchResponse {
        return service.createInterviewType(request)
    }


    @Operation(summary = "Fetch interviewType by id", description = "Fetch an interview type in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Interview type fetched successfully",
                content = [Content(schema = Schema(implementation = InterviewTypeCreateRequest::class))]
            ),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @GetMapping("/{id}")
    fun getInterviewType(@PathVariable id: Long): InterviewTypeResponse {
        return service.getInterviewTypeById(id)
    }

    @Operation(summary = "Update interviewType", description = "Update an interview type in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Interview type updated successfully",
                content = [Content(schema = Schema(implementation = InterviewTypeUpdateRequest::class))]
            ),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateInterviewType(@PathVariable id: Long, @Valid @RequestBody request: InterviewTypeUpdateRequest) {
        service.updateInterviewType(id, request)
    }

    @Operation(summary = "Delete interviewType by id", description = "Deletes an interview type in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Interview type deleted successfully",
                content = [Content(schema = Schema(implementation = InterviewTypeCreateRequest::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteInterviewTypeById(@PathVariable id: Long) {
        try {
            service.deleteInterviewType(id)
        } catch (ex: DataIntegrityViolationException) {
            throw LinchpinException(
                "can not delete interview type with id: $id, it is used by other entities",
                ex,
                ErrorCode.INTEGRITY_VIOLATION,
                InterviewType::class.java.simpleName,
                "removed"
            )
        }
    }
}
