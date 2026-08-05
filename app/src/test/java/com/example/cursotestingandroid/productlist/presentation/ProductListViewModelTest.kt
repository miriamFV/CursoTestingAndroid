package com.example.cursotestingandroid.productlist.presentation

import app.cash.turbine.test
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.core.stubs.FailingProductRepositoryStub
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeProductRepository: ProductRepository = FakeProductRepository(),
        fakePromotionRepository: FakePromotionRepository = FakePromotionRepository(),
        fakeSettingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock(),
    ): ProductListViewModel {
        val getProductUseCase =
            GetProductsUseCase(
                fakeProductRepository,
                fakePromotionRepository,
                GetPromotionForProduct(),
                fakeSettingsRepository,
                fakeClock,
            )

        return ProductListViewModel(
            getProductsUseCase = getProductUseCase,
            settingsRepository = fakeSettingsRepository,
        )
    }

    @Test
    fun givenProducts_whenInitialized_thenEmitsSuccessState() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val productId = "productId"
            val product = product { withId(productId) }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }

            // When
            val viewModel = createViewModel(fakeProductRepository = fakeProductRepository)

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is ProductListUiState.Success)
                assertEquals(1, (state as ProductListUiState.Success).products.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenSelectedCategory_whenSetCategory_thenFiltersProducts() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val product1 =
                product {
                    withId("p1")
                    withCategory("carne")
                }
            val product2 =
                product {
                    withId("p2")
                    withCategory("pasta")
                }

            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product1, product2)) }
            val fakeSettingsRepository =
                FakeSettingsRepository().apply { setSelectedCategory("carne") }

            // When
            val viewModel =
                createViewModel(
                    fakeProductRepository = fakeProductRepository,
                    fakeSettingsRepository = fakeSettingsRepository,
                )

            // Then
            viewModel.uiState.test {
                val initialState = awaitItem()

                assertTrue(initialState is ProductListUiState.Success)
                assertEquals(1, (initialState as ProductListUiState.Success).products.size)
                assertEquals("carne", initialState.selectedCategory)

                viewModel.setCategory("pasta")

                val updatedState = awaitItem()
                assertEquals(1, (updatedState as ProductListUiState.Success).products.size)
                assertEquals("pasta", updatedState.selectedCategory)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenPriceAscSortOption_whenSetSortOption_thenSortsByEffectivePrice() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val product1 =
                product {
                    withId("p1")
                    withPrice(10.0)
                }
            val product2 =
                product {
                    withId("p2")
                    withPrice(5.0)
                }
            val product3 =
                product {
                    withId("p3")
                    withPrice(15.0)
                }

            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product1, product2, product3)) }

            val viewModel =
                createViewModel(
                    fakeProductRepository = fakeProductRepository,
                )

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.setSortOption(SortOption.PRICE_ASC)

                // Then
                val state = awaitItem() as ProductListUiState.Success
                val actualProductList = state.products.map { it.product }
                val listWithExpectedOrder = fakeProductRepository.getProducts().first().sortedBy { it.price }

                assertEquals(listWithExpectedOrder, actualProductList)
                assertEquals(SortOption.PRICE_ASC, state.sortOption)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenRepositoryError_whenLoadingProducts_thenEmitErrorState() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val failingProductRepositoryStub = FailingProductRepositoryStub(Exception("Prueba test"))

            // When
            val viewModel =
                createViewModel(
                    fakeProductRepository = failingProductRepositoryStub,
                )

            viewModel.uiState.test {
                val state = awaitItem()

                // Then
                assertTrue(state is ProductListUiState.Error)
                assertEquals("Prueba test", (state as ProductListUiState.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
