package com.linchpino.core.enums.converters

import com.linchpino.core.enums.RolesEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class RolesEnumConverter : AttributeConverter<RolesEnum, Int> {

    override fun convertToDatabaseColumn(attribute: RolesEnum): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): RolesEnum? =
        RolesEnum.entries.find { it.value == dbData }

}
