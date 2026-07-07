package com.example.cursotestingandroid.productdetail.presentation

import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion

data class ProductDetailUiState(
    val item: ProductWithPromotion? = null,
    val isLoading: Boolean = true
)
