package com.example.cursotestingandroid.productlist.presentation

import com.example.cursotestingandroid.productlist.domain.model.Product

sealed class ProductListUiState {

    data object Loading: ProductListUiState()
    data class Error(val message: String): ProductListUiState()
    data class Success(
        val products: List<Product>,
        //val categories: List<>,
        //val selectedCategory: String,
        //val sortOption
    ): ProductListUiState()
}