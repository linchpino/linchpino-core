package com.linchpino.core.enums.converters

import com.linchpino.core.enums.MentorTimeSlotEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MentorTimeSlotEnumConverter : AttributeConverter<MentorTimeSlotEnum, Int> {

    override fun convertToDatabaseColumn(attribute: MentorTimeSlotEnum): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): MentorTimeSlotEnum? =
        MentorTimeSlotEnum.entries.find { it.value == dbData }

}
