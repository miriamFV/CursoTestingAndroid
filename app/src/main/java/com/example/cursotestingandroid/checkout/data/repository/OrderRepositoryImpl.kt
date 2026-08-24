package com.example.cursotestingandroid.checkout.data.repository

import com.example.cursotestingandroid.checkout.data.mapper.toDomain
import com.example.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.example.cursotestingandroid.productlist.data.remote.RemoteDataSource
import javax.inject.Inject

class OrderRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteDataSource,
    ) : OrderRepository {
        override suspend fun placeOrder(): OrderConfirmation = remoteDataSource.placeOrder().getOrThrow().toDomain()
    }
