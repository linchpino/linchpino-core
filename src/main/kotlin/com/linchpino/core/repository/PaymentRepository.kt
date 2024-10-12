package com.linchpino.core.repository

import com.linchpino.core.dto.PaymentResponse
import com.linchpino.core.entity.Payment
import com.linchpino.core.enums.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PaymentRepository : CrudRepository<Payment, Long> {

    @Query(
        """
        select NEW com.linchpino.core.dto.PaymentResponse(
            p.id,
            p.interview.id,
            p.refNumber,
            p.amount,
            p.status
        )
         from Payment p
        where
        (:status is null or p.status = :status) and (:refNumber is null or p.refNumber = :refNumber)
    """
    )
    fun search(status: PaymentStatus?, refNumber: String?, pageable: Pageable): Page<PaymentResponse>
}
