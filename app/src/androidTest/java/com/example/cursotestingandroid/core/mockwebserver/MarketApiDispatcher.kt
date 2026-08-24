package com.example.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MarketApiDispatcher(
    private val productJson: String,
    private val promoJson: String = """{"promotions":[]}""",
    private val orderJson: String = """{"orderId": "ORD-1001","etaMinutes": 130,"total": 0.0}""",
    private val productCode: Int = 200,
    private val promoCode: Int = 200,
    private val orderCode: Int = 200,
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse =
        when {
            request.path?.contains("promotions.json") == true ->
                MockResponse()
                    .setBody(promoJson)
                    .setResponseCode(promoCode)
            request.path?.contains("products.json") == true ->
                MockResponse()
                    .setBody(productJson)
                    .setResponseCode(productCode)
            request.path?.contains("order_confirmation.json") == true ->
                MockResponse()
                    .setBody(orderJson)
                    .setResponseCode(orderCode)
            else -> MockResponse().setResponseCode(404)
        }
}
