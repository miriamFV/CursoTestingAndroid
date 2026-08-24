package com.example.cursotestingandroid.core.builders

import com.example.cursotestingandroid.checkout.presentation.CheckoutForm

class CheckoutFormBuilder {
    private var name: String = "Miriam"
    private var address: String = "Kotlin"
    private var email: String = "apruebame@porfa.com"

    fun withName(name: String) = apply { this.name = name }

    fun withAddress(address: String) = apply { this.address = address }

    fun withEmail(email: String) = apply { this.email = email }

    fun build() =
        CheckoutForm(
            name = name,
            address = address,
            email = email,
        )
}

fun checkoutForm(block: CheckoutFormBuilder.() -> Unit = {}) = CheckoutFormBuilder().apply(block).build()
