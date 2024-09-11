package com.linchpino.core.enums.converters

import com.linchpino.core.enums.InterviewLogType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class InterviewLogEnumConverter : AttributeConverter<InterviewLogType, Int> {

    override fun convertToDatabaseColumn(attribute: InterviewLogType): Int =
        attribute.value

    override fun convertToEntityAttribute(dbData: Int): InterviewLogType? =
        InterviewLogType.entries.find { it.value == dbData }

}
