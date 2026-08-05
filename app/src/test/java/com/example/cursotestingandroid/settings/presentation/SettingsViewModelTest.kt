package com.example.cursotestingandroid.settings.presentation

import app.cash.turbine.test
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.core.fakes.FakeSettingsRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenRepositoryWithValues_whenViewModelIsInitialized_thenUiStateIsUpdated() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val settingsRepository =
                FakeSettingsRepository().apply {
                    setInStockOnly(true)
                }

            // When
            val viewModel = SettingsViewModel(settingsRepository)

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.inStockOnly)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenViewModel_whenThemeModeIsChanged_thenUiStateAndRepositoryAreUpdated() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(fakeSettingsRepository)

            viewModel.uiState.test {
                awaitItem() // We’ll wait in case something hasn’t loaded yet
                // When
                val newThemeMode = ThemeMode.DARK
                viewModel.setThemeMode(newThemeMode)

                // Then
                val state = awaitItem()
                assertEquals(newThemeMode, state.themeMode)
                assertEquals(newThemeMode, fakeSettingsRepository.themeMode.first())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenViewModel_whenInStockOnlyIsChanged_thenUiStateAndRepositoryAreUpdated() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(fakeSettingsRepository)

            viewModel.uiState.test {
                awaitItem() // We’ll wait in case something hasn’t loaded yet
                // When
                viewModel.setInStockOnly(true)

                // Then
                val state = awaitItem()
                assertTrue(state.inStockOnly)
                assertTrue(fakeSettingsRepository.inStockOnly.first())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenViewModel_whenRepositoryChangeExternally_thenUiStateUpdateAutomatically() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettingsRepository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(fakeSettingsRepository)

            viewModel.uiState.test {
                awaitItem() // We’ll wait in case something hasn’t loaded yet

                // When
                fakeSettingsRepository.setInStockOnly(true)

                // Then
                val state = awaitItem()
                assertTrue(state.inStockOnly)
                assertTrue(fakeSettingsRepository.inStockOnly.first())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
