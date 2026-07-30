package com.example.cursotestingandroid.core.mothers.uistate

import com.example.cursotestingandroid.cart.domain.model.CartItem
import com.example.cursotestingandroid.cart.domain.model.CartSummary
import com.example.cursotestingandroid.cart.presentation.CartUiState
import com.example.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.example.cursotestingandroid.core.mothers.ProductMother.bread
import com.example.cursotestingandroid.core.mothers.ProductMother.coffee
import com.example.cursotestingandroid.core.mothers.PromotionMother.percent
import com.example.cursotestingandroid.productlist.domain.model.Product
import com.example.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion

object CartUiStateMother {
    fun cartSuccess(
        summary: CartSummary? = CartSummary(
            subtotal = 11.0,
            discountTotal = 0.7,
            finalTotal = 10.3
        ),
        cartItems: List<CartItemWithPromotion> = listOf(
            cartItemWithPromotion(product = bread(), quantity = 2),
            cartItemWithPromotion(product = coffee(), quantity = 1, promotion = percent())
        ),
        isLoading: Boolean = false

    ) = CartUiState.Success(
        summary = summary,
        cartItems = cartItems,
        isLoading = isLoading
    )

    fun cartItemWithPromotion(
        product: Product,
        quantity: Int,
        promotion: ProductPromotion? = null
    ) = CartItemWithPromotion(
        cartItem = CartItem(productId = product.id, quantity = quantity),
        item = ProductWithPromotion(product = product, promotion = promotion)
    )
}