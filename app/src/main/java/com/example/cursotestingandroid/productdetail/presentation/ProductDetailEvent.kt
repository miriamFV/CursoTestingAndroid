package com.example.cursotestingandroid.productdetail.presentation

sealed interface ProductDetailEvent {
    data object UNKNOWN_ERROR: ProductDetailEvent
    data object NETWORK_ERROR: ProductDetailEvent
    data object INSUFFICIENT_STOCK: ProductDetailEvent
    data object SUCCESS_ADD_TO_CART: ProductDetailEvent
}