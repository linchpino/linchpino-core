package com.linchpino.core.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IBANTest {

    @Test
    fun `should return true for a valid IBAN`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654 32")
        assertThat(iban.isValid()).isTrue
    }

    @Test
    fun `should return false for an invalid IBAN`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654 33")
        assertThat(iban.isValid()).isFalse
    }

    @Test
    fun `should return null for an invalid IBAN number`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654 33")
        assertThat(iban.number()).isNull()
    }

    @Test
    fun `should return the IBAN number for a valid IBAN`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654 32")
        assertThat(iban.number()).isEqualTo("GB82WEST12345698765432")
    }

    @Test
    fun `should handle IBAN with leading and trailing spaces`() {
        val iban = IBAN("  GB82 WEST 1234 5698 7654 32  ")
        assertThat(iban.isValid()).isTrue
        assertThat(iban.number()).isEqualTo("GB82WEST12345698765432")
    }

    @Test
    fun `should return false for IBAN with invalid characters`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654 !@#")
        assertThat(iban.isValid()).isFalse
        assertThat(iban.number()).isNull()
    }

    @Test
    fun `should handle lower-case letters in IBAN`() {
        val iban = IBAN("gb82 west 1234 5698 7654 32")
        assertThat(iban.isValid()).isTrue
        assertThat(iban.number()).isEqualTo("gb82west12345698765432".uppercase())
    }

    @Test
    fun `should return false for an IBAN with incorrect length`() {
        val iban = IBAN("GB82 WEST 1234 5698 7654")
        assertThat(iban.isValid()).isFalse
        assertThat(iban.number()).isNull()
    }

    @Test
    fun `should return false for an empty IBAN`() {
        val iban = IBAN("")
        assertThat(iban.isValid()).isFalse
    }
}
