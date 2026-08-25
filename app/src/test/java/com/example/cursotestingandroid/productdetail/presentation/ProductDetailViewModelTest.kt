package com.example.cursotestingandroid.productdetail.presentation

import app.cash.turbine.test
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productdetail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeProductRepository: ProductRepository = FakeProductRepository(),
        fakePromotionRepository: PromotionRepository = FakePromotionRepository(),
        fakeCartRepository: CartRepository = FakeCartRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock(),
    ): ProductDetailViewModel {
        val getProductDetailWithPromotionUseCase =
            GetProductDetailWithPromotionUseCase(
                fakeProductRepository,
                fakePromotionRepository,
                GetPromotionForProduct(),
                fakeClock,
            )
        val addToCartUseCase =
            AddToCartUseCase(
                fakeCartRepository,
                fakeProductRepository,
            )

        return ProductDetailViewModel(
            getProductDetailWithPromotionUseCase,
            addToCartUseCase,
        )
    }

    @Test
    fun givenValidProductId_whenLoadProduct_thenEmitsItem() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "productId"
            val product =
                product {
                    withId(productId)
                    withName("leche")
                }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val viewModel = createViewModel(fakeProductRepository = fakeProductRepository)

            viewModel.uiState.test {
                awaitItem()
                // When
                viewModel.loadProduct(productId)
                // Then
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertNotNull(state.item)
                assertEquals(productId, state.item?.product?.id)
                assertEquals("leche", state.item?.product?.name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenMissingProductId_whenLoadProduct_thenEndsWithItemNull() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "productId"
            val product =
                product {
                    withId(productId)
                    withName("leche")
                }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val viewModel = createViewModel(fakeProductRepository = fakeProductRepository)

            viewModel.uiState.test {
                awaitItem()
                // When
                val missingProductId = "pId"
                viewModel.loadProduct(missingProductId)
                // Then
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertNull(state.item)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenLoadProduct_whenAddToCartSucceeds_thenEmitSuccessEvent() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "productId"
            val product =
                product {
                    withId(productId)
                    withName("leche")
                    withStock(10)
                }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val viewModel = createViewModel(fakeProductRepository = fakeProductRepository)

            viewModel.loadProduct(productId)

            viewModel.event.test {
                // When
                viewModel.addToCart()

                // Then
                val result = awaitItem()
                assertEquals(ProductDetailEvent.SuccessAddToCart, result)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenLoadedProductWithoutStock_whenAddToCart_thenEmitsInsufficientStockError() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "productId"
            val product =
                product {
                    withId(productId)
                    withName("leche")
                    withStock(0)
                }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            val viewModel = createViewModel(fakeProductRepository = fakeProductRepository)

            viewModel.loadProduct(productId)

            viewModel.event.test {
                // When
                viewModel.addToCart()

                // Then
                val result = awaitItem()
                assertEquals(ProductDetailEvent.InsufficientStock, result)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
