package com.linchpino.core.enums.converters


import com.linchpino.core.enums.AccountStatusEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class AccountStatusEnumConverter : AttributeConverter<AccountStatusEnum, Int> {

    override fun convertToDatabaseColumn(attribute: AccountStatusEnum): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): AccountStatusEnum? =
        AccountStatusEnum.entries.find { it.value == dbData }

}
