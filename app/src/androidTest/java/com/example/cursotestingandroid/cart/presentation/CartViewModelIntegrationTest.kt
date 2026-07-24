package com.example.cursotestingandroid.cart.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.domain.usecase.GetCartItemsWithPromotionsUseCase
import com.example.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.example.cursotestingandroid.cart.domain.usecase.UpdateCartItemUseCase
import com.example.cursotestingandroid.core.MainDispatcherRule
import com.example.cursotestingandroid.core.mockwebserver.MarketApiDispatcher
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.core.utils.asAsset
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartViewModelIntegrationTest {

    private companion object{
        const val PRODUCT_ID = "p1"
        const val INITIAL_QUANTITY = 1
        const val UPDATED_QUANTITY = 2
    }

    @get:Rule(order = 0)
    val mockWebServerRule= MockWebServerRule()

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var cartRepository: CartRepository
    @Inject
    lateinit var promotionRepository: PromotionRepository
    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var getCartSummaryUseCase: GetCartSummaryUseCase
    @Inject
    lateinit var updateCartItemUseCase: UpdateCartItemUseCase
    @Inject
    lateinit var getCartItemsWithPromotionsUseCase: GetCartItemsWithPromotionsUseCase

    @Before
    fun setUp() = runTest {
        mockWebServerRule.server.dispatcher =
            MarketApiDispatcher(productJson = "product_list_default.json".asAsset())
        hiltAndroidRule.inject()
        cartRepository.clearCart()
        productRepository.refreshProduct()
        promotionRepository.refreshPromotions()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenCartWithItems_whenViewModelCollectsUiState_thenSuccessWithSummary() = runTest {

        cartRepository.addToCart(PRODUCT_ID, UPDATED_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val result =
                awaitSuccessMatching { state ->
                    state.summary != null && state.cartItems.isNotEmpty()
                }
            assertTrue(result.cartItems.isNotEmpty())
            assertTrue(result.summary != null)
            assertEquals(20.0, result.summary?.subtotal)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSingleProduct_whenIncreaseQuantity_thenQuantityUpdates() = runTest {

        cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val success =
                awaitSuccessMatching { state ->
                    state.cartItems.any {
                        it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                    }
                }
            assertEquals(INITIAL_QUANTITY, success.cartItems.first().cartItem.quantity)
            viewModel.increaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)
            val updateSuccess =
                awaitSuccessMatching { state ->
                    state.cartItems.any {
                        it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == UPDATED_QUANTITY
                    }
                }
            assertEquals(UPDATED_QUANTITY, updateSuccess.cartItems.first().cartItem.quantity)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSingleProduct_whenDecreaseQuantity_thenQuantityUpdates() = runTest {

        cartRepository.addToCart(PRODUCT_ID, 3)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val success =
                awaitSuccessMatching { state ->
                    state.cartItems.any {
                        it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == 3
                    }
                }
            assertEquals(3, success.cartItems.first().cartItem.quantity)
            viewModel.decreaseQuantity(PRODUCT_ID, 3)
            val updateSuccess =
                awaitSuccessMatching { state ->
                    state.cartItems.any {
                        it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == UPDATED_QUANTITY
                    }
                }
            assertEquals(UPDATED_QUANTITY, updateSuccess.cartItems.first().cartItem.quantity)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSingleProduct_whenDecreaseToZero_thenCartBecomesEmpty() = runTest {

        cartRepository.addToCart(PRODUCT_ID, INITIAL_QUANTITY)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val success =
                awaitSuccessMatching { state ->
                    state.cartItems.any {
                        it.cartItem.productId == PRODUCT_ID && it.cartItem.quantity == INITIAL_QUANTITY
                    }
                }
            assertEquals(INITIAL_QUANTITY, success.cartItems.first().cartItem.quantity)
            viewModel.decreaseQuantity(PRODUCT_ID, INITIAL_QUANTITY)
            val updateSuccess =
                awaitSuccessMatching { state ->
                    state.cartItems.isEmpty()
                }
            assertTrue(updateSuccess.cartItems.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(): CartViewModel{
        return CartViewModel(
            cartRepository = cartRepository,
            getCartSummaryUseCase = getCartSummaryUseCase,
            updateCartItemUseCase = updateCartItemUseCase,
            getCartItemsWithPromotionsUseCase = getCartItemsWithPromotionsUseCase
        )
    }

    private suspend fun ReceiveTurbine<CartUiState>.awaitSuccessMatching(
        predicate:(CartUiState.Success) -> Boolean
    ): CartUiState.Success{
        while (true){
            when(val item = awaitItem()){
                is CartUiState.Success -> if(predicate(item)) return item
                is CartUiState.Error -> error("Unexpected error: ${item.message}")
                is CartUiState.Loading -> Unit
            }
        }
    }
}