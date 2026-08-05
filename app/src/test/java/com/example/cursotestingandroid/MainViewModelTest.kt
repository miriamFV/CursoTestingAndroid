package com.example.cursotestingandroid

import app.cash.turbine.test
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.core.fakes.FakeSettingsRepository
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(fakeSettingsRepository: SettingsRepository = FakeSettingsRepository()): MainViewModel =
        MainViewModel(settingsRepository = fakeSettingsRepository)

    @Test
    fun givenDefaultRepository_whenInitialized_thenEmitsSystemThemeMode() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettingsRepository = FakeSettingsRepository()

            // When
            val viewModel = createViewModel(fakeSettingsRepository)

            // Then
            viewModel.themeMode.test {
                val state = awaitItem()
                assertTrue(state is ThemeMode.SYSTEM)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenRepositoryWithDarkMode_whenInitialized_thenEmitsDarkThemeMode() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val fakeSettingsRepository =
                FakeSettingsRepository().apply { setThemeMode(ThemeMode.DARK) }

            // When
            val viewModel = createViewModel(fakeSettingsRepository)

            // Then
            viewModel.themeMode.test {
                val state = awaitItem()
                assertTrue(state is ThemeMode.DARK)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
