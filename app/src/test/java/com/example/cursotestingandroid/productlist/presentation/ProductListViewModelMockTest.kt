package com.example.cursotestingandroid.productlist.presentation

import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProductListViewModelMockTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true){
        every { selectedCategory } returns flowOf(null)
        every { sortOption } returns flowOf(SortOption.NONE)
        every { inStockOnly } returns flowOf(false)
        every { filtersVisible } returns flowOf(true)
    }

    private fun createViewModel(
        fakeProductRepository: ProductRepository = FakeProductRepository(),
        fakePromotionRepository: FakePromotionRepository = FakePromotionRepository(),
        fakeSettingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        fakeClock: FakeSystemClock = FakeSystemClock()
    ): ProductListViewModel {

        val getProductUseCase = GetProductsUseCase(
            fakeProductRepository,
            fakePromotionRepository,
            GetPromotionForProduct(),
            fakeSettingsRepository, //fake repository to make use case as realistic as possible
            fakeClock
        )
        return ProductListViewModel(
            getProductsUseCase = getProductUseCase,
            settingsRepository = settingsRepository //mocked repository for the setters methods
        )
    }

    @Test
    fun givenCategory_whenSetCategory_thenDelegatesToSettingsRepository() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val viewModel = createViewModel()
            val category = "carne"

            //When
            viewModel.setCategory(category)

            //Then
            coVerify(exactly = 1){ settingsRepository.setSelectedCategory(category)}
        }

    @Test
    fun givenSortOption_whenSetSortOption_thenDelegatesToSettingsRepository() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val viewModel = createViewModel()
            val sortOption = SortOption.PRICE_ASC

            //When
            viewModel.setSortOption(sortOption)

            //Then
            coVerify(exactly = 1){ settingsRepository.setSortOption(sortOption)}
        }


    @Test
    fun givenFilterVisible_whenSetFilterVisible_thenDelegatesToSettingsRepository() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val viewModel = createViewModel()
            val filterVisible = true

            //When
            viewModel.setFiltersVisible(filterVisible)

            //Then
            coVerify(exactly = 1){ settingsRepository.setFiltersVisible(filterVisible)}
        }

}