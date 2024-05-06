package com.linchpino.core.enums.converters

import com.linchpino.core.enums.AccountStatusEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class AccountStatusEnumConverterTest {
    private val converter = AccountStatusEnumConverter()

    @Test
    fun `test convertToDatabaseColumn with mocked enum`() {
        val accountType = AccountStatusEnum.ACTIVATED
        val dbColumn = converter.convertToDatabaseColumn(accountType)
        assertEquals(accountType.value, dbColumn)
    }

    @Test
    fun `test convertToEntityAttribute`() {
        val dbData = 1
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(AccountStatusEnum.ACTIVATED, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with invalid data enum`() {
        val dbData = 999
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals(null, accountType)
    }

    @Test
    fun `test convertToEntityAttribute with mocked enum`() {
        val mockedEnum = Mockito.mock(AccountStatusEnum::class.java)
        Mockito.`when`(mockedEnum.value).thenReturn(2)
        val dbData = 2
        val accountType = converter.convertToEntityAttribute(dbData)
        assertEquals("DEACTIVATED", accountType?.name)
    }
}
