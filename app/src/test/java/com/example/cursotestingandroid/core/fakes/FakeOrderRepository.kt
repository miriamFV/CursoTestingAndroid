package com.example.cursotestingandroid.core.fakes

import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository

class FakeOrderRepository : OrderRepository {
    var returnError = false // Simular error
    var placeOrderCalled = false

    val orderConfirmation = OrderConfirmation(orderId = "ORD-1001", etaMinutes = 130, total = 0.0)

    override suspend fun placeOrder(): OrderConfirmation {
        placeOrderCalled = true
        if (returnError) throw Exception("Network error")
        return orderConfirmation
    }
}
