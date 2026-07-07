package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
){
    suspend operator fun invoke(productId: String, quantity: Int = 1){
        if(quantity <= 0){
            throw AppError.Validation.QuantityMustBePositive
        }
        val product = productRepository.getProductById(productId).first() ?: throw AppError.NotFoundError

        val existingItem = cartRepository.getCartItemById(productId = productId)
        val newQuantity = (existingItem?.quantity ?: 0) + quantity
        if(newQuantity > product.stock) throw AppError.Validation.InsufficientStock(product.stock)

        cartRepository.addToCart(productId = productId, quantity = quantity)
    }
}