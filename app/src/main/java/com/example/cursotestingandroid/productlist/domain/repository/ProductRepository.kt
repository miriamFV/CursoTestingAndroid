package com.example.cursotestingandroid.productlist.domain.repository

import com.example.cursotestingandroid.productlist.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    suspend fun refreshProduct()
}