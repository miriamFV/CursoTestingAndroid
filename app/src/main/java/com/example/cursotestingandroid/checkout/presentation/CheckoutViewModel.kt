package com.example.cursotestingandroid.checkout.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.example.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val placeOrderUseCase: PlaceOrderUseCase,
    getCartSummaryUseCase: GetCartSummaryUseCase
) : ViewModel() {

    private val formState = MutableStateFlow(CheckoutForm())
    private val submission = MutableStateFlow<Submission>(Submission.Idle)

    private val _event = MutableSharedFlow<CheckoutEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<CheckoutEvent> = _event

    val uiState: StateFlow<CheckoutUiState> = combine(
        getCartSummaryUseCase(), formState, submission
    ) { summary, form, submission ->
        when (submission) {
            is Submission.Success -> CheckoutUiState.Success(submission.confirmation)
            is Submission.Failed -> CheckoutUiState.Error(submission.message)
            Submission.Idle, Submission.Submitting -> {

                val errors = form.validate()
                val isCartEmpty = summary.subtotal <= 0.0
                val isSubmitting = submission == Submission.Submitting

                CheckoutUiState.Idle(
                    summary = summary,
                    form = form,
                    errors = errors,
                    isCartEmpty = isCartEmpty,
                    isSubmitting = isSubmitting,
                    canSubmit = !isCartEmpty && !isSubmitting && errors.isValid
                )
            }
        }
    }.catch { e: Throwable ->
        _event.emit(CheckoutEvent.ShowMessage(e.message.orEmpty()))
        emit(CheckoutUiState.Error(e.message.orEmpty()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState.Loading,
    )


    fun onRetry() {
        submission.value = Submission.Idle
    }

    fun onNameChanged(name: String) {
        formState.update { it.copy(name = name) }
    }

    fun onEmailChanged(email: String) {
        formState.update { it.copy(email = email) }
    }

    fun onAddressChanged(address: String) {
        formState.update { it.copy(address = address) }
    }

    fun onConfirm() {
        if (!formState.value.validate().isValid) return

        viewModelScope.launch {
            submission.value = Submission.Submitting
            placeOrderUseCase()
                .onSuccess {
                    submission.value = Submission.Success(it)
                    Log.v("Miriam", "Success $it")
                }
                .onFailure { e ->
                    submission.value = Submission.Failed(e.message.orEmpty())
                    _event.emit(CheckoutEvent.ShowMessage(e.message.orEmpty()))
                }
        }
    }

}
