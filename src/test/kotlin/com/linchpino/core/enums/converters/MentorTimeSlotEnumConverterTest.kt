package com.linchpino.core.enums.converters

import com.linchpino.core.enums.MentorTimeSlotEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MentorTimeSlotEnumConverterTest {
    private val converter = MentorTimeSlotEnumConverter()

    @Test
    fun `test convertToDatabaseColumn with mocked enum`() {
        val accountType = MentorTimeSlotEnum.AVAILABLE
        val dbColumn = converter.convertToDatabaseColumn(accountType)
        assertEquals(accountType.value, dbColumn)
    }

    @Test
    fun `test convertToEntityAttribute`() {
        val dbData = 3
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(MentorTimeSlotEnum.ALLOCATED, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with invalid data enum`() {
        val dbData = 999
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(null, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with mocked enum`() {
        val mockedEnum = Mockito.mock(MentorTimeSlotEnum::class.java)
        Mockito.`when`(mockedEnum.value).thenReturn(2)
        val dbData = 2
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals("DRAFT", accountType?.name)
    }
}
