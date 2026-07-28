package com.example.cursotestingandroid.core.presentation.testing

object UiTestTag {
    //TOOLBAR
    const val TOP_APP_BAR = "top_app_bar"

    //SETTINGS
    const val SETTINGS_CONTENT = "settings_content"
    const val SETTINGS_IN_STOCK_SWITCH = "settings_in_stock_switch"
    const val SETTINGS_TAX_SWITCH = "settings_tax_switch"

    fun settingsThemeOption(themeModeName: String): String = "settings_theme_${themeModeName.lowercase()}"
}