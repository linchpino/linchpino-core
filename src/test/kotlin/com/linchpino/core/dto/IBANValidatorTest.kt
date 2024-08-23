package com.linchpino.core.dto

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class IBANValidatorTest {

    @InjectMocks
    private lateinit var ibanValidator: IBANValidator

    private lateinit var context: ConstraintValidatorContext

    @BeforeEach
    fun setup() {
        context = mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `should return true for a valid IBAN`() {
        val validIBAN = "GB82 WEST 1234 5698 7654 32"
        val result = ibanValidator.isValid(validIBAN, context)
        assertThat(result).isTrue
    }

    @Test
    fun `should return false for an invalid IBAN`() {
        val invalidIBAN = "GB82 WEST 1234 5698 7654 33"
        val result = ibanValidator.isValid(invalidIBAN, context)
        assertThat(result).isFalse
    }

    @Test
    fun `should return false for a null IBAN`() {
        val result = ibanValidator.isValid(null, context)
        assertThat(result).isFalse
    }

    @Test
    fun `should return false for an empty IBAN`() {
        val emptyIBAN = ""
        val result = ibanValidator.isValid(emptyIBAN, context)
        assertThat(result).isFalse
    }

    @Test
    fun `should return false for an IBAN with invalid characters`() {
        val invalidIBAN = "GB82 WEST 1234 5698 7654 !@#"
        val result = ibanValidator.isValid(invalidIBAN, context)
        assertThat(result).isFalse
    }

    @Test
    fun `should return false for an IBAN with incorrect length`() {
        val invalidLengthIBAN = "GB82 WEST 1234 5698 7654"
        val result = ibanValidator.isValid(invalidLengthIBAN, context)
        assertThat(result).isFalse
    }
}
