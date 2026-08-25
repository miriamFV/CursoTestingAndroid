package com.example.cursotestingandroid.productdetail.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.mothers.ProductMother.bread
import com.example.cursotestingandroid.core.mothers.ProductMother.milk
import com.example.cursotestingandroid.core.mothers.PromotionMother.buyXPayY
import com.example.cursotestingandroid.core.mothers.PromotionMother.percent
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_ADD_TO_CART_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_ADD_TO_CART_BUTTON_NO_STOCK
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_BUYXPAYY_PROMOTION_LABEL
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_LOADING
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_PERCENT_PROMOTION_LABEL
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_PERCENT_PROMO_OLD_PRICE
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_PRODUCT_CATEGORY
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_PRODUCT_NAME
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_STOCK_QUANTITY
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_BAR_TITLE
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createProductDetailScreen(
        uiState: ProductDetailUiState,
        onBack: () -> Unit = {},
        onAddToCart: () -> Unit = {},
    ) {
        composeRule.setContent {
            ProductDetailContent(
                uiState = uiState,
                onBack = onBack,
                onAddToCart = onAddToCart,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() {
        createProductDetailScreen(uiState = ProductDetailUiState(isLoading = true))
        composeRule.onNodeWithTag(PRODUCT_DETAIL_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenTopBarShowsProductName() {
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item = ProductWithPromotion(product = bread(), promotion = percent()),
                ),
        )
        composeRule
            .onNodeWithTag(TOP_BAR_TITLE)
            .assertIsDisplayed()
            .assertTextEquals(bread().name)
    }

    @Test
    fun givenProductWithoutStock_whenRendered_thenShowsDisabledNoStockAction() {
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item = ProductWithPromotion(product = bread(stock = 0)),
                ),
        )
        // Add to cart button
        composeRule.onNodeWithTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON_NO_STOCK).assertIsDisplayed()
        composeRule.onNodeWithTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON).assertDoesNotExist()
        // Stock text
        val expectedText = composeRule.activity.getString(R.string.product_detail_screen_no_stock)
        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_STOCK_QUANTITY)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }

    @Test
    fun givenProductWithStock_whenRendered_thenShowsAddToCartButtonWithStockAndStockQuantity() {
        val stock = 8
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item = ProductWithPromotion(product = bread(stock = stock), promotion = percent()),
                ),
        )
        // Add to cart button
        composeRule.onNodeWithTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON_NO_STOCK).assertDoesNotExist()

        // Stock text
        val expectedText =
            composeRule.activity.resources.getQuantityString(
                R.plurals.product_detail_screen_product_units,
                stock,
                stock,
            )
        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_STOCK_QUANTITY)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }

    @Test
    fun givenProductWithPercentPromotion_whenRendered_thenShowPercentPromotionInfo() {
        val percentPromo = percent(percent = 10.0, discountedPrice = 10.0)
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            product = milk(),
                            promotion = percentPromo,
                        ),
                ),
        )
        composeRule.onNodeWithTag(PRODUCT_DETAIL_PERCENT_PROMO_OLD_PRICE)
            .performScrollTo()
            .assertIsDisplayed()

        val expectedText =
            composeRule.activity.getString(
                R.string.product_detail_screen_percent_off,
                percentPromo.percent.toInt(),
            )
        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_PERCENT_PROMOTION_LABEL)
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }

    @Test
    fun givenProductWithBuyXPayYPromotion_whenRendered_thenShowBuyXPayYPromotionInfo() {
        val promotion = buyXPayY(buy = 3, pay = 2)
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            product = bread(stock = 0),
                            promotion = promotion,
                        ),
                ),
        )

        val expectedText =
            composeRule.activity.getString(
                R.string.product_detail_screen_buy_x_pay_y_promotion,
                promotion.label,
            )
        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_BUYXPAYY_PROMOTION_LABEL)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }

    @Test
    fun givenProductWithoutPromotion_whenRendered_thenShowNormalProductInfo() {
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            product = bread(),
                            promotion = null,
                        ),
                ),
        )

        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_PRODUCT_NAME)
            .assertIsDisplayed()
            .assertTextEquals(bread().name)

        composeRule
            .onNodeWithTag(PRODUCT_DETAIL_PRODUCT_CATEGORY)
            .assertIsDisplayed()
            .assertTextEquals(bread().category)
    }

    @Test
    fun givenProductWithStock_whenAddToCartClicked_thenEmitsAddToCart() {
        var addToCart: Boolean = false
        createProductDetailScreen(
            uiState =
                ProductDetailUiState(
                    isLoading = false,
                    item =
                        ProductWithPromotion(
                            product = bread(),
                            promotion = null,
                        ),
                ),
            onAddToCart = { addToCart = true },
        )
        composeRule.onNodeWithTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON).performClick()
        assertTrue(addToCart)
    }
}
