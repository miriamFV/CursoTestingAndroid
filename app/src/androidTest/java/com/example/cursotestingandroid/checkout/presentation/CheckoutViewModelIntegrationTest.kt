package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.example.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.example.cursotestingandroid.checkout.presentation.CheckoutUiState
import com.example.cursotestingandroid.checkout.presentation.CheckoutViewModel
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.mockwebserver.MarketApiDispatcher
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.core.utils.asAsset
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * EXAMEN — Tests de INTEGRACIÓN del ViewModel de checkout (extremo a extremo).
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] con casos de uso reales + Room + MockWebServer.
 * Pistas: inyecta dependencias con Hilt, prepara el carrito real, observa `uiState` con Turbine,
 * y verifica que tras un pedido OK el estado es Success y el carrito queda vacío.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CheckoutViewModelIntegrationTest {
    private companion object {
        const val DEFAULT_PRODUCT_ASSET = "product_list_default.json"
        const val PRODUCT_ID = "p1"
        const val QUANTITY = 2
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var cartRepository: CartRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var placeOrderUseCase: PlaceOrderUseCase

    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase

    @Before
    fun setUp() =
        runTest {
            mockWebServer.server.dispatcher =
                MarketApiDispatcher(productJson = DEFAULT_PRODUCT_ASSET.asAsset())
            hilt.inject()
            cartRepository.clearCart()
            productRepository.refreshProduct()
            promotionRepository.refreshPromotions()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenItemsInCart_whenViewModelInitialized_thenIdleStateWithSummary() {
        runTest {
            // GIVEN
            cartRepository.addToCart(PRODUCT_ID, QUANTITY)

            // WHEN
            val viewModel = createViewModel()

            // THEN
            viewModel.uiState.test {
                val state = awaitState<CheckoutUiState.Idle>()
                assertEquals(20.0, state.summary.subtotal, 0.01)
                assertFalse(state.isCartEmpty, "isCartEmpty debería ser false")
                assertFalse(state.isSubmitting, "isSubmitting debería ser false")
                assertFalse(state.canSubmit, "canSubmit debería ser false (formulario inválido)")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenValidFormAndSuccessfulOrder_whenOnConfirm_thenSuccessStateAndCartCleared() =
        runTest {
            // GIVEN
            cartRepository.addToCart(PRODUCT_ID, QUANTITY)

            val viewModel = createViewModel().apply { fillValidForm() }

            viewModel.uiState.test {
                // WHEN
                viewModel.onConfirm()

                // THEN
                val successState = awaitState<CheckoutUiState.Success>()
                assertTrue(successState.confirmation.orderId.isNotEmpty())

                val summary = getCartSummaryUseCase().first()
                assertEquals(0.0, summary.subtotal, 0.01)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenOrderEndpointFails_whenOnConfirm_thenErrorState() =
        runTest {
            // GIVEN
            mockWebServer.server.dispatcher =
                MarketApiDispatcher(productJson = DEFAULT_PRODUCT_ASSET.asAsset(), orderCode = 500)

            cartRepository.addToCart(PRODUCT_ID, QUANTITY)

            val viewModel = createViewModel().apply { fillValidForm() }

            viewModel.uiState.test {
                // WHEN
                viewModel.onConfirm()

                // THEN
                val errorState = awaitState<CheckoutUiState.Error>()
                assertTrue(errorState.message.isNotEmpty())
                assertEquals("Network Error", errorState.message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createViewModel(): CheckoutViewModel =
        CheckoutViewModel(
            placeOrderUseCase = placeOrderUseCase,
            getCartSummaryUseCase = getCartSummaryUseCase,
        )

    private fun CheckoutViewModel.fillValidForm() {
        onNameChanged("Miriam")
        onEmailChanged("apruebame@porfi.com")
        onAddressChanged("Kotlin")
    }

    private suspend inline fun <reified T : CheckoutUiState> ReceiveTurbine<CheckoutUiState>.awaitState(): T {
        while (true) {
            val item = awaitItem()
            if (item is T) return item
            if (item is CheckoutUiState.Error && T::class != CheckoutUiState.Error::class) {
                error("Se esperaba el estado ${T::class.simpleName} pero se recibió Error: ${item.message}")
            }
        }
    }
}
