package com.linchpino.core.dto

import java.time.ZonedDateTime

data class InterviewListResponse(
    val intervieweeId: Long,
    val intervieweeName: String,
    val fromTime: ZonedDateTime,
    val toTime: ZonedDateTime,
    val interviewType: String,
)
