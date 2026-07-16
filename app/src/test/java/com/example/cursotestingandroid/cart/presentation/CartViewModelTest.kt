package com.example.cursotestingandroid.cart.presentation

import app.cash.turbine.test
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.example.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.example.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.core.fakes.FakePromotionRepository
import com.example.cursotestingandroid.core.fakes.FakeSystemClock
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        fakeCartRepository: CartRepository = FakeCartRepository(),
        fakeProductRepository: ProductRepository = FakeProductRepository(),
        fakePromotionRepository: PromotionRepository = FakePromotionRepository(),
        fakeClock: Clock = FakeSystemClock()
    ): CartViewModel {

        val getCartSummaryUseCase = GetCartSummaryUseCase(
            fakeCartRepository, fakeProductRepository, fakePromotionRepository,
            GetPromotionForProduct(), fakeClock
        )
        val updateCartItemUseCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)
        val getCartItemsWithPromotionsUseCase =
            GetCartItemsWithPromotionsUseCase(
                fakeCartRepository, fakeProductRepository, fakePromotionRepository,
                GetPromotionForProduct(), fakeClock
            )

        return CartViewModel(
            fakeCartRepository,
            getCartSummaryUseCase,
            updateCartItemUseCase,
            getCartItemsWithPromotionsUseCase
        )
    }

    @Test
    fun givenCartData_whenInitialize_thenEmitSuccessState() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val productId = "pId"
            val product = product { withId(productId); withPrice(10.0); withName("salmon") }
            val cartItem = cartItem { withProductId(productId); withQuantity(2) }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }

            //When
            val viewModel = createViewModel(
                fakeCartRepository = fakeCartRepository,
                fakeProductRepository = fakeProductRepository
            )

            //Then
            viewModel.uiState.test {
                val state = awaitItem() as CartUiState.Success
                assertEquals(1, state.cartItems.size)
                assertEquals(20.0, state.summary?.subtotal)

                cancelAndIgnoreRemainingEvents()
            }
        }


    @Test
    fun givenCartItemWithQuantityOne_whenDecreaseQuantity_thenRemoveItemFromCart() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val productId = "pId"
            val product = product { withId(productId); withPrice(10.0); withStock(5) }
            val cartItem = cartItem { withProductId(productId); withQuantity(1) }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }

            val viewModel = createViewModel(
                fakeCartRepository = fakeCartRepository,
                fakeProductRepository = fakeProductRepository
            )

            viewModel.uiState.test {
                awaitItem()
                //When
                viewModel.decreaseQuantity(productId, 1)

                //Then
                val state = awaitItem() as CartUiState.Success
                assertTrue(state.cartItems.isEmpty())
                assertEquals(0.0, state.summary?.finalTotal ?: 0.0, 0.001)

                cancelAndIgnoreRemainingEvents()
            }

        }

    @Test
    fun givenInsufficientStock_whenUpdateQuantity_thenEmitsErrorEvent() =
        runTest(mainDispatcherRule.scheduler) {
            //Given
            val productId = "pId"
            val product = product { withId(productId); withPrice(10.0); withStock(2) }
            val cartItem = cartItem { withProductId(productId); withQuantity(1) }
            val fakeProductRepository =
                FakeProductRepository().apply { setProducts(listOf(product)) }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }

            val viewModel = createViewModel(
                fakeCartRepository = fakeCartRepository,
                fakeProductRepository = fakeProductRepository
            )

            viewModel.events.test {
                //When
                viewModel.increaseQuantity(productId, 5)
                val event = awaitItem()

                //Then
                assertTrue(event is CartEvent.ShowMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }



}