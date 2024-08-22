package com.linchpino.core.dto

import com.linchpino.core.enums.PaymentMethodType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@ValidPaymentMethod
data class PaymentMethodRequest(
    val type: PaymentMethodType?,
    val minPayment: Double? = null,
    val maxPayment: Double? = null,
    val fixRate: Double? = null
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PaymentMethodValidator::class])
annotation class ValidPaymentMethod(
    val message: String = "Invalid payment method details",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class PaymentMethodValidator : ConstraintValidator<ValidPaymentMethod, PaymentMethodRequest> {

    override fun isValid(paymentMethodDTO: PaymentMethodRequest, context: ConstraintValidatorContext): Boolean {
        var isValid = true
        context.disableDefaultConstraintViolation() // Disable the default message
        when (paymentMethodDTO.type) {
            PaymentMethodType.PAY_AS_YOU_GO -> {
                if (paymentMethodDTO.minPayment == null) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("Minimum payment must not be null")
                        .addPropertyNode("minPayment")
                        .addConstraintViolation()
                } else if (paymentMethodDTO.minPayment < 0.0) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("Minimum payment must be greater than or equal to 0.0")
                        .addPropertyNode("minPayment")
                        .addConstraintViolation()
                }

                if (paymentMethodDTO.maxPayment == null) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("Maximum payment must not be null")
                        .addPropertyNode("maxPayment")
                        .addConstraintViolation()
                } else if (paymentMethodDTO.maxPayment > 1000.0) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("Maximum payment must be less than or equal to 1000.0")
                        .addPropertyNode("maxPayment")
                        .addConstraintViolation()
                }
            }

            PaymentMethodType.FIX_PRICE -> {
                if (paymentMethodDTO.fixRate == null) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("Fixed rate must not be null")
                        .addPropertyNode("fixRate")
                        .addConstraintViolation()
                }
            }

            PaymentMethodType.FREE -> {
                if (paymentMethodDTO.minPayment != null || paymentMethodDTO.maxPayment != null || paymentMethodDTO.fixRate != null) {
                    isValid = false
                    context.buildConstraintViolationWithTemplate("No payment-related fields should be provided for FREE payment method")
                        .addPropertyNode("type")
                        .addConstraintViolation()
                }
            }

            null -> {
                isValid = false
                context.buildConstraintViolationWithTemplate("Payment method type is required")
                    .addPropertyNode("type")
                    .addConstraintViolation()
            }
        }

        return isValid
    }
}
