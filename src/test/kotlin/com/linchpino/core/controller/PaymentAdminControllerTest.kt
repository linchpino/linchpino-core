package com.linchpino.core.controller

import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.dto.PaymentVerificationRequest
import com.linchpino.core.dto.PaymentVerificationResponse
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class PaymentAdminControllerTest {

    @InjectMocks
    lateinit var paymentAdminController: PaymentAdminController

    @Mock
    lateinit var paymentService: PaymentService


    @Test
    fun `test verify payment`() {

        val response = PaymentVerificationResponse(1, 1, "refNumber", "10.5", PaymentStatus.VERIFIED)
        val request = PaymentVerificationRequest(10.5)

        `when`(paymentService.verifyPaymentFor(1, request)).thenReturn(response)

        val result = paymentAdminController.verifyPayment(1, request)

        verify(paymentService, times(1)).verifyPaymentFor(1, request)
        assertThat(result).isEqualTo(response)
    }

    @Test
    fun `test reject payment`() {

        val response = PaymentResponse(1, 1, "refNumber", BigDecimal("10.5"), PaymentStatus.REJECTED)

        `when`(paymentService.rejectPaymentFor(1)).thenReturn(response)

        val result = paymentAdminController.rejectPayment(1)

        verify(paymentService, times(1)).rejectPaymentFor(1)
        assertThat(result).isEqualTo(response)
    }

    @Test
    fun `test search payment`() {

        val response = PageImpl(listOf(PaymentResponse(1, 1, "refNumber", BigDecimal("10.5"), PaymentStatus.REJECTED)))
        `when`(paymentService.search(PaymentStatus.PENDING, "refNumber", Pageable.ofSize(10))).thenReturn(
            response
        )

        val result = paymentAdminController.searchPayments(PaymentStatus.PENDING, "refNumber", Pageable.ofSize(10))

        verify(paymentService, times(1)).search(PaymentStatus.PENDING, "refNumber", Pageable.ofSize(10))
        assertThat(result).isEqualTo(response)
    }

}
