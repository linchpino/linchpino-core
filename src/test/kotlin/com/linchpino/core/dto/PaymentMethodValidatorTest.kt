package com.linchpino.core.dto

import com.linchpino.core.enums.PaymentMethodType
import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentMethodValidatorTest {

    private lateinit var validator: PaymentMethodValidator
    private lateinit var context: ConstraintValidatorContext
    private lateinit var violationBuilder: ConstraintValidatorContext.ConstraintViolationBuilder

    @BeforeEach
    fun setUp() {
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder::class.java)

        validator = PaymentMethodValidator()
        context = mock(ConstraintValidatorContext::class.java)
        // Disable default constraint violation to capture custom ones
        doNothing().`when`(context).disableDefaultConstraintViolation() // Correct way to handle void method

    }

    @Test
    fun `should be valid for PAY_AS_YOU_GO with valid min and max payments`() {
        val dto = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = 100.0,
            maxPayment = 500.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isTrue()
    }

    @Test
    fun `should be invalid for PAY_AS_YOU_GO with null minPayment`() {
        `when`(context.buildConstraintViolationWithTemplate("Minimum payment must not be null"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("minPayment"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))

        val dto = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = null,
            maxPayment = 500.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
        // Additional checks could be made here to validate that the right error message is added
    }

    @Test
    fun `should be invalid for PAY_AS_YOU_GO with minPayment less than 0`() {
        `when`(context.buildConstraintViolationWithTemplate("Minimum payment must be greater than or equal to 0.0"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("minPayment"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))

        val dto = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = -10.0,
            maxPayment = 500.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `should be invalid for PAY_AS_YOU_GO with null maxPayment`() {
        `when`(context.buildConstraintViolationWithTemplate("Maximum payment must not be null"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("maxPayment"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))

        val dto = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = 100.0,
            maxPayment = null
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `should be invalid for PAY_AS_YOU_GO with maxPayment greater than 1000`() {
        `when`(context.buildConstraintViolationWithTemplate("Maximum payment must be less than or equal to 1000.0"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("maxPayment"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))

        val dto = PaymentMethodRequest(
            type = PaymentMethodType.PAY_AS_YOU_GO,
            minPayment = 100.0,
            maxPayment = 1500.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `should be valid for FIX_PRICE with valid fixRate`() {

        val dto = PaymentMethodRequest(
            type = PaymentMethodType.FIX_PRICE,
            fixRate = 300.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isTrue()
    }

    @Test
    fun `should be invalid for FIX_PRICE with null fixRate`() {
        `when`(context.buildConstraintViolationWithTemplate("Fixed rate must not be null"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("fixRate"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))


        val dto = PaymentMethodRequest(
            type = PaymentMethodType.FIX_PRICE,
            fixRate = null
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `should be valid for FREE with no payment fields`() {
        val dto = PaymentMethodRequest(
            type = PaymentMethodType.FREE
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isTrue()
    }

    @Test
    fun `should be invalid for FREE with payment fields set`() {
        `when`(context.buildConstraintViolationWithTemplate("No payment-related fields should be provided for FREE payment method"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("type"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))


        val dto = PaymentMethodRequest(
            type = PaymentMethodType.FREE,
            minPayment = 100.0
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }

    @Test
    fun `should be invalid when payment type is null`() {
        `when`(context.buildConstraintViolationWithTemplate("Payment method type is required"))
            .thenReturn(violationBuilder)

        `when`(violationBuilder.addPropertyNode("type"))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java))

        val dto = PaymentMethodRequest(
            type = null
        )

        val isValid = validator.isValid(dto, context)

        assertThat(isValid).isFalse()
    }
}
