package com.example.cursotestingandroid.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_CONFIRM_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_FORM_EMAIL_FIELD
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_LOADING
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_ORDER_CONFIRMATION
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_RETRY_BUTTON

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CheckoutEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CheckoutContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = { viewModel.onExit { onBack() } },
        onRetry = { viewModel.onRetry() },
        onNameChanged = { viewModel.onNameChanged(it) },
        onEmailChanged = { viewModel.onEmailChanged(it) },
        onAddressChanged = { viewModel.onAddressChanged(it) },
        onConfirm = { viewModel.onConfirm() },
    )
}

@Composable
fun CheckoutContent(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MarketTopAppBar(title = stringResource(R.string.checkout_screen_top_app_bar_title)) {
                onBack()
            }
        },
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (uiState) {
                CheckoutUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.testTag(CHECKOUT_LOADING))
                }

                is CheckoutUiState.Error -> CheckoutContentError(errorMessage = uiState.message, onRetry = onRetry)
                is CheckoutUiState.Idle ->
                    CheckoutContentIdle(
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        onEmailChanged = onEmailChanged,
                        onAddressChanged = onAddressChanged,
                        onConfirm = onConfirm,
                    )

                is CheckoutUiState.Success ->
                    CheckoutContentSuccess(
                        orderId = uiState.confirmation.orderId,
                        estimatedTime = uiState.confirmation.etaMinutes.toString(),
                        total = uiState.confirmation.total.toString(),
                    )
            }
        }
    }
}

@Composable
fun CheckoutContentError(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(errorMessage)
        Button(
            modifier = Modifier.testTag(CHECKOUT_RETRY_BUTTON),
            onClick = { onRetry() },
        ) {
            Text(stringResource(R.string.checkout_screen_button_retry))
        }
    }
}

@Composable
fun CheckoutContentSuccess(
    orderId: String,
    estimatedTime: String,
    total: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag(CHECKOUT_ORDER_CONFIRMATION),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.checkout_screen_order_confirmed, orderId))
        Text(stringResource(R.string.checkout_screen_order_estimated_time, estimatedTime))
        Text(stringResource(R.string.checkout_screen_order_price, total))
    }
}

@Composable
fun CheckoutContentIdle(
    uiState: CheckoutUiState.Idle,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.checkout_screen_order_total, uiState.summary.finalTotal),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = uiState.form.name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.checkout_screen_form_name)) },
            isError = uiState.errors.nameError != null,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.form.address,
            onValueChange = onAddressChanged,
            label = { Text(stringResource(R.string.checkout_screen_form_address)) },
            isError = uiState.errors.addressError != null,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.form.email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(R.string.checkout_screen_form_email)) },
            isError = uiState.errors.emailError != null,
            modifier = Modifier.fillMaxWidth().testTag(CHECKOUT_FORM_EMAIL_FIELD),
        )

        if (uiState.isCartEmpty) {
            Text(
                stringResource(R.string.checkout_screen_form_empty_cart),
            )
        }

        Button(
            onClick = onConfirm,
            enabled = uiState.canSubmit,
            modifier = Modifier.fillMaxWidth().testTag(CHECKOUT_CONFIRM_BUTTON),
        ) {
            Text(
                text =
                    if (uiState.isSubmitting) {
                        stringResource(R.string.checkout_screen_form_processing_payment)
                    } else {
                        stringResource(R.string.checkout_screen_form_confirm_order)
                    },
            )
        }
    }
}
