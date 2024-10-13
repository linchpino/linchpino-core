package com.linchpino.core.controller

import com.linchpino.core.dto.PaymentCreateRequest
import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.enums.PaymentStatus
import com.linchpino.core.service.PaymentService
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentControllerTest {

    @InjectMocks
    lateinit var paymentController: PaymentController

    @Mock
    lateinit var paymentService: PaymentService

    @Test
    fun `test create payment calls service`() {
        val request = PaymentCreateRequest(1L, "refNumber")
        val response = PaymentResponse(1L, 1L, "refNumber", BigDecimal.ZERO, PaymentStatus.PENDING)
        `when`(paymentService.createPayment(request)).thenReturn(response)

        val result = paymentController.createPayment(request)

        verify(paymentService, times(1)).createPayment(request)
        assertThat(result).isEqualTo(response)
    }
}
