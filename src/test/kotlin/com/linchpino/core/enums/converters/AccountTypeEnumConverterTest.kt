package com.linchpino.core.enums.converters

import com.linchpino.core.enums.AccountTypeEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AccountTypeEnumConverterTest {
    private val converter = AccountTypeEnumConverter()

    @Test
    fun `test convertToDatabaseColumn with mocked enum`() {
        val accountType = AccountTypeEnum.JOB_SEEKER
        val dbColumn = converter.convertToDatabaseColumn(accountType)
        assertEquals(accountType.value, dbColumn)
    }

    @Test
    fun `test convertToEntityAttribute`() {
        val dbData = 1
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(AccountTypeEnum.GUEST, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with invalid data enum`() {
        val dbData = 999
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(null, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with mocked enum`() {
        val mockedEnum = mock(AccountTypeEnum::class.java)
        `when`(mockedEnum.value).thenReturn(2)
        val dbData = 2
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals("JOB_SEEKER", accountType?.name)
    }
}
