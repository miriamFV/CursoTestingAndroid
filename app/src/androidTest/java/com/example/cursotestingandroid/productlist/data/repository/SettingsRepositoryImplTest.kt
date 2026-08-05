package com.example.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServerRule = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() =
        runTest {
            hiltAndroidRule.inject()
            (settingsRepository as? SettingsRepositoryImpl)?.clear()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenNoDataSaved_whenInStockOnlyIsRead_thenReturnsDefaultFalse() =
        runTest {
            // Given
            // When
            val defaultInStockOnly = settingsRepository.inStockOnly.first()
            // Then
            assertFalse(defaultInStockOnly)
        }

    @Test
    fun givenNoDataSaved_whenFiltersVisibleIsRead_thenReturnsDefaultTrue() =
        runTest {
            // Given
            // When
            val defaultFiltersVisible = settingsRepository.filtersVisible.first()
            // Then
            assertTrue(defaultFiltersVisible)
        }

    @Test
    fun givenNoDataSaved_whenSelectedCategoryIsRead_thenReturnsDefaultNull() =
        runTest {
            // Given
            // When
            val defaultSelectedCategory = settingsRepository.selectedCategory.first()
            // Then
            assertNull(defaultSelectedCategory)
        }

    @Test
    fun givenNoDataSaved_whenThemeModeIsRead_thenReturnsDefaultThemeModeSystem() =
        runTest {
            // Given
            // When
            val defaultThemeMode = settingsRepository.themeMode.first()
            // Then
            assertEquals(ThemeMode.SYSTEM, defaultThemeMode)
        }

    @Test
    fun givenNoDataSaved_whenSortOptionIsRead_thenReturnsDefaultSortOptionNone() =
        runTest {
            // Given
            // When
            val defaultSortOption = settingsRepository.sortOption.first()
            // Then
            assertEquals(SortOption.NONE, defaultSortOption)
        }

    @Test
    fun givenRepository_whenSetFiltersVisibleToFalse_thenPersistValue() =
        runTest {
            // Given
            // When
            settingsRepository.setFiltersVisible(false)
            // Then
            val defaultFiltersVisible = settingsRepository.filtersVisible.first()
            assertFalse(defaultFiltersVisible)
        }

    @Test
    fun givenMultipleSettingsChanges_whenReadAll_thenStateIsConsisteng() =
        runTest {
            // Given
            // When
            settingsRepository.setFiltersVisible(false)
            settingsRepository.setInStockOnly(true)
            settingsRepository.setThemeMode(ThemeMode.DARK)
            settingsRepository.setSortOption(SortOption.PRICE_ASC)
            settingsRepository.setSelectedCategory("bread")
            // Then
            assertFalse(settingsRepository.filtersVisible.first())
            assertTrue(settingsRepository.inStockOnly.first())
            assertEquals(ThemeMode.DARK, settingsRepository.themeMode.first())
            assertEquals(SortOption.PRICE_ASC, settingsRepository.sortOption.first())
            assertEquals("bread", settingsRepository.selectedCategory.first())
        }
}
