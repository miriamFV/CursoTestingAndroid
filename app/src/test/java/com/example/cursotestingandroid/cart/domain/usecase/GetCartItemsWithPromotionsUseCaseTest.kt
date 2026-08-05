package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.builders.promotion
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class GetCartItemsWithPromotionsUseCaseTest {
    private val clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }

    private fun useCase(
        cartRepository: FakeCartRepository = FakeCartRepository(),
        productRepository: FakeProductRepository = FakeProductRepository(),
        promotionRepository: FakePromotionRepository = FakePromotionRepository(),
        clock: FakeSystemClock = this.clock,
    ) = GetCartItemsWithPromotionsUseCase(
        cartRepository,
        productRepository,
        promotionRepository,
        GetPromotionForProduct(),
        clock,
    )

    @Test
    fun givenEmptyCart_whenInvoke_thenReturnEmptyList() =
        runTest {
            // Given
            val cart = FakeCartRepository().apply { setCartItems(emptyList()) }

            // When
            val result = (useCase(cartRepository = cart)()).first()

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun givenExistingCartItemWithActivePromotion_whenInvoke_thenReturnsItemWithPromotion() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                }
            val now = clock.now()
            val promotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(10))
                }

            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(2)
                }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakePromotionRepository =
                FakePromotionRepository().apply {
                    setPromotions(listOf(promotion))
                }

            // When
            val result = (useCase(fakeCartRepository, fakeProductRepository, fakePromotionRepository)()).first()

            // Then
            assertEquals(1, result.size)
            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun givenCartItemWithoutMatchingProduct_whenInvoke_thenSkipItem() =
        runTest {
            // Given
            val fakeCartRepository =
                FakeCartRepository().apply {
                    setCartItems(listOf(cartItem { withProductId("ghost-id") }))
                }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product { withId("other-id") })) }

            // When
            val result = (useCase(fakeCartRepository, fakeProductRepository)()).first()

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun givenPromotionEndingExactlyNow_whenInvoke_thenItMustBeIncluded() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                }
            val now = clock.now()
            val endingPromotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now)
                }

            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakePromotionRepository =
                FakePromotionRepository().apply {
                    setPromotions(listOf(endingPromotion))
                }

            // When
            val result = (useCase(fakeCartRepository, fakeProductRepository, fakePromotionRepository)()).first()

            // Then
            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun givenExpiredPromotion_whenInvoke_thenItemRemainsButWithoutPromotion() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                }
            val now = clock.now()
            val endPromotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.minusSeconds(1))
                }

            val fakeCartRepository =
                FakeCartRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakePromotionRepository =
                FakePromotionRepository().apply {
                    setPromotions(listOf(endPromotion))
                }

            // When
            val result = (useCase(fakeCartRepository, fakeProductRepository, fakePromotionRepository)()).first()

            // Then
            assertNull(result.first().item.promotion)
        }

    @Test
    fun givenActivePromotion_whenTimesAdvance_thenFlowEmitsUpdatedListWithoutPromotion() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                }
            val now = clock.now()
            val promotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.plusSeconds(5))
                }

            val fakeCartRepository =
                FakeCartRepository().apply { setCartItems(listOf(cartItem { withProductId(productId) })) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakePromotionRepository =
                FakePromotionRepository().apply {
                    setPromotions(listOf(promotion))
                }

            // When
            val myUseCase = useCase(fakeCartRepository, fakeProductRepository, fakePromotionRepository)()
            val firstEmition = myUseCase.first()
            assertNotNull(firstEmition.first().item.promotion)
            clock.advanceTime(6)

            // Then
            val secondEmition = myUseCase.first()
            assertNull(secondEmition.first().item.promotion)
        }
}
