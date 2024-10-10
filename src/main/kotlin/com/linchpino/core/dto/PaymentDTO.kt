package com.linchpino.core.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.linchpino.core.entity.Payment
import com.linchpino.core.enums.PaymentStatus
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class PaymentCreateRequest(
    @field:NotNull(message = "interview id must be provided") val interviewId: Long?,
    @field:NotNull(message = "refNumber must be provided") val refNumber: String?
)

data class PaymentResponse(
    val id: Long?,
    val interviewId: Long?,
    val refNumber: String?,
    @JsonSerialize(using = ToStringSerializer::class)
    val amount: BigDecimal?,
    val status: PaymentStatus
)

data class PaymentVerificationResponse(
    val id: Long?,
    val interviewId: Long?,
    val refNumber: String?,
    val amount: String?,
    val status: PaymentStatus
)

fun Payment.toResponse() = PaymentResponse(id, interview?.id, refNumber, amount, status)

fun Payment.toVerificationResponse() =
    PaymentVerificationResponse(id, interview?.id, refNumber, amount.toPlainString(), status)

data class PaymentVerificationRequest(
    @field:NotNull(message = "payment amount must not be null") val amount: Double?,
)
