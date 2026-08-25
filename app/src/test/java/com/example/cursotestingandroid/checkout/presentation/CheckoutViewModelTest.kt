package com.example.cursotestingandroid.checkout.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.example.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeOrderRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del ViewModel de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] — estados [CheckoutUiState], `canSubmit`, `onConfirm`, eventos.
 * Pistas: usa Turbine sobre `uiState`/`event`, `runTest(mainDispatcherRule.scheduler)`,
 * fakes (FakeCartItemRepository, FakeProductRepository, FakePromotionRepository, FakeSystemClock)
 * y un fake de OrderRepository que tendrás que crear.
 */
class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeOrderRepository: OrderRepository = FakeOrderRepository(),
        fakeCartRepository: CartRepository = FakeCartRepository(),
        fakeProductRepository: ProductRepository = FakeProductRepository(),
        fakePromotionRepository: PromotionRepository = FakePromotionRepository(),
        fakeClock: Clock = FakeSystemClock(),
    ): CheckoutViewModel {
        val placeOrderUseCase =
            PlaceOrderUseCase(
                fakeOrderRepository,
                fakeCartRepository,
            )
        val getCartSummaryUseCase =
            GetCartSummaryUseCase(
                fakeCartRepository,
                fakeProductRepository,
                fakePromotionRepository,
                GetPromotionForProduct(),
                fakeClock,
            )
        return CheckoutViewModel(
            placeOrderUseCase,
            getCartSummaryUseCase,
        )
    }

    @Test
    fun `given empty cart when initialized then canSubmit is false`() {
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(emptyList()) }
            // WHEN
            val viewModel =
                createViewModel(
                    fakeCartRepository = fakeCartRepository,
                )
            // THEN
            viewModel.uiState.test {
                val state = awaitItem() as CheckoutUiState.Idle
                assertFalse(state.canSubmit)
            }
        }
    }

    @Test
    fun `given valid form and non empty cart when form completed then canSubmit is true`() {
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "p1"
            val cart = listOf(cartItem { withProductId(productId) })
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product { withId(productId) })) }
            val viewModel =
                createViewModel(
                    fakeCartRepository = fakeCartRepository,
                    fakeProductRepository = fakeProductRepository,
                )

            val validForm = CheckoutForm(name = "Miriam", address = "Kotlin", email = "apruebame@porfa.com")

            // WHEN
            viewModel.onNameChanged(validForm.name)
            viewModel.onAddressChanged(validForm.address)
            viewModel.onEmailChanged(validForm.email)

            // THEN
            viewModel.uiState.test {
                val state = awaitItem() as CheckoutUiState.Idle
                assertTrue(state.canSubmit)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `given malformed email when email changed then emailError is INVALID_EMAIL and canSubmit is false`() {
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "p1"
            val cart = listOf(cartItem { withProductId(productId) })
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product { withId(productId) })) }
            val viewModel =
                createViewModel(
                    fakeCartRepository = fakeCartRepository,
                    fakeProductRepository = fakeProductRepository,
                )

            // WHEN
            viewModel.onNameChanged("Miriam")
            viewModel.onAddressChanged("Kotlin")
            viewModel.onEmailChanged("email-no-valido@")

            // THEN
            viewModel.uiState.test {
                val state = awaitItem() as CheckoutUiState.Idle
                assertFalse("canSubmit debería ser false porque el email es inválido", state.canSubmit)
                assertEquals(FieldError.INVALID_EMAIL, state.errors.emailError)
                assertNull("El nombre del formulario debería ser válido", state.errors.nameError)
                assertNull("La dirección del formulario debería ser válida", state.errors.addressError)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `given valid form when onConfirm succeeds then emits Success state`() {
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val productId = "p1"
            val cart = listOf(cartItem { withProductId(productId) })
            val fakeOrderRepository = FakeOrderRepository()
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product { withId(productId) })) }
            val viewModel =
                createViewModel(
                    fakeOrderRepository = fakeOrderRepository,
                    fakeCartRepository = fakeCartRepository,
                    fakeProductRepository = fakeProductRepository,
                )

            val validForm = CheckoutForm(name = "Miriam", address = "Kotlin", email = "apruebame@porfa.com")

            viewModel.onNameChanged(validForm.name)
            viewModel.onAddressChanged(validForm.address)
            viewModel.onEmailChanged(validForm.email)

            viewModel.uiState.test {
                // WHEN
                viewModel.onConfirm()

                // THEN
                val state = expectMostRecentItem() // Descarta estados intermedios (Idle, Submitting)
                assertTrue("El estado debería ser Success", state is CheckoutUiState.Success)
                val confirmation = (state as CheckoutUiState.Success).confirmation
                assertEquals(fakeOrderRepository.orderConfirmation, confirmation)
            }
        }
    }

    @Test
    fun `given place order fails when onConfirm then emits Error state and ShowMessage event`() {
        runTest(mainDispatcherRule.scheduler) {
            // GIVEN
            val fakeOrderRepository = FakeOrderRepository().apply { returnError = true }
            val productId = "p1"
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(
                        listOf(
                            product {
                                withId(productId)
                                withPrice(10.0)
                            },
                        ),
                    )
                }
            val fakeCartRepository =
                FakeCartRepository().apply {
                    setCartItems(listOf(cartItem { withProductId(productId) }))
                }

            val viewModel =
                createViewModel(
                    fakeOrderRepository = fakeOrderRepository,
                    fakeCartRepository = fakeCartRepository,
                    fakeProductRepository = fakeProductRepository,
                )

            viewModel.onNameChanged("Miriam")
            viewModel.onAddressChanged("Kotlin")
            viewModel.onEmailChanged("apruebame@porfi.com")

            turbineScope {
                val stateTurbine = viewModel.uiState.testIn(this)
                val eventTurbine = viewModel.event.testIn(this)

                // WHEN
                viewModel.onConfirm()

                // THEN - Comprobamos el estado
                val state = stateTurbine.expectMostRecentItem()
                assertTrue("El estado final debería ser Error", state is CheckoutUiState.Error)

                // THEN - Comprobamos el evento
                val event = eventTurbine.awaitItem()
                assertTrue("Debería emitirse un evento ShowMessage", event is CheckoutEvent.ShowMessage)

                stateTurbine.cancel()
                eventTurbine.cancel()
            }
        }
    }

    @Test
    fun `given invalid form when onConfirm then does not place order`() {
        // GIVEN
        val fakeOrderRepository = FakeOrderRepository()
        val viewModel = createViewModel(fakeOrderRepository = fakeOrderRepository)

        viewModel.onEmailChanged("email-invalido")

        // WHEN
        viewModel.onConfirm()

        // THEN
        assertFalse("El repositorio no debería haberse llamado", fakeOrderRepository.placeOrderCalled)
    }
}
