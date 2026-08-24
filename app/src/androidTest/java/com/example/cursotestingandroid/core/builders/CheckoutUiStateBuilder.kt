package com.example.cursotestingandroid.core.builders

import com.example.cursotestingandroid.cart.domain.model.CartSummary
import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.example.cursotestingandroid.checkout.presentation.CheckoutForm
import com.example.cursotestingandroid.checkout.presentation.CheckoutFormErrors
import com.example.cursotestingandroid.checkout.presentation.CheckoutUiState
import com.example.cursotestingandroid.checkout.presentation.validate

class CheckoutUiStateBuilder {
    private var summary: CartSummary = cartSummary { }
    private var form: CheckoutForm = checkoutForm { }
    private var errors: CheckoutFormErrors? = null
    private var isCartEmpty: Boolean = false
    private var isSubmitting: Boolean = false
    private var canSubmit: Boolean? = null

    fun withForm(form: CheckoutForm) = apply { this.form = form }

    fun withErrors(errors: CheckoutFormErrors) = apply { this.errors = errors }

    fun withIsCartEmpty(isCartEmpty: Boolean) = apply { this.isCartEmpty = isCartEmpty }

    fun withIsSubmitting(isSubmitting: Boolean) = apply { this.isSubmitting = isSubmitting }

    fun withCanSubmit(canSubmit: Boolean) = apply { this.canSubmit = canSubmit }

    fun buildIdle(): CheckoutUiState.Idle {
        val finalErrors = errors ?: form.validate()

        val finalCanSubmit = canSubmit ?: (!isCartEmpty && !isSubmitting && finalErrors.isValid)

        return CheckoutUiState.Idle(
            summary = summary,
            form = form,
            errors = finalErrors,
            isCartEmpty = isCartEmpty,
            isSubmitting = isSubmitting,
            canSubmit = finalCanSubmit,
        )
    }
}

fun checkoutIdleState(block: CheckoutUiStateBuilder.() -> Unit = {}) = CheckoutUiStateBuilder().apply(block).buildIdle()

fun checkoutSuccessState(
    confirmation: OrderConfirmation = OrderConfirmation(orderId = "ORD-1001", etaMinutes = 130, total = 0.0),
) = CheckoutUiState.Success(confirmation)

fun checkoutErrorState(message: String = "Error en checkout") = CheckoutUiState.Error(message)
