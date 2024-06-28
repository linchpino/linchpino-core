package com.linchpino.core.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class InterviewFeedBackRequest(@field:Max(5) @field:Min(1) val status: Int, @field:NotBlank val content: String)
