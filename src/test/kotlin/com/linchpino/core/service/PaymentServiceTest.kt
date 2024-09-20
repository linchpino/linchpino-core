package com.linchpino.core.service

import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.PaymentMethod
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.repository.PaymentMethodRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest{

    @InjectMocks
    private lateinit var paymentService: PaymentService
    @Mock
    private lateinit var paymentMethodRepository: PaymentMethodRepository

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

        val paymentMethodCaptor:ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

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

        val paymentMethodCaptor:ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)


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

        val paymentMethodCaptor:ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

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

        val paymentMethodCaptor:ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

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
    fun `should update payment method`(){
        // Given
        val paymentMethod = PaymentMethod().apply {
            id = 1
            type = PaymentMethodType.FREE
        }
        val paymentMethodCaptor:ArgumentCaptor<PaymentMethod> = ArgumentCaptor.forClass(PaymentMethod::class.java)

        // When
        paymentService.update(paymentMethod)

        // Then
        verify(paymentMethodRepository, times(1)).save(paymentMethodCaptor.capture())

        assertThat(paymentMethodCaptor.value.id).isEqualTo(1)
        assertThat(paymentMethodCaptor.value.type).isEqualTo(PaymentMethodType.FREE)
    }
}
