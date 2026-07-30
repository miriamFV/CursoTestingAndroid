package com.example.cursotestingandroid.cart.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.cart.domain.model.CartSummary
import com.example.cursotestingandroid.core.mothers.ProductMother.bread
import com.example.cursotestingandroid.core.mothers.ProductMother.coffee
import com.example.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartItemWithPromotion
import com.example.cursotestingandroid.core.mothers.uistate.CartUiStateMother.cartSuccess
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CART_EMPTY_VIEW
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CART_ERROR_MESSAGE
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CART_LOADING
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.CART_RETRY_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.cartItem
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.cartQuantityDecrease
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.cartQuantityIncrease
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CartScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createCartScreen(
        uiState: CartUiState,
        onBack: () -> Unit = {},
        onIncreaseQuantity: (String, Int) -> Unit = {_, _ -> },
        onDecreaseQuantity: (String, Int) -> Unit = {_, _ -> },
        onRefresh: () -> Unit = {},
        onRemoveFromCart: (String) -> Unit = {}
    ){
        composeRule.setContent {
            CartScreenContent(
                uiState = uiState,
                onBack = onBack,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
                onRefresh = onRefresh,
                onRemoveFromCart = onRemoveFromCart
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowProgress(){
        createCartScreen(uiState = CartUiState.Loading)
        composeRule.onNodeWithTag(CART_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowProgress(){
        //Given
        val errorMessage = "Prueba error"
        createCartScreen(uiState = CartUiState.Error(errorMessage))
        //Then
        composeRule.onNodeWithTag(CART_ERROR_MESSAGE).assertIsDisplayed()
        val expectedErrorMessage = composeRule.activity.getString(R.string.cart_screen_error_with_message, errorMessage)
        composeRule.onNodeWithText(expectedErrorMessage).assertIsDisplayed()
        composeRule.onNodeWithTag(CART_RETRY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenEmitsRetryCallback(){
        var retryClicked: Boolean = false
        val errorMessage = "Prueba error"
        createCartScreen(uiState = CartUiState.Error(errorMessage), onRefresh = {retryClicked = true})
        composeRule.onNodeWithTag(CART_RETRY_BUTTON).performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun givenEmptyCart_whenRendered_thenShowEmptyView() {
        createCartScreen(uiState = CartUiState.Success(cartItems = emptyList(), isLoading = false))
        composeRule.onNodeWithTag(CART_EMPTY_VIEW).assertIsDisplayed()
        val emptyCartTitle = composeRule.activity.getString(R.string.cart_screen_empty_cart)
        val emptyCartSubtitle = composeRule.activity.getString(R.string.cart_screen_add_products_to_start)
        composeRule.onNodeWithText(emptyCartTitle).assertIsDisplayed()
        composeRule.onNodeWithText(emptyCartSubtitle).assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsItemsQuantitiesAndSummary() {
        createCartScreen(uiState = cartSuccess())
        composeRule.onNodeWithText(bread().name).assertIsDisplayed()
        composeRule.onNodeWithText(coffee().name).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_cart_summary)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_subtotal)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_discount)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_total)).assertIsDisplayed()

        composeRule.onNodeWithTag(cartItem(bread().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(cartItem(coffee().id)).assertIsDisplayed()
    }

    @Test
    fun givenSuccessStateWithoutDiscount_whenRendered_thenShowsItemsQuantitiesAndSummaryWithoutDiscount() {
        createCartScreen(uiState = cartSuccess(summary = CartSummary(
            subtotal = 11.0,
            discountTotal = 0.0,
            finalTotal = 11.0
        )))
        composeRule.onNodeWithText(bread().name).assertIsDisplayed()
        composeRule.onNodeWithText(coffee().name).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_cart_summary)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_subtotal)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_discount)).assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_screen_total)).assertIsDisplayed()
    }

    @Test
    fun givenInitialQuantity_whenIncreaseClicked_thenEmitsIncreaseQuantity(){
        var emitted : Pair<String,Int>? = null
        val initialQuantity = 2
        createCartScreen(
            uiState = cartSuccess(
                cartItems = listOf(
                    cartItemWithPromotion(product = bread(), quantity = initialQuantity)
                )
            ),
            onIncreaseQuantity = { productId, quantity -> emitted = productId to quantity}
        )
        composeRule.onNodeWithTag(cartQuantityIncrease(bread().id))
            .assertIsEnabled()
            .performClick()

        assertEquals(bread().id to initialQuantity, emitted)
    }

    @Test
    fun givenInitialQuantity_whenDecreaseClicked_thenEmitsDecreaseQuantity(){
        var emitted : Pair<String,Int>? = null
        val initialQuantity = 3
        createCartScreen(
            uiState = cartSuccess(
                cartItems = listOf(
                    cartItemWithPromotion(product = bread(), quantity = initialQuantity)
                )
            ),
            onDecreaseQuantity = { productId, quantity -> emitted = productId to quantity}
        )
        composeRule.onNodeWithTag(cartQuantityDecrease(bread().id))
            .assertIsEnabled()
            .performClick()

        assertEquals(bread().id to initialQuantity, emitted)
    }

    @Test
    fun givenCartItem_whenSwipedRight_thenEmitsRemovecallback(){
        var removeProductId: String? = null
        createCartScreen(
            uiState = cartSuccess(
                cartItems = listOf(
                    cartItemWithPromotion(product = bread(), quantity = 2)
                )
            ),
            onRemoveFromCart = { productId -> removeProductId = productId}
        )
        composeRule.onNodeWithTag(cartItem(bread().id)).performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 3000){
            removeProductId != null
        }
        assertEquals(bread().id, removeProductId)
    }

    @Test
    fun givenItemsAtStockEdges_whenRedered_thenInvalidControlsAreDisabled(){
        val fullStockItem = cartItemWithPromotion(
            product = bread(stock = 7),
            quantity = 7
        )
        createCartScreen(uiState = cartSuccess(cartItems = listOf(fullStockItem)))
        composeRule.onNodeWithTag(cartQuantityIncrease(bread().id)).assertIsNotEnabled()
    }

}