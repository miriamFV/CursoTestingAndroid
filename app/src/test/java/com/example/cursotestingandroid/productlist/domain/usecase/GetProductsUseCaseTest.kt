package com.example.cursotestingandroid.productlist.domain.usecase

import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.builders.promotion
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.model.Product
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.model.Promotion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GetProductsUseCaseTest {

    private fun useCase(
        productRepository: FakeProductRepository = FakeProductRepository(),
        promotionRespository: FakePromotionRepository = FakePromotionRepository(),
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        clock: FakeSystemClock = FakeSystemClock()
    ) = GetProductsUseCase(
        productRepository,
        promotionRespository,
        GetPromotionForProduct(),
        settingsRepository,
        clock
    )

    @Test
    fun givenPromotionEndingNow_whenInvoke_thenItShouldBeIncluded() = runTest {
        //Given
        val now: Instant = Instant.parse("2026-04-03T10:00:00Z")
        val clock: FakeSystemClock = FakeSystemClock().apply { setTime(now) }

        val productId = "product-id"
        val product: Product = product {
            withId(productId)
        }

        val promotion: Promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now.minusSeconds(60))
            withEndTime(now)
        }

        val productRepository: FakeProductRepository =
            FakeProductRepository().apply { setProducts(listOf(product)) }
        val promoRepository: FakePromotionRepository =
            FakePromotionRepository().apply { setPromotions(listOf(promotion)) }

        //When
        val result: List<ProductWithPromotion> = (useCase(
            productRepository = productRepository,
            promotionRespository = promoRepository,
            clock = clock
        )()).first()

        //Then
        assertNotNull(result.first())
    }

    @Test
    fun givenActivePromotion_whenTimeAdvances_thenPromotionShouldNotBeLongerReturned() = runTest {
        //Given
        val now: Instant = Instant.parse("2026-04-03T10:00:00Z")
        val clock: FakeSystemClock = FakeSystemClock().apply { setTime(now) }

        val productId = "product-id"
        val product: Product = product {
            withId(productId)
        }

        val promotion: Promotion = promotion {
            withProductsIds(listOf(productId))
            withStartTime(now)
            withEndTime(now.plusSeconds(5))
        }

        val productRepository: FakeProductRepository =
            FakeProductRepository().apply { setProducts(listOf(product)) }
        val promoRepository: FakePromotionRepository =
            FakePromotionRepository().apply { setPromotions(listOf(promotion)) }


        //When
        val firstResult = (useCase(
            productRepository = productRepository,
            promotionRespository = promoRepository,
            clock = clock
        )()).first()

        clock.advanceTime(6)

        val secondResult = (useCase(
            productRepository = productRepository,
            promotionRespository = promoRepository,
            clock = clock
        )()).first()

        //Then
        assertNotNull(firstResult.first().promotion)
        assertNull(secondResult.first().promotion)
    }

    @Test
    fun givenInStockOnlyEnabled_whenProductsGoesOutOfStock_thenItShouldBeFiltered() = runTest {
        //Given
        val productId = "product-id"
        val product: Product = product {
            withId(productId)
            withStock(0)
        }
        val settingsRepository = FakeSettingsRepository().apply {
            setInStockOnly(true)
        }
        val productRepository: FakeProductRepository =
            FakeProductRepository().apply { setProducts(listOf(product)) }

        val myUseCAse = useCase(productRepository = productRepository, settingsRepository = settingsRepository)

        //When
        val result = myUseCAse().first()

        //Then
        assertTrue(result.isEmpty())
    }
}