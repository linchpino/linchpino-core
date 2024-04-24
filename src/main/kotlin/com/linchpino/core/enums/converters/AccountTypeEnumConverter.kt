package com.linchpino.core.enums.converters

import com.linchpino.core.enums.AccountTypeEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter()
class AccountTypeEnumConverter : AttributeConverter<AccountTypeEnum, Int> {

    override fun convertToDatabaseColumn(attribute: AccountTypeEnum): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): AccountTypeEnum? =
        AccountTypeEnum.entries.find { it.value == dbData }

}

