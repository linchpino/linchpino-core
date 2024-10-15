package com.linchpino.core.entity

import com.linchpino.core.enums.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal

@Entity
@Table(
    name = "Payment", uniqueConstraints = [
        UniqueConstraint(name = "uc_payment_refnumber", columnNames = ["refNumber"])
    ]
)
class Payment : AbstractEntity() {


    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING

    @Column(nullable = false, unique = true)
    var refNumber: String? = null

    var amount: BigDecimal = BigDecimal.ZERO

    @ManyToOne(fetch = FetchType.LAZY)
    var interview: Interview? = null

}
