package com.example.cursotestingandroid.checkout.domain.usecase

import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): Result<OrderConfirmation>{
        return try {
            val confirmation = orderRepository.placeOrder()
            cartRepository.clearCart()
            Result.success(confirmation)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}
