package com.linchpino.core.controller

import com.linchpino.core.dto.PaymentCreateRequest
import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPayment(@Valid @RequestBody request: PaymentCreateRequest): PaymentResponse {
        return paymentService.createPayment(request)
    }

}
