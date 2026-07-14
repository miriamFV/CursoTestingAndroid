package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.builders.promotion
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.model.PromotionType
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetCartSummaryUseCaseTest {

    private lateinit var clock: FakeSystemClock
    private lateinit var fakeCartRepo: FakeCartRepository
    private lateinit var fakeProductRepo: FakeProductRepository
    private lateinit var fakePromotionRepo: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        fakeCartRepo = FakeCartRepository()
        fakeProductRepo = FakeProductRepository()
        fakePromotionRepo = FakePromotionRepository()
    }

    private fun useCase() =
        GetCartSummaryUseCase(
            fakeCartRepo,
            fakeProductRepo,
            fakePromotionRepo,
            GetPromotionForProduct(),
            clock
        )

    @Test
    fun givenPercentPromotion_whenInvoke_thenCalculateCorrectly() = runTest {
        //Given
        val productId = "productId-1"
        val product = product { withId(productId);withPrice(100.0) }
        val cartItem = cartItem { withProductId(productId); withQuantity(2) }
        val percentPromoId = "promotionId-1"
        val percentPromotion = promotion {
            withId(percentPromoId)
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withProductsIds(listOf(productId))
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }

        fakeCartRepo.setCartItems(listOf(cartItem))
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(percentPromotion))

        //When
        val summary = (useCase()()).first()

        //Then
        assertEquals(200.0, summary.subtotal)
        assertEquals(20.0, summary.discountTotal)
        assertEquals(180.0, summary.finalTotal)
    }

    @Test
    fun given3ItemsIn2x1Promotion_whenInvoke_thenOnlyDiscounts1Unit() = runTest {
        //Given
        val productId = "productId-1"
        val product = product { withId(productId);withPrice(100.0) }
        val cartItem = cartItem { withProductId(productId); withQuantity(3) }
        val buyPayPromoId = "promotionId-1"
        val buyPayPromotion = promotion {
            withId(buyPayPromoId)
            withType(PromotionType.BUY_X_PAY_Y)
            withBuyQuantity(2); withValue(1.0)
            withProductsIds(listOf(productId))
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }

        fakeCartRepo.setCartItems(listOf(cartItem))
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(buyPayPromotion))

        //When
        val summary = (useCase()()).first()

        //Then
        assertEquals(300.0, summary.subtotal)
        assertEquals(100.0, summary.discountTotal)
        assertEquals(200.0, summary.finalTotal)
    }

    @Test
    fun givenMultipleProductsWithDifferentPromotions_whenInvoke_thenSumsAllCorrectly() = runTest {
        //Given
        val now = clock.now()
        val product1Id = "productId-1"
        val product2Id = "productId-2"
        val product1 = product { withId(product1Id); withPrice(100.0) } //Con promo
        val product2 = product { withId(product2Id); withPrice(50.0) } //Sin promo

        val percentPromotion = promotion {
            withId("promotionId-1")
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withProductsIds(listOf(product1Id))
            withStartTime(now.minusSeconds(10)); withEndTime(now.plusSeconds(10))
        }

        val cart = listOf(
            cartItem { withProductId(product1Id); withQuantity(1) },
            cartItem { withProductId(product2Id); withQuantity(1) }
            )

        fakeCartRepo.setCartItems(cart)
        fakeProductRepo.setProducts(listOf(product1, product2))
        fakePromotionRepo.setPromotions(listOf(percentPromotion))

        //When
        val summary = (useCase()()).first()

        //Then
        assertEquals(150.0, summary.subtotal)
        assertEquals(10.0, summary.discountTotal)
        assertEquals(140.0, summary.finalTotal)
    }

    @Test
    fun givenExpiredPromotion_whenInvoke_thenDiscountIsZero() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId-1"
        val product = product { withId(productId);withPrice(100.0) }
        val cartItem = cartItem { withProductId(productId); withQuantity(1) }
        val percentPromoId = "promotionId-1"
        val percentPromotion = promotion {
            withId(percentPromoId)
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(50)); withEndTime(now.minusSeconds(10))
        }

        fakeCartRepo.setCartItems(listOf(cartItem))
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(percentPromotion))

        //When
        val summary = (useCase()()).first()

        //Then
        assertEquals(100.0, summary.subtotal)
        assertEquals(0.0, summary.discountTotal)
        assertEquals(100.0, summary.finalTotal)
    }

    @Test
    fun givenActivePromotionWhenTimeAdvances_whenInvoke_thenSummaryUpdateAutomatically() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId-1"
        val product = product { withId(productId);withPrice(100.0) }
        val cartItem = cartItem { withProductId(productId); withQuantity(1) }
        val percentPromoId = "promotionId-1"
        val percentPromotion = promotion {
            withId(percentPromoId)
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(50)); withEndTime(now.plusSeconds(5))
        }

        fakeCartRepo.setCartItems(listOf(cartItem))
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(percentPromotion))

        //When
        val summaryBeforePromotionEnds = (useCase()()).first()
        clock.advanceTime(6)
        val summaryAfterPromotionEnds = (useCase()()).first()

        //Then
        assertEquals(100.0, summaryBeforePromotionEnds.subtotal)
        assertEquals(10.0, summaryBeforePromotionEnds.discountTotal)
        assertEquals(90.0, summaryBeforePromotionEnds.finalTotal)

        assertEquals(100.0, summaryAfterPromotionEnds.subtotal)
        assertEquals(0.0, summaryAfterPromotionEnds.discountTotal)
        assertEquals(100.0, summaryAfterPromotionEnds.finalTotal)
    }

}