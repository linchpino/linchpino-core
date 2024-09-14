package com.linchpino.core.dto

import jakarta.validation.constraints.NotBlank

data class InterviewTypeSearchResponse(val id: Long?, val title: String)
data class InterviewTypeResponse(val id: Long?, val title: String,val jobPosition: JobPositionSearchResponse)


data class InterviewTypeCreateRequest(@field:NotBlank val name: String, val jobPositionId: Long)

data class InterviewTypeUpdateRequest(val name: String?,val jobPositionId: Long?)
