package com.linchpino.core.service

import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.PaymentMethod
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.repository.PaymentMethodRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentMethodRepository: PaymentMethodRepository
) {

    fun savePaymentMethod(request: PaymentMethodRequest, account: Account): PaymentMethod {
        return PaymentMethod().apply {
            type = request.type ?: PaymentMethodType.FREE
            minPayment = if (request.type == PaymentMethodType.PAY_AS_YOU_GO) request.minPayment else null
            maxPayment = if (request.type == PaymentMethodType.PAY_AS_YOU_GO) request.maxPayment else null
            fixRate = if (request.type == PaymentMethodType.FIX_PRICE) request.fixRate else null
            this.account = account
        }.also {
            paymentMethodRepository.save(it)
        }
    }


    @Transactional(readOnly = true)
    fun findByIdOrNull(id: Long) =
        paymentMethodRepository.findByIdOrNull(id)


    fun update(paymentMethod: PaymentMethod) {
        paymentMethodRepository.save(paymentMethod)
    }
}
