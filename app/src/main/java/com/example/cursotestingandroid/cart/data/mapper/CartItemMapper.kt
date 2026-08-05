package com.example.cursotestingandroid.cart.data.mapper

import com.example.cursotestingandroid.cart.data.local.database.entities.CartItemEntity
import com.example.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain(): CartItem =
    CartItem(
        productId = productId,
        quantity = quantity,
    )

fun CartItem.toEntity(): CartItemEntity =
    CartItemEntity(
        productId = productId,
        quantity = quantity,
    )
