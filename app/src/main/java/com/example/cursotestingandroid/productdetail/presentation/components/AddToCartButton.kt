package com.example.cursotestingandroid.productdetail.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_ADD_TO_CART_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_DETAIL_ADD_TO_CART_BUTTON_NO_STOCK
import com.example.cursotestingandroid.productlist.domain.model.Product

@Composable
fun AddToCartButton(
    modifier: Modifier = Modifier,
    product: Product?,
    isLoading: Boolean,
    addToCart: () -> Unit,
) {
    product?.let {
        if (it.stock > 0) {
            AddToCartButtonWithStock(
                modifier = modifier.testTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON),
                product = product,
                isLoading = isLoading,
                addToCart = addToCart,
            )
        } else {
            AddToCartButtonNoStock(modifier.testTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON_NO_STOCK))
        }
    }
}
