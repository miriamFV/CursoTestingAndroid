package com.example.cursotestingandroid.cart.presentation

import com.example.cursotestingandroid.cart.domain.model.CartSummary
import com.example.cursotestingandroid.cart.presentation.model.CartItemWithPromotion


sealed class CartUiState {
    data class Success(
        val summary: CartSummary? = null,
        val cartItems: List<CartItemWithPromotion>,
        val isLoading: Boolean
    ): CartUiState()
    data object Loading: CartUiState()
    data class Error(val message:String): CartUiState()
}