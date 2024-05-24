package com.linchpino.core.dto

import com.linchpino.core.entity.Interview
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

data class CreateInterviewRequest(
    @field:NotNull(message = "jobPositionId is required") val jobPositionId: Long,
    @field:NotNull(message = "interviewTypeId is required") val interviewTypeId: Long,
    @field:NotNull(message = "timeSlotId is required") val timeSlotId: Long,
    @field:NotNull(message = "mentorAccId is required") val mentorAccId: Long,
    @field:Email(message = "jobSeekerEmail is required") val jobSeekerEmail: String,
)

data class CreateInterviewResult(
    val interviewId: Long?,
    val jobPositionId: Long?,
    val interviewTypeId: Long?,
    val timeSlotId: Long?,
    val mentorAccountId: Long?,
    val jobSeekerEmail: String?,
)

data class InterviewListResponse(
    val intervieweeId: Long,
    val intervieweeName: String,
    val fromTime: ZonedDateTime,
    val toTime: ZonedDateTime,
    val interviewType: String,
)

fun Interview.toCreateInterviewResult(): CreateInterviewResult = CreateInterviewResult(
    id,
    jobPosition?.id,
    interviewType?.id,
    timeSlot?.id,
    mentorAccount?.id,
    jobSeekerAccount?.email,
)
