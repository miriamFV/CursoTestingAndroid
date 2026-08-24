package com.example.cursotestingandroid.checkout.presentation

import com.example.cursotestingandroid.core.builders.checkoutForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS de la validación del formulario de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutForm.validate], [CheckoutFormErrors.isValid], [FieldError].
 */
class CheckoutFormTest {
    @Test
    fun `given blank name when validate then nameError is REQUIRED`() {
        // GIVEN
        val blankName = " "
        val checkoutForm = checkoutForm { withName(blankName) }
        // WHEN
        val nameError = checkoutForm.validate().nameError
        // THEN
        assertEquals(FieldError.REQUIRED, nameError)
    }

    @Test
    fun `given blank address when validate then addressError is REQUIRED`() {
        // GIVEN
        val blankAddress = " "
        val checkoutForm = checkoutForm { withAddress(blankAddress) }
        // WHEN
        val addressError = checkoutForm.validate().addressError
        // THEN
        assertEquals(FieldError.REQUIRED, addressError)
    }

    @Test
    fun `given blank email when validate then emailError is REQUIRED`() {
        // GIVEN
        val blankEmail = " "
        val checkoutForm = checkoutForm { withEmail(blankEmail) }
        // WHEN
        val emailError = checkoutForm.validate().emailError
        // THEN
        assertEquals(FieldError.REQUIRED, emailError)
    }

    @Test
    fun `given malformed email when validate then emailError is INVALID_EMAIL`() {
        // GIVEN
        val malformedEmail = "apruebame@porficom"
        val checkoutForm = checkoutForm { withEmail(malformedEmail) }
        // WHEN
        val emailError = checkoutForm.validate().emailError
        // THEN
        assertEquals(FieldError.INVALID_EMAIL, emailError)
    }

    @Test
    fun `given all fields valid when validate then errors isValid is true`() {
        // GIVEN
        val correctForm =
            checkoutForm {
                withName("Miriam")
                withAddress("Kotlin")
                withEmail("apruebame@porfa.com")
            }
        // WHEN
        val isValidForm = correctForm.validate().isValid
        // THEN
        assertTrue(isValidForm)
    }
}
