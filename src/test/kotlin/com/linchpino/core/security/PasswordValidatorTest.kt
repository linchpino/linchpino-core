package com.linchpino.core.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PasswordValidatorTest{

    val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$"

    @Test
    fun `test password does not match password policy`(){

        val invalidPassword1 = "secret"
        val invalidPassword2 = "secret1"
        val invalidPassword3 = "Secret1"
        val invalidPassword4 = "Se1!"
        val validator = PasswordValidator(regex)

        assertThat(validator.isValid(invalidPassword1, null)).isFalse()
        assertThat(validator.isValid(invalidPassword2, null)).isFalse()
        assertThat(validator.isValid(invalidPassword3, null)).isFalse()
        assertThat(validator.isValid(invalidPassword4, null)).isFalse()
    }

    @Test
    fun `test password meets policy`(){
        val validPassword = "Se1!cret"
        val validator = PasswordValidator(regex)
        assertThat(validator.isValid(validPassword, null)).isTrue()
    }
}
