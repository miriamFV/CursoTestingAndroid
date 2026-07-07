package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCartItemUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(productId: String, quantity: Int){
        if(quantity < 0){
            throw AppError.Validation.QuantityMustBePositive
        }
        if(quantity == 0){
            cartRepository.removeFromCart(productId = productId)
            return
        }
        val product = productRepository.getProductById(productId).first() ?: throw AppError.NotFoundError
        if(quantity > product.stock){
            throw AppError.Validation.InsufficientStock(product.stock)
        }

        cartRepository.updateQuantity(productId = productId, quantity = quantity)
    }
}