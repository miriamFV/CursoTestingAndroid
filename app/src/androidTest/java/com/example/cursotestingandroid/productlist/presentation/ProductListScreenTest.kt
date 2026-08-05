package com.example.cursotestingandroid.productlist.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.mothers.ProductMother.bread
import com.example.cursotestingandroid.core.mothers.ProductMother.coffee
import com.example.cursotestingandroid.core.mothers.ProductMother.milk
import com.example.cursotestingandroid.core.mothers.uistate.ProductListUiStateMother
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.FILTER_VIEW
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LOADING
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_BADGE
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_CART_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_FILTERS_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_SETTINGS_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.productListCategory
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.productListItem
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.productListSortOption
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createProductListScreen(
        uiState: ProductListUiState = ProductListUiStateMother.success(),
        cartItemCount: Int = 0,
        filterVisible: Boolean = true,
        onFiltersSelected: (Boolean) -> Unit = {},
        onCategorySelected: (String?) -> Unit = {},
        onSortOptionSelected: (SortOption) -> Unit = {},
        onSettingsSelected: () -> Unit = {},
        onProductSelected: (ProductWithPromotion) -> Unit = {},
        onCartSelected: () -> Unit = {},
    ) {
        composeRule.setContent {
            ProductListContent(
                uiState = uiState,
                cartItemCount = cartItemCount,
                filterVisible = filterVisible,
                onFiltersSelected = onFiltersSelected,
                onSettingsSelected = onSettingsSelected,
                onCartSelected = onCartSelected,
                onCategorySelected = onCategorySelected,
                onSortOptionSelected = onSortOptionSelected,
                onProductSelected = onProductSelected,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgressView() {
        createProductListScreen(uiState = ProductListUiState.Loading)
        composeRule.onNodeWithTag(PRODUCT_LIST_LOADING).assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRendered_thenShowsErrorMessage() {
        createProductListScreen(uiState = ProductListUiState.Error("test"))
        composeRule.onNodeWithText("Error: test").assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsProductsAndCount() {
        createProductListScreen(uiState = ProductListUiStateMother.success())
        composeRule.onNodeWithText("3 productos").assertIsDisplayed()
        composeRule.onNodeWithTag(FILTER_VIEW).assertIsDisplayed()

        composeRule.onNodeWithTag(productListItem(coffee().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(productListItem(milk().id)).assertIsDisplayed()
        composeRule.onNodeWithTag(productListItem(bread().id)).assertIsDisplayed()

//        composeRule.onNodeWithTag(PRODUCT_LIST_LIST).performScrollToIndex(6) //Goes to the exact position
//        composeRule.onNodeWithTag(productListItem("1234")).assertIsDisplayed()

//        composeRule.onNodeWithTag(PRODUCT_LIST_LIST).performScrollToNode(hasTestTag(productListItem("1234")))
//        composeRule.onNodeWithTag(productListItem("1234")).assertIsDisplayed()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsEmptyMessage() {
        createProductListScreen(uiState = ProductListUiStateMother.success(products = emptyList()))
        composeRule.onNodeWithText("No se encontraron productos").assertIsDisplayed()
    }

    @Test
    fun givenNoCategorySelected_whenRendered_thenMarkAllChip() {
        createProductListScreen(uiState = ProductListUiStateMother.success(selectedCategory = null))
        composeRule.onNodeWithTag(productListCategory(null)).assertIsSelected()
    }

    @Test
    fun givenSelectedCategory_whenRendered_thenMarkThatChip() {
        val category = "drinks"
        createProductListScreen(uiState = ProductListUiStateMother.success(selectedCategory = category))
        composeRule.onNodeWithTag(productListCategory(category)).assertIsSelected()
    }

    @Test
    fun givenCartItemCountZero_whenRendered_thenHidesBadge() {
        createProductListScreen(cartItemCount = 0)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertDoesNotExist()
    }

    @Test
    fun givenCartItemCountPositive_whenRendered_thenShowsBadgeWithCount() {
        val cartItemCount = 25
        createProductListScreen(cartItemCount = cartItemCount)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText(cartItemCount.toString()).assertIsDisplayed()
    }

    @Test
    fun givenCartItemCountPositiveAndPlusThan99_whenRendered_thenShowsBadgeWith99Plus() {
        val cartItemCount = 100
        createProductListScreen(cartItemCount = cartItemCount)
        composeRule.onNodeWithTag(TOP_APP_BAR_BADGE).assertIsDisplayed()
        val expectedBadgeItemCount =
            composeRule.activity.getString(R.string.product_list_screen_top_app_bar_cart_max_count)
        composeRule.onNodeWithText(expectedBadgeItemCount).assertIsDisplayed()
    }

    @Test
    fun givenFiltersVisible_whenToggleClicked_thenEmitFalse() {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = true,
            onFiltersSelected = { emitted = it },
        )
        composeRule.onNodeWithTag(TOP_APP_BAR_FILTERS_BUTTON).performClick()
        assertEquals(false, emitted)
    }

    @Test
    fun givenFiltersHiden_whenToggleClicked_thenEmitTrue() {
        var emitted: Boolean? = null
        createProductListScreen(
            filterVisible = false,
            onFiltersSelected = { emitted = it },
        )
        composeRule.onNodeWithTag(TOP_APP_BAR_FILTERS_BUTTON).performClick()
        assertEquals(true, emitted)
    }

    @Test
    fun givenProductListRendered_whenSettingsIconClicked_thenEmitCallback() {
        var settingClicked = false
        createProductListScreen(
            onSettingsSelected = { settingClicked = true },
        )
        composeRule.onNodeWithTag(TOP_APP_BAR_SETTINGS_BUTTON).performClick()
        assertEquals(true, settingClicked)
    }

    @Test
    fun givenProductListRendered_whenCartIconClicked_thenEmitCallback() {
        var cartClicked = false
        createProductListScreen(
            onCartSelected = { cartClicked = true },
        )
        composeRule.onNodeWithTag(TOP_APP_BAR_CART_BUTTON).performClick()
        assertEquals(true, cartClicked)
    }

    @Test
    fun givenNoSortOptionSelected_whenRendered_thenNoneChipIsSelected() {
        createProductListScreen(uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE))
        composeRule.onNodeWithTag(productListSortOption(SortOption.PRICE_ASC.name)).assertIsNotSelected()
        composeRule
            .onNodeWithTag(productListSortOption(SortOption.PRICE_DESC.name))
            .assertIsNotSelected()
        composeRule.onNodeWithTag(productListSortOption(SortOption.DISCOUNT.name)).assertIsNotSelected()
    }

    @Test
    fun givenSortOptionSelected_whenRendered_thenThatChipIsSelected() {
        createProductListScreen(uiState = ProductListUiStateMother.success(sortOption = SortOption.PRICE_DESC))
        composeRule.onNodeWithTag(productListSortOption(SortOption.PRICE_ASC.name)).assertIsNotSelected()
        composeRule.onNodeWithTag(productListSortOption(SortOption.PRICE_DESC.name)).assertIsSelected()
        composeRule.onNodeWithTag(productListSortOption(SortOption.DISCOUNT.name)).assertIsNotSelected()
    }

    @Test
    fun givenProductListRendered_whenSortPriceAscClicked_thenEmitPriceAscSortOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })
        composeRule.onNodeWithTag(productListSortOption(SortOption.PRICE_ASC.name)).performClick()
        assertEquals(SortOption.PRICE_ASC, selectedSort)
    }

    @Test
    fun givenProductListRendered_whenSortPriceDescClicked_thenEmitPriceDescSortOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })
        composeRule.onNodeWithTag(productListSortOption(SortOption.PRICE_DESC.name)).performClick()
        assertEquals(SortOption.PRICE_DESC, selectedSort)
    }

    @Test
    fun givenProductListRendered_whenSortDiscountClicked_thenEmitDiscountSortOption() {
        var selectedSort: SortOption? = null
        createProductListScreen(onSortOptionSelected = { newSort -> selectedSort = newSort })
        composeRule.onNodeWithTag(productListSortOption(SortOption.DISCOUNT.name)).performClick()
        assertEquals(SortOption.DISCOUNT, selectedSort)
    }
}
