package com.example.cursotestingandroid.productdetail.presentation

sealed interface ProductDetailEvent {
    data object UnknownError : ProductDetailEvent

    data object NetworkError : ProductDetailEvent

    data object InsufficientStock : ProductDetailEvent

    data object SuccessAddToCart : ProductDetailEvent
}
