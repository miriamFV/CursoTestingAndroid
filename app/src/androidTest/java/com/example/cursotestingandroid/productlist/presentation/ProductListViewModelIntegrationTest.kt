package com.example.cursotestingandroid.productlist.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.mockwebserver.MarketApiDispatcher
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.core.utils.asAsset
import com.example.cursotestingandroid.productlist.data.repository.SettingsRepositoryImpl
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductListViewModelIntegrationTest {
    private companion object {
        const val EXPECTED_PRODUCT_SIZE = 3
        const val DAIRY_CATEGORY = "Lácteos"
    }

    @get:Rule(order = 0)
    val mockWebServerRule = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var getProductsUseCase: GetProductsUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() =
        runTest {
            mockWebServerRule.server.dispatcher =
                MarketApiDispatcher(productJson = "product_list_default.json".asAsset())
            hiltAndroidRule.inject()
            (settingsRepository as? SettingsRepositoryImpl)?.clear()
            productRepository.refreshProduct()
            promotionRepository.refreshPromotions()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenSuccessfullApi_whenViewModelLoads_thenShowsProducts() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)
            viewModel.uiState.test {
                val result =
                    awaitSuccessMatching {
                        it.products.size == EXPECTED_PRODUCT_SIZE
                    } // Wait until our 3 products in product_list_default.json had been loaded
                assertTrue(result.products.isNotEmpty())
                assertTrue(result.products.size == 3)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenDairyCategorySelected_whenFiltering_thenOnlyDairyProductsAreShown() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)
            viewModel.uiState.test {
                awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE } // Wait until our 3 products in our json had been loaded
                viewModel.setCategory(DAIRY_CATEGORY)
                val result =
                    awaitSuccessMatching { state ->
                        state.selectedCategory == DAIRY_CATEGORY &&
                            state.products.isNotEmpty() &&
                            state.products.all { it.product.category == DAIRY_CATEGORY }
                    }
                assertTrue(result.products.size == 2)
                assertTrue(result.products.all { it.product.category == DAIRY_CATEGORY })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenProductsLoaded_whenSortingByPriceAsc_thenListIsCorrectlyOrdered() =
        runTest {
            val viewModel = ProductListViewModel(getProductsUseCase, settingsRepository)
            viewModel.uiState.test {
                awaitSuccessMatching { it.products.size == EXPECTED_PRODUCT_SIZE } // Wait until our 3 products in our json had been loaded
                viewModel.setSortOption(SortOption.PRICE_ASC)
                val result =
                    awaitSuccessMatching { state ->
                        state.sortOption == SortOption.PRICE_ASC &&
                            state.products.map { it.product.price } == state.products.map { it.product.price }.sorted()
                    }
                assertEquals(
                    10.0,
                    result.products
                        .first()
                        .product.price,
                )
                assertEquals(listOf(10.0, 15.0, 20.0), result.products.map { it.product.price })
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun ReceiveTurbine<ProductListUiState>.awaitSuccessMatching(
        predicate: (ProductListUiState.Success) -> Boolean,
    ): ProductListUiState.Success {
        while (true) {
            when (val item = awaitItem()) {
                is ProductListUiState.Success -> if (predicate(item)) return item
                is ProductListUiState.Error -> error("Unexpected error: ${item.message}")
                is ProductListUiState.Loading -> Unit
            }
        }
    }
}
