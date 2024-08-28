package com.linchpino.core.entity

import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.enums.converters.PaymentMethodTypeEnumConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "PAYMENT_METHOD")
class PaymentMethod {

    @Id
    var id: Long? = null

    @Convert(converter = PaymentMethodTypeEnumConverter::class)
    var type: PaymentMethodType = PaymentMethodType.FREE

    var minPayment: Double? = null

    var maxPayment: Double? = null

    var fixRate: Double? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var account: Account? = null
}
