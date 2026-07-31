package com.example.cursotestingandroid.productlist.data.remote

import com.example.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import com.example.cursotestingandroid.productlist.data.remote.response.PromotionsResponse
import retrofit2.http.GET

interface MarketApiService {
    @GET("data/products.json")
    suspend fun getProducts(): ProductsResponse

    @GET("data/promotions.json")
    suspend fun getPromotions(): PromotionsResponse
}
