package com.linchpino.core.dto

import jakarta.validation.constraints.NotNull

data class InterviewRequest(
    @field:NotNull(message = "jobPositionId is required") val jobPositionId: Long,
    @field:NotNull(message = "interviewTypeId is required") val interviewTypeId: Long,
    @field:NotNull(message = "timeSlotId is required") val timeSlotId: Long,
    @field:NotNull(message = "jobSeekerEmail is required") val jobSeekerEmail: String,
)

data class InterviewResult(
    val id: Long?,
    val jobPositionId: Long?,
    val interviewTypeId: Long?,
    val timeSlotId: Long?,
    val jobSeekerEmail: String?,
)