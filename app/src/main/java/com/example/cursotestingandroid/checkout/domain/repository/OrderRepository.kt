package com.example.cursotestingandroid.checkout.domain.repository

import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation

interface OrderRepository {
    suspend fun placeOrder(): OrderConfirmation
}
