package com.linchpino.core.controller

import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.service.JobPositionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
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
@RequestMapping("api/admin/jobposition")
class JobPositionAdminController(private val jobPositionService: JobPositionService) {

    @Operation(summary = "Delete job position by id", description = "Deletes a job position in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "job position deleted successfully",
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteJobPosition(@PathVariable id: Long) {
        jobPositionService.deleteById(id)
    }

    @Operation(summary = "Update job position title", description = "Updates job position title")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "job position title updated successfully",
            ),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateJobPosition(@PathVariable id: Long, @Valid @RequestBody request: JobPositionCreateRequest){
        jobPositionService.update(id,request)
    }

    @Operation(summary = "Get job position by id", description = "Fetches a specific job position.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "job position fetched successfully",
                content = [Content(schema = Schema(implementation = JobPositionSearchResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @GetMapping("/{id}")
    fun getJobPosition(@PathVariable id: Long): JobPositionSearchResponse {
        return  jobPositionService.getById(id)
    }


    @Operation(summary = "Add a new job position", description = "Creates a new job position in the system.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "job position created",
                content = [Content(schema = Schema(implementation = JobPositionCreateRequest::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden if not ADMIN"),
        ]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun addJobPosition(@Valid @RequestBody request: JobPositionCreateRequest):JobPositionSearchResponse {
        return jobPositionService.createJobPosition(request)
    }
}
