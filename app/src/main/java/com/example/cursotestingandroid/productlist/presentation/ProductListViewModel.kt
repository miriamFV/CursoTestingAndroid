package com.example.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _filtersVisible = MutableStateFlow<Boolean>(true)
    val filtersVisible: StateFlow<Boolean> = _filtersVisible.asStateFlow()

    private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProductListEvent> = _events.asSharedFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        _uiState.value = ProductListUiState.Loading
        getProductsUseCase().onEach { products: List<ProductWithPromotion> ->
            val categories = products.map { it.product.category }.distinct().sorted()
            _uiState.value = ProductListUiState.Success(
                products = products,
                categories = categories,
                selectedCategory = null, //TODO sacar de otro repo
                sortOption = SortOption.NONE
            )
        }.catch { e: Throwable ->
            _uiState.value = ProductListUiState.Error(e.message.orEmpty())
        }.launchIn(viewModelScope)
    }

    fun setCategory(it: String?) {
        viewModelScope.launch {
            //TODO: guardar en DataStore settingsRepository
        }
    }

    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            //TODO: guardar en DataStore settingsRepository
        }
    }

    fun setFiltersVisible(showFilters: Boolean) {
        _filtersVisible.value = showFilters
    }
}