package com.linchpino.core.controller

import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.dto.PaymentVerificationRequest
import com.linchpino.core.dto.PaymentVerificationResponse
import com.linchpino.core.enums.PaymentStatus
import com.linchpino.core.service.PaymentService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/admin/payments")
class PaymentAdminController(private val paymentService: PaymentService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/verify")
    fun verifyPayment(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: PaymentVerificationRequest
    ): PaymentVerificationResponse {
        return paymentService.verifyPaymentFor(id, request)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/reject")
    fun rejectPayment(@PathVariable("id") id: Long): PaymentResponse {
        return paymentService.rejectPaymentFor(id)
    }

    @GetMapping
    fun searchPayments(
        @RequestParam(required = false) status: PaymentStatus?,
        @RequestParam(required = false) refNumber: String?,
        pageable: Pageable
    ): Page<PaymentResponse> {
        return paymentService.search(status, refNumber, pageable)
    }
}
