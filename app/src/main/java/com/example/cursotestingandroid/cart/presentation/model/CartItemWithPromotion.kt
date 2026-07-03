package com.example.cursotestingandroid.cart.presentation.model

import com.example.cursotestingandroid.cart.domain.model.CartItem
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class CartItemWithPromotion(
    val cartItem: CartItem,
    val item: ProductWithPromotion
)