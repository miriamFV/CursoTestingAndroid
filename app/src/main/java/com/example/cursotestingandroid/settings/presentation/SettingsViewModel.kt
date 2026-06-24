package com.example.cursotestingandroid.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init{
        loadSettings()
    }

    private fun loadSettings() {
        combine(
            settingsRepository.inStockOnly, settingsRepository.themeMode
        ) { inStockOnly, themeMode ->
            _uiState.value = SettingsUiState(inStockOnly, themeMode)
        }.launchIn(viewModelScope)
    }

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