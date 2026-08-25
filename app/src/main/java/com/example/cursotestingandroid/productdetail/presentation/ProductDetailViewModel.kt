package com.example.cursotestingandroid.productdetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.domain.model.AppError.DatabaseError
import com.example.cursotestingandroid.core.domain.model.AppError.NetworkError
import com.example.cursotestingandroid.core.domain.model.AppError.NotFoundError
import com.example.cursotestingandroid.core.domain.model.AppError.UnknownError
import com.example.cursotestingandroid.core.domain.model.AppError.Validation
import com.example.cursotestingandroid.productdetail.domain.usecase.GetProductDetailWithPromotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel
    @Inject
    constructor(
        private val getProductDetailWithPromotionUseCase: GetProductDetailWithPromotionUseCase,
        private val addToCartUseCase: AddToCartUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState())
        val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

        private val _event = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
        val event: SharedFlow<ProductDetailEvent> = _event

        private var productJob: Job? = null

        fun loadProduct(productId: String) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            productJob?.cancel()
            productJob =
                getProductDetailWithPromotionUseCase(productId)
                    .onEach { product ->
                        _uiState.value = _uiState.value.copy(isLoading = false, item = product)
                    }.catch { e: Throwable ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (e is AppError) {
                            handleError(e)
                        } else {
                            handleError(UnknownError(e.message))
                        }
                    }.launchIn(viewModelScope)
        }

        fun addToCart() {
            val product =
                _uiState.value.item
                    ?.product
                    ?.id ?: return
            viewModelScope.launch {
                try {
                    addToCartUseCase(product)
                    _event.emit(ProductDetailEvent.SuccessAddToCart)
                } catch (e: AppError) {
                    handleError(e)
                } catch (e: Exception) {
                    handleError(UnknownError(e.message))
                }
            }
        }

        private suspend fun handleError(e: AppError) {
            val newEvent =
                when (e) {
                    NetworkError -> ProductDetailEvent.NetworkError
                    is Validation.InsufficientStock -> ProductDetailEvent.InsufficientStock
                    is UnknownError, DatabaseError, NotFoundError, Validation.QuantityMustBePositive ->
                        ProductDetailEvent.UnknownError
                }
            _event.emit(newEvent)
        }
    }
