package com.example.cursotestingandroid.settings.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_CONTENT
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_IN_STOCK_SWITCH
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_TAX_SWITCH
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.settingsThemeOption
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertTrue

class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createSettingsScreen(
        uiState: SettingsUiState = SettingsUiState(),
        onBack:() -> Unit = {},
        onInStockOnlyChange:(Boolean) -> Unit = {},
        onThemeModeSelected:(ThemeMode) -> Unit = {}
    ){
        composeRule.setContent {
            SettingsContent(
                uiState = uiState,
                onBack = onBack,
                onInStockOnlyChange = onInStockOnlyChange,
                onThemeModeSelected = onThemeModeSelected
            )
        }
    }

    private fun getString(resId: Int): String = composeRule.activity.getString(resId)

    @Test
    fun givenDefaultSettingsState_whenRendered_thenShowsFilterAndAppearanceSections(){
        createSettingsScreen(uiState = SettingsUiState())

        val SettingsTitleText = getString(R.string.settings_screen_top_app_bar_title)
        val FiltersSectionTitleText = getString(R.string.settings_screen_filters_and_visualization)
        val inStockText = getString(R.string.settings_screen_only_in_stock_products)
        val inTaxesText = getString(R.string.settings_screen_show_taxes_included)
        val AppearanceSectionTitleText = getString(R.string.settings_screen_appearance)
        val themeText = getString(R.string.settings_screen_app_theme)

        composeRule.onNodeWithText(SettingsTitleText).assertIsDisplayed()
        composeRule.onNodeWithText(FiltersSectionTitleText).assertIsDisplayed()
        composeRule.onNodeWithText(inStockText).assertIsDisplayed()
        composeRule.onNodeWithText(inTaxesText).assertIsDisplayed()
        composeRule.onNodeWithText(AppearanceSectionTitleText).assertIsDisplayed()
        composeRule.onNodeWithText(themeText).assertIsDisplayed()

        composeRule.onNodeWithTag(SETTINGS_CONTENT).assertIsDisplayed()
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(SETTINGS_TAX_SWITCH).assertIsOn()
    }

    @Test
    fun givenInStockOnlyFalse_whenRendered_thenSwitchIsOff() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = false))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
    }

    @Test
    fun givenInStockOnlyOn_whenRendered_thenSwitchIsOn() {
        createSettingsScreen(uiState = SettingsUiState(inStockOnly = true))
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOn()
    }

    @Test
    fun givenLightTheme_whenRendered_thenLightOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.LIGHT))
        composeRule.onNodeWithTag(settingsThemeOption("LIGHT")).assertIsSelected()
    }

    @Test
    fun givenDarkTheme_whenRendered_thenDarktOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.DARK))
        composeRule.onNodeWithTag(settingsThemeOption("DARK")).assertIsSelected()
    }

    @Test
    fun givenSystemTheme_whenRendered_thenSystemOptionIsSelected() {
        createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM))
        composeRule.onNodeWithTag(settingsThemeOption("SYSTEM")).assertIsSelected()
    }

    @Test
    fun givenSettingsRendered_whenBackClicked_thenEmitBackCallback() {
        var backClicked = false
        createSettingsScreen(onBack = {backClicked = true})
        composeRule.onNodeWithTag(TOP_APP_BAR).performClick()
        //Then
        assertTrue(backClicked)
    }

    @Test
    fun givenInStockSwitchOff_whenClicked_thenEmitsTrue() {
        var emitted: Boolean? = null
        createSettingsScreen(
            uiState = SettingsUiState(inStockOnly = false),
            onInStockOnlyChange = { newState -> emitted = newState })
        composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).performClick()
        //Then
        assertEquals(true, emitted)
    }

    @Test
    fun givenLightTheme_whenDarkClicked_thenEmitDarkTheme() {
        var selectedTheme: ThemeMode? = null
        createSettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.LIGHT),
            onThemeModeSelected = { themeMode -> selectedTheme = themeMode })
        composeRule.onNodeWithTag(settingsThemeOption("DARK")).performClick()
        //Then
        assertEquals(ThemeMode.DARK, selectedTheme)
    }
}