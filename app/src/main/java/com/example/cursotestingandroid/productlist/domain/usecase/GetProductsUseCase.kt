package com.example.cursotestingandroid.productlist.domain.usecase

import com.example.cursotestingandroid.productlist.domain.model.Product
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
){

    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getProducts()
    }

}