package com.linchpino.core.dto

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.math.BigInteger
import kotlin.reflect.KClass

class IBAN(private val iban: String) {

    private val minimumValidLength = 15

    fun isValid(): Boolean {
        val trimmed = iban.trim().replace(" ", "")
        val hasInvalidCharacters = trimmed.any { !(it.isLetter() || it.isDigit()) }

        if(hasInvalidCharacters || trimmed.length < minimumValidLength) {
            return false
        }
        val rearrangedIban = trimmed.let { it.substring(4) + it.substring(0, 4) }.uppercase()

        val numericIban = rearrangedIban.map {
            if (it.isLetter()) {
                (it.code - 'A'.code + 10).toString()
            } else {
                it.toString()
            }
        }.joinToString("")

        val ibanAsNumber = BigInteger(numericIban)
        return ibanAsNumber.mod(BigInteger.valueOf(97)) == BigInteger.ONE
    }

    fun number(): String? {
        return if (!isValid()) {
            null
        } else {
            iban.trim().replace(" ", "").uppercase()
        }
    }
}

fun String.toIBAN(): IBAN {
    return IBAN(this)
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IBANValidator::class])
annotation class ValidIBAN(
    val message: String = "Invalid IBAN number",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class IBANValidator : ConstraintValidator<ValidIBAN, String> {

    override fun isValid(iban: String?, context: ConstraintValidatorContext): Boolean {
        return iban != null && IBAN(iban).isValid()
    }

}

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IBANValidatorForUpdate::class])
annotation class ValidIBANUpdate(
    val message: String = "Invalid IBAN number",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class IBANValidatorForUpdate : ConstraintValidator<ValidIBANUpdate, String> {

    override fun isValid(iban: String?, context: ConstraintValidatorContext): Boolean {
        if (iban == null) return true
        return IBANValidator().isValid(iban,context)
    }

}

