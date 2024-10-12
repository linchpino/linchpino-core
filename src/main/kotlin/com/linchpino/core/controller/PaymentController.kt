package com.linchpino.core.controller

import com.linchpino.core.dto.PaymentCreateRequest
import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.service.PaymentService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    fun createPayment(@Valid @RequestBody request: PaymentCreateRequest): PaymentResponse {
        return paymentService.createPayment(request)
    }

}
