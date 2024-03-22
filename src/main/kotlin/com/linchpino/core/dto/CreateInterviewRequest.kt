package com.linchpino.core.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class CreateInterviewRequest(
    @field:NotNull(message = "jobPositionId is required") val jobPositionId: Long,
    @field:NotNull(message = "interviewTypeId is required") val interviewTypeId: Long,
    @field:NotNull(message = "timeSlotId is required") val timeSlotId: Long,
    @field:NotNull(message = "mentorAccId is required") val mentorAccId: Long,
    @field:Email(message = "jobSeekerEmail is required") val jobSeekerEmail: String,
)

data class CreateInterviewResult(
    val id: Long?,
    val jobPositionId: Long?,
    val interviewTypeId: Long?,
    val timeSlotId: Long?,
    val mentorAccId: Long?,
    val jobSeekerEmail: String?,
)
