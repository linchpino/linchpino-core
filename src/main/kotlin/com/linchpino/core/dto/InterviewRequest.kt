package com.linchpino.core.dto

import jakarta.validation.constraints.NotNull

data class InterviewRequest(
    @field:NotNull(message = "JobPositionId is required") val JobPositionId: Long,
    @field:NotNull(message = "InterviewTypeId is required") val InterviewTypeId: Long,
    @field:NotNull(message = "MentorTimeSlotId is required") val MentorTimeSlotId: Long,
    @field:NotNull(message = "jobSeekerAccount is required") val jobSeekerAccount: SilenceAccountRequest,
)

data class InterviewResult(
    val id: Long,
    val JobPositionId: Long,
    val InterviewTypeId: Long,
    val MentorTimeSlotId: Long,
    val jobSeekerAccount: SilenceAccountResult,
)