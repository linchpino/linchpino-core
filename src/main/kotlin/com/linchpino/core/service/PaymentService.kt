package com.linchpino.core.service

import com.linchpino.core.dto.PaymentCreateRequest
import com.linchpino.core.dto.PaymentMethodRequest
import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.dto.PaymentVerificationRequest
import com.linchpino.core.dto.PaymentVerificationResponse
import com.linchpino.core.dto.toResponse
import com.linchpino.core.dto.toVerificationResponse
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
import java.math.BigDecimal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentRepository: PaymentRepository,
    private val interviewRepository: InterviewRepository,
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


    fun createPayment(request: PaymentCreateRequest): PaymentResponse {
        val interview = interviewRepository.findByIdOrNull(request.interviewId)
            ?: throw LinchpinException(ErrorCode.ENTITY_NOT_FOUND, "Interview not found", Interview::class.java)
        return Payment().apply {
            this.interview = interview
            refNumber = request.refNumber
            status = PaymentStatus.PENDING
        }.let {
            paymentRepository.save(it)
            it.toResponse()
        }
    }


    fun verifyPaymentFor(id: Long, request: PaymentVerificationRequest): PaymentVerificationResponse {
        return paymentRepository.findByIdOrNull(id)?.let {
            it.status = PaymentStatus.VERIFIED
            it.amount = BigDecimal(request.amount!!)
            it
        }?.toVerificationResponse() ?: throw LinchpinException(
            ErrorCode.ENTITY_NOT_FOUND,
            "payment with id: $id not found",
            Payment::class.java
        )
    }

    fun rejectPaymentFor(id: Long): PaymentResponse {
        return paymentRepository.findByIdOrNull(id)?.let {
            it.status = PaymentStatus.REJECTED
            it.toResponse()
        } ?: throw LinchpinException(
            ErrorCode.ENTITY_NOT_FOUND,
            "payment with id: $id not found",
            Payment::class.java
        )
    }

    @Transactional(readOnly = true)
    fun search(status: PaymentStatus?, refNumber: String?, pageable: Pageable): Page<PaymentResponse> {
        return paymentRepository.search(status, refNumber, pageable)
    }
}
