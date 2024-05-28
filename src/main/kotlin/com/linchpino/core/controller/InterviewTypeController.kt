package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.service.InterviewTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/interviewtypes")
class InterviewTypeController(private val service: InterviewTypeService) {


    @Operation(summary = "Search interview types by name")
    @Parameter(
        name = "name",
        description = "Name of the interview type to search",
        `in` = ParameterIn.PATH,
        required = false
    )
    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun interviewTypes(
        @RequestParam(required = false) name: String?,
        @PageableDefault(size = 10) pageable: Pageable
    ): Page<InterviewTypeSearchResponse> = service.searchByName(name, pageable)
}
