package com.linchpino.core.enums.converters

import com.linchpino.core.enums.PaymentMethodType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PaymentMethodTypeEnumConverter : AttributeConverter<PaymentMethodType, Int> {

    override fun convertToDatabaseColumn(attribute: PaymentMethodType): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): PaymentMethodType? =
        PaymentMethodType.entries.find { it.value == dbData }

}
