package com.example.cursotestingandroid.productdetail.domain.usecase

import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.builders.promotion
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetProductDetailWithPromotionUseCaseTest {

    private lateinit var clock: FakeSystemClock
    private lateinit var fakeProductRepo: FakeProductRepository
    private lateinit var fakePromotionRepo: FakePromotionRepository

    @Before
    fun setUp() {
        clock = FakeSystemClock().apply { setTime(Instant.parse("2026-04-03T10:00:00Z")) }
        fakeProductRepo = FakeProductRepository()
        fakePromotionRepo = FakePromotionRepository()
    }

    private fun useCase() =
        GetProductDetailWithPromotionUseCase(
            fakeProductRepo,
            fakePromotionRepo,
            GetPromotionForProduct(),
            clock
        )

    @Test
    fun givenActivePromotion_whenInvoke_thenReturnsProductWithPromotion() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId"
        val product = product { withId(productId); withName("Pan") }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(10)); withEndTime(now.plusSeconds(10))
        }
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(promotion))

        //When
        val result = useCase()(productId).first()

        //Then
        assertNotNull(result)
        assertEquals(productId, result?.product?.id)
        assertNotNull(result?.promotion)
    }

    @Test
    fun givenExpiredPromotion_whenInvoke_thenReturnsProductWithoutPromotion() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId"
        val product = product { withId(productId) }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(20)); withEndTime(now.minusSeconds(10))
        }
        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(promotion))

        //When
        val result = useCase().invoke(productId).first()

        //Then
        assertNotNull(result)
        assertNotNull(result?.product)
        assertNull(result?.promotion)
    }

    @Test
    fun givenNonExistingProductId_whenInvoke_thenReturnsNull() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId"
        val wrongProductId = "wrongProductId"
        val product = product { withId(productId) }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(20)); withEndTime(now.minusSeconds(10))
        }

        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(promotion))

        //When
        val result = useCase().invoke(wrongProductId).first()

        //Then
        assertNull(result)
    }


    @Test
    fun givenActivePromotion_whenTimeAdvances_thenProductPromotionBecomesNull() = runTest {
        //Given
        val now = clock.now()
        val productId = "productId"
        val product = product { withId(productId) }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(20)); withEndTime(now.plusSeconds(5))
        }

        fakeProductRepo.setProducts(listOf(product))
        fakePromotionRepo.setPromotions(listOf(promotion))

        //When
        val resultBeforeAdvanceTime = useCase()(productId).first()
        clock.advanceTime(6)
        val resultAfterAdvanceTime = useCase()(productId).first()

        //Then
        assertNotNull(resultBeforeAdvanceTime)
        assertEquals(productId, resultBeforeAdvanceTime?.product?.id)
        assertNotNull(resultBeforeAdvanceTime?.promotion)

        assertNotNull(resultAfterAdvanceTime)
        assertNotNull(resultAfterAdvanceTime?.product)
        assertNull(resultAfterAdvanceTime?.promotion)

    }

}