package com.linchpino.core.dto

import jakarta.validation.constraints.NotBlank

data class InterviewTypeSearchResponse(val id: Long?, val title: String)


data class InterviewTypeCreateRequest(@field:NotBlank val name:String, val jobPositionId:Long)
