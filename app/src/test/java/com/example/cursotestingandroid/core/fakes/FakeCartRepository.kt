package com.example.cursotestingandroid.core.fakes

import com.example.cursotestingandroid.cart.domain.model.CartItem
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.domain.model.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.List

class FakeCartRepository : CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    fun setCartItems(items: List<CartItem>){
        _cartItems.value = items
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(productId: String, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }
        if (existingIndex >= 0) {
            val item = currentItems[existingIndex]
            currentItems[existingIndex] = item.copy(quantity = item.quantity + quantity)
        }else{
            currentItems.add(CartItem(productId = productId, quantity = quantity))
        }
        _cartItems.value = currentItems
    }

    override suspend fun removeFromCart(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            currentItems.removeAt(existingIndex)
        }else{
            throw AppError.NotFoundError
        }
        _cartItems.value = currentItems
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            val item = currentItems[existingIndex]
            currentItems[existingIndex] = item.copy(quantity = item.quantity + quantity)
        }else{
            throw AppError.NotFoundError
        }
        _cartItems.value = currentItems
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override suspend fun getCartItemById(productId: String): CartItem? {
        return _cartItems.value.find { it.productId == productId }
    }
}