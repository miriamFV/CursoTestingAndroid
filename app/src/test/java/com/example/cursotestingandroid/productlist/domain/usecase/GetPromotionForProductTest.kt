package com.example.cursotestingandroid.productlist.domain.usecase

import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.builders.promotion
import com.example.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.example.cursotestingandroid.productlist.domain.model.PromotionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPromotionForProductTest {
    private val useCase = GetPromotionForProduct()

    @Test
    fun givenNoPromotions_whenInvoke_thenReturnsNull() {
        // Given
        val product = product()

        // When
        val response = useCase(product, emptyList())

        // Then
        assertNull(response)
    }

    @Test
    fun givenPercentPromotion_whenInvoke_thenReturnsDiscountedPriceRoundedTo2Decimals() {
        // Given
        val productId = "productId-1"
        val promotionId = "promotionId-1"
        val product =
            product {
                withId(productId)
                withPrice(10.0)
            }
        val promotion =
            promotion {
                withId(promotionId)
                withType(PromotionType.PERCENT)
                withValue(15.0)
                withProductsIds(listOf(productId))
            }

        // When
        val response = useCase(product, listOf(promotion))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(8.50, response.discountedPrice, 0.001)
        assertEquals(15.0, response.percent, 0.001)
    }

    @Test
    fun givenBuyPayAndPercentPromotions_whenInvoke_thenPrioritizesBuyPayPromotion() {
        // Given
        val productId = "productId-1"
        val buyPayPromoId = "promotionId-1"
        val percentPromoId = "promotionId-2"
        val product =
            product {
                withId(productId)
                withPrice(10.0)
            }
        val buyPayPromotion =
            promotion {
                withId(buyPayPromoId)
                withType(PromotionType.BUY_X_PAY_Y)
                withValue(2.0)
                withBuyQuantity(3)
                withProductsIds(listOf(productId))
            }
        val percentPromotion =
            promotion {
                withId(percentPromoId)
                withType(PromotionType.PERCENT)
                withValue(15.0)
                withProductsIds(listOf(productId))
            }

        // When
        val response = useCase(product, listOf(percentPromotion, buyPayPromotion))

        // Then
        assertTrue(response is ProductPromotion.BuyXPayY)
        response as ProductPromotion.BuyXPayY
        assertEquals(2, response.pay)
        assertEquals(3, response.buy)
        assertEquals("3x2", response.label)
    }

    @Test
    fun givenMultiplePercentPromotions_whenInvoke_thenReturnHighestPercentPromotion() {
        // Given
        val productId = "productId-1"
        val percentPromoId1 = "promotionId-1"
        val percentPromoId2 = "promotionId-2"
        val product =
            product {
                withId(productId)
                withPrice(10.0)
            }

        val percentPromotionLow =
            promotion {
                withId(percentPromoId1)
                withType(PromotionType.PERCENT)
                withValue(15.0)
                withProductsIds(listOf(productId))
            }
        val percentPromotionHigh =
            promotion {
                withId(percentPromoId2)
                withType(PromotionType.PERCENT)
                withValue(30.0)
                withProductsIds(listOf(productId))
            }

        // When
        val response = useCase(product, listOf(percentPromotionLow, percentPromotionHigh))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(30.0, response.percent, 0.001)
        assertEquals(7.0, response.discountedPrice, 0.001)
    }

    @Test
    fun givenBuyPayPromotionWithoutBuyQuantity_whenInvoke_thenReturnNull() {
        // Given
        val productId = "productId-1"
        val percentPromotionId = "promotionId-1"
        val buyPayPromotionId = "promotionId-2"
        val product =
            product {
                withId(productId)
                withPrice(10.0)
            }
        val percentPromotion =
            promotion {
                withId(percentPromotionId)
                withType(PromotionType.PERCENT)
                withValue(15.0)
                withProductsIds(listOf(productId))
            }
        val brokenBuyPayPromotion =
            promotion {
                withId(buyPayPromotionId)
                withType(PromotionType.BUY_X_PAY_Y)
                withValue(2.0)
                withBuyQuantity(null)
                withProductsIds(listOf(productId))
            }

        // When
        val response = useCase(product, listOf(percentPromotion, brokenBuyPayPromotion))

        // Then
        assertNull(response)
    }
}
