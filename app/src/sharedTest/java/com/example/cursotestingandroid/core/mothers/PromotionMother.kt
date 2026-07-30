package com.example.cursotestingandroid.core.mothers

import com.example.cursotestingandroid.productlist.domain.model.ProductPromotion

object PromotionMother {
    fun percent(percent: Double = 25.0, discountedPrice: Double = 4.65) =
        ProductPromotion.Percent(percent = percent, discountedPrice = discountedPrice)

    fun buyXPayY(buy:Int = 3, pay:Int = 2) =
        ProductPromotion.BuyXPayY(buy = buy, pay = pay, label = "${buy}x${pay}")
}