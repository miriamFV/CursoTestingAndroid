package com.example.cursotestingandroid.core.mothers.uistate

import com.example.cursotestingandroid.core.mothers.ProductMother
import com.example.cursotestingandroid.core.mothers.PromotionMother
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.presentation.ProductListUiState

object ProductListUiStateMother {
    fun success(
        products: List<ProductWithPromotion> =
            listOf(
                ProductWithPromotion(ProductMother.coffee(), PromotionMother.percent()),
                ProductWithPromotion(ProductMother.milk()),
                ProductWithPromotion(ProductMother.bread()),
//            ProductWithPromotion(product { withId("1") }),
//            ProductWithPromotion(product { withId("12") }),
//            ProductWithPromotion(product { withId("123") }),
//            ProductWithPromotion(product { withId("1234") }),
            ),
        categories: List<String> = listOf("drinks", "lacteo", "bread"),
        selectedCategory: String? = null,
        sortOption: SortOption = SortOption.NONE,
    ) = ProductListUiState.Success(
        products = products,
        categories = categories,
        selectedCategory = selectedCategory,
        sortOption = sortOption,
    )
}
