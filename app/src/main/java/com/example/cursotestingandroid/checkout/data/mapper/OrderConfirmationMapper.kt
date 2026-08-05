package com.example.cursotestingandroid.checkout.data.mapper

import com.example.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation

fun OrderConfirmationResponse.toDomain(): OrderConfirmation{
    return OrderConfirmation(
        orderId = orderId,
        etaMinutes = etaMinutes,
        total = total
    )
}
