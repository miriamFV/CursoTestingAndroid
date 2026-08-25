package com.example.cursotestingandroid.productlist.domain.repository

import com.example.cursotestingandroid.productlist.domain.model.Promotion
import kotlinx.coroutines.flow.Flow

interface PromotionRepository {
    fun getActivePromotions(): Flow<List<Promotion>>

    suspend fun refreshPromotions()
}
