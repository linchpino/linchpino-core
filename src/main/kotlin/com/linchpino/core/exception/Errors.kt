package com.linchpino.core.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

data class ErrorMessage(val field: String, val message: String)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val validationErrorMap: List<ErrorMessage>,
    val path: String
)
