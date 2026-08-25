package com.example.cursotestingandroid.checkout.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.cursotestingandroid.core.builders.checkoutErrorState
import com.example.cursotestingandroid.core.builders.checkoutIdleState
import com.example.cursotestingandroid.core.builders.checkoutSuccessState
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_CONFIRM_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_FORM_EMAIL_FIELD
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_LOADING
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_ORDER_CONFIRMATION
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CHECKOUT_RETRY_BUTTON
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests de UI (Compose) de la pantalla de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: composables de [CheckoutScreen] / CheckoutContent renderizando cada [CheckoutUiState].
 * Pistas: usa `composeRule.setContent { ... }` pasando el estado deseado y callbacks de prueba;
 * localiza nodos por texto (la pantalla aún no expone testTags) y verifica habilitación del botón.
 */
class CheckoutScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createCheckoutScreen(
        uiState: CheckoutUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onNameChanged: (String) -> Unit = { _ -> },
        onEmailChanged: (String) -> Unit = { _ -> },
        onAddressChanged: (String) -> Unit = { _ -> },
        onConfirm: () -> Unit = {},
    ) {
        composeRule.setContent {
            CheckoutContent(
                uiState = uiState,
                onBack = onBack,
                onRetry = onRetry,
                onNameChanged = onNameChanged,
                onEmailChanged = onEmailChanged,
                onAddressChanged = onAddressChanged,
                onConfirm = onConfirm,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() {
        // GIVEN + WHEN
        createCheckoutScreen(uiState = CheckoutUiState.Loading)
        // THEN
        composeRule.onNodeWithTag(CHECKOUT_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenIdleStateWithEmptyCart_whenRendered_thenConfirmButtonDisabled() {
        // GIVEN
        val emptyCartIdleState = checkoutIdleState { withIsCartEmpty(true) }
        // WHEN
        createCheckoutScreen(uiState = emptyCartIdleState)
        // THEN
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun givenIdleStateWithValidForm_whenRendered_thenConfirmButtonEnabled() {
        // GIVEN
        val validForm = CheckoutForm(name = "Miriam", address = "Kotlin", email = "apruebame@porfa.com")

        val validFormIdleState = checkoutIdleState { withForm(validForm) }
        // WHEN
        createCheckoutScreen(uiState = validFormIdleState)
        // THEN
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun givenIdleState_whenTypingInvalidEmail_thenConfirmButtonDisabled() {
        // GIVEN
        val validEmail = "apruebame@porfa.com"
        val validForm = CheckoutForm(name = "Miriam", address = "Kotlin", email = validEmail)

        var uiState by mutableStateOf(checkoutIdleState { withForm(validForm) })

        composeRule.setContent {
            CheckoutContent(uiState = uiState, onEmailChanged = { newEmail ->
                // Actualizamos la variable reactiva
                uiState =
                    checkoutIdleState {
                        withForm(uiState.form.copy(email = newEmail))
                    }
            }, onBack = {}, onRetry = {}, onNameChanged = {}, onAddressChanged = {}, onConfirm = {})
        }

        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsEnabled()

        // WHEN
        val invalidEmail = validEmail.dropLast(3) // "apruebame@porfa." (inválido)
        composeRule.onNodeWithTag(CHECKOUT_FORM_EMAIL_FIELD).performTextReplacement(invalidEmail)

        // THEN
        composeRule.onNodeWithTag(CHECKOUT_CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsOrderConfirmation() {
        // GIVEN + WHEN
        createCheckoutScreen(uiState = checkoutSuccessState())
        // THEN
        composeRule.onNodeWithTag(CHECKOUT_ORDER_CONFIRMATION).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenInvokesRetryCallback() {
        // GIVEN
        var retryClicked: Boolean = false
        createCheckoutScreen(
            uiState = checkoutErrorState(),
            onRetry = { retryClicked = true },
        )
        // WHEN
        composeRule.onNodeWithTag(CHECKOUT_RETRY_BUTTON).performClick()
        // THEN
        assertTrue(retryClicked)
    }
}
