package com.example.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            combine(
                settingsRepository.inStockOnly,
                settingsRepository.themeMode,
            ) { inStockOnly, themeMode ->
                SettingsUiState(inStockOnly, themeMode)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState(),
            )

        fun setInStockOnly(inStockOnly: Boolean) {
            viewModelScope.launch {
                settingsRepository.setInStockOnly(inStockOnly)
            }
        }

        fun setThemeMode(themeMode: ThemeMode) {
            viewModelScope.launch {
                settingsRepository.setThemeMode(themeMode)
            }
        }
    }
