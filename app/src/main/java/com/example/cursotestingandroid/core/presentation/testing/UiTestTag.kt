package com.example.cursotestingandroid.core.presentation.testing

import com.example.cursotestingandroid.productlist.domain.model.SortOption

object UiTestTag {
    //TOOLBAR
    const val TOP_APP_BAR_BACK = "top_app_bar_back"
    const val TOP_APP_BAR_BADGE = "top_app_bar_badge"
    const val TOP_APP_BAR_FILTERS_BUTTON = "top_app_bar_filters_button"
    const val TOP_APP_BAR_SETTINGS_BUTTON = "top_app_bar_settings_button"
    const val TOP_APP_BAR_CART_BUTTON = "top_app_bar_cart_button"
    const val FILTER_VIEW = "product_list_filter"


    //SETTINGS
    const val SETTINGS_CONTENT = "settings_content"
    const val SETTINGS_IN_STOCK_SWITCH = "settings_in_stock_switch"
    const val SETTINGS_TAX_SWITCH = "settings_tax_switch"

    fun settingsThemeOption(themeModeName: String): String = "settings_theme_${themeModeName.lowercase()}"

    //PRODUCT LIST
    const val PRODUCT_LIST_LOADING = "product_list_loading"
    const val PRODUCT_LIST_LIST = "product_list_list"
    fun productListItem(productId: String) = "product_list_item_$productId"
    fun productListCategory(category: String?) = "product_list_category_${category ?: "all"}"
    fun productListSortOption(sortOptionName: String) = "product_list_sort_option_${sortOptionName.lowercase()}"
}