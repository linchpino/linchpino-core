package com.linchpino.core.service

import com.linchpino.core.dto.PaymentCreateRequest
import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.dto.PaymentVerificationRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.Payment
import com.linchpino.core.entity.PaymentMethod
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.enums.PaymentStatus
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.PaymentMethodRepository
import com.linchpino.core.repository.PaymentRepository
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {

    @InjectMocks
    private lateinit var paymentService: PaymentService

    @Mock
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @Mock
    private lateinit var interviewRepository: InterviewRepository

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Test
    fun `should save PAY_AS_YOU_GO payment method with valid min and max payment`() {
        // Given
        val account = Account().apply {
            id = 1
            firstName = "john"
            lastName = "doe"
            email = "john.doe@example.com"
        }

        val request = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = 100.0,
            maxPayment = 500.0
        )

        val paymentMethodCaptor: ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

        // When
        paymentService.savePaymentMethod(request, account)

        // Then
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture())

        val savedPaymentMethod = paymentMethodCaptor.value
        assertThat(savedPaymentMethod.type).isEqualTo(PaymentMethodType.PAY_AS_YOU_GO)
        assertThat(savedPaymentMethod.minPayment).isEqualTo(100.0)
        assertThat(savedPaymentMethod.maxPayment).isEqualTo(500.0)
        assertThat(savedPaymentMethod.fixRate).isNull()
        assertThat(savedPaymentMethod.account).isEqualTo(account)
    }

    @Test
    fun `should save FIX_PRICE payment method with valid fix rate`() {
        // Given
        val account = Account().apply {
            id = 1
            firstName = "john"
            lastName = "doe"
            email = "john.doe@example.com"
        }

        val request = PaymentMethodRequest(
            type = PaymentMethodType.FIX_PRICE,
            fixRate = 250.0
        )

        val paymentMethodCaptor: ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)


        // When
        paymentService.savePaymentMethod(request, account)

        // Then
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture())

        val savedPaymentMethod = paymentMethodCaptor.value
        assertThat(savedPaymentMethod.type).isEqualTo(PaymentMethodType.FIX_PRICE)
        assertThat(savedPaymentMethod.fixRate).isEqualTo(250.0)
        assertThat(savedPaymentMethod.minPayment).isNull()
        assertThat(savedPaymentMethod.maxPayment).isNull()
        assertThat(savedPaymentMethod.account).isEqualTo(account)
    }

    @Test
    fun `should save FREE payment method`() {
        // Given
        val account = Account().apply {
            id = 1
            firstName = "john"
            lastName = "doe"
            email = "john.doe@example.com"
        }

        val request = PaymentMethodRequest(
            type = PaymentMethodType.FREE
        )

        val paymentMethodCaptor: ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

        // When
        paymentService.savePaymentMethod(request, account)

        // Then
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture())

        val savedPaymentMethod = paymentMethodCaptor.value
        assertThat(savedPaymentMethod.type).isEqualTo(PaymentMethodType.FREE)
        assertThat(savedPaymentMethod.minPayment).isNull()
        assertThat(savedPaymentMethod.maxPayment).isNull()
        assertThat(savedPaymentMethod.fixRate).isNull()
        assertThat(savedPaymentMethod.account).isEqualTo(account)
    }

    @Test
    fun `should save payment method with default FREE type when type is null`() {
        // Given
        val account = Account().apply {
            id = 1
            firstName = "john"
            lastName = "doe"
            email = "john.doe@example.com"
        }

        val request = PaymentMethodRequest(
            type = null
        )

        val paymentMethodCaptor: ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

        // When
        paymentService.savePaymentMethod(request, account)

        // Then
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture())

        val savedPaymentMethod = paymentMethodCaptor.value
        assertThat(savedPaymentMethod.type).isEqualTo(PaymentMethodType.FREE)
        assertThat(savedPaymentMethod.minPayment).isNull()
        assertThat(savedPaymentMethod.maxPayment).isNull()
        assertThat(savedPaymentMethod.fixRate).isNull()
        assertThat(savedPaymentMethod.account).isEqualTo(account)
    }


    @Test
    fun `should update payment method`() {
        // Given
        val paymentMethod = PaymentMethod().apply {
            id = 1
            type = PaymentMethodType.FREE
        }
        val paymentMethodCaptor: ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

        // When
        paymentService.update(paymentMethod)

        // Then
        verify(paymentMethodRepository, times(1)).save(paymentMethodCaptor.capture())

        assertThat(paymentMethodCaptor.value.id).isEqualTo(1)
        assertThat(paymentMethodCaptor.value.type).isEqualTo(PaymentMethodType.FREE)
    }

    @Test
    fun `should create a payment`() {
        val interview = Interview().apply {
            id = 1
        }
        val request = PaymentCreateRequest(1, "ref")

        `when`(interviewRepository.findById(request.interviewId!!)).thenReturn(Optional.of(interview))

        val response = paymentService.createPayment(request)

        assertThat(response.status).isEqualTo(PaymentStatus.PENDING)
        assertThat(response.interviewId).isEqualTo(interview.id)
        assertThat(response.refNumber).isEqualTo(request.refNumber)
        verify(paymentRepository, times(1)).save(any())
    }

    @Test
    fun `create payment should throw exception if interview not found`() {
        val request = PaymentCreateRequest(1, "ref")
        `when`(interviewRepository.findById(request.interviewId!!)).thenReturn(Optional.empty())

        val ex = assertThrows(LinchpinException::class.java) {
            paymentService.createPayment(request)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun `verify payment should change the status of payment to VERIFIED`() {
        val request = PaymentVerificationRequest(15.5)
        val payment = Payment().apply {
            id = 1
            status = PaymentStatus.PENDING
        }
        `when`(paymentRepository.findById(1)).thenReturn(Optional.of(payment))

        val response = paymentService.verifyPaymentFor(1, request)

        assertThat(response.amount).isEqualTo("${request.amount}")
        assertThat(response.status).isEqualTo(PaymentStatus.VERIFIED)
        assertThat(response.id).isEqualTo(payment.id)

    }

    @Test
    fun `verify payment should throw exception if payment not found`() {

        `when`(paymentRepository.findById(1)).thenReturn(Optional.empty())

        val ex = assertThrows(LinchpinException::class.java) {
            paymentService.verifyPaymentFor(1, PaymentVerificationRequest(15.5))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun `reject payment should change payment status to REJECTED`() {
        val payment = Payment().apply {
            id = 1
            status = PaymentStatus.VERIFIED
        }

        `when`(paymentRepository.findById(1)).thenReturn(Optional.of(payment))

        val response = paymentService.rejectPaymentFor(1)

        assertThat(response.id).isEqualTo(payment.id)
        assertThat(response.status).isEqualTo(PaymentStatus.REJECTED)
    }

    @Test
    fun `reject payment should throw exceptoin if payment does not exist`() {

        `when`(paymentRepository.findById(1)).thenReturn(Optional.empty())

        val ex = assertThrows(LinchpinException::class.java) {
            paymentService.rejectPaymentFor(1)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

}
