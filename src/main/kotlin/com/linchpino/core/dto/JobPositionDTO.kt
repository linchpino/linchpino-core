package com.linchpino.core.dto

import jakarta.validation.constraints.NotBlank

data class JobPositionSearchResponse(val id: Long?, val title: String)

data class JobPositionCreateRequest(@field:NotBlank val title: String)
