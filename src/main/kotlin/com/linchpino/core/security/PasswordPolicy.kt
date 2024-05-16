package com.linchpino.core.security


import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [PasswordValidator::class])
annotation class PasswordPolicy(
    val message: String = "Password must be at least 6 character containing alpha-numeric and special characters)",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Component
class PasswordValidator(@Value("\${password.policyRegex}") var regex: String) : ConstraintValidator<PasswordPolicy, String> {

    override fun isValid(password: String?, context: ConstraintValidatorContext?) = password != null && Regex(regex).matches(password)
}
