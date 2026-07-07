package com.example.cursotestingandroid.cart.data.mapper

import com.example.cursotestingandroid.cart.data.local.database.entities.CartItemEntity
import com.example.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain() : CartItem{
    return CartItem(
        productId = productId,
        quantity = quantity
    )
}
fun CartItem.toEntity() : CartItemEntity{
    return CartItemEntity(
        productId = productId,
        quantity = quantity
    )
}