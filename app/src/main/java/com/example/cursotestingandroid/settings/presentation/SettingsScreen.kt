package com.example.cursotestingandroid.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.domain.model.ThemeMode
import com.example.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_CONTENT
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_IN_STOCK_SWITCH
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.SETTINGS_TAX_SWITCH
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.settingsThemeOption

@Preview
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onInStockOnlyChange = { newState ->
            settingsViewModel.setInStockOnly(newState)
        },
        onThemeModeSelected = { newThemeMode ->
            settingsViewModel.setThemeMode(newThemeMode)
        },
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onInStockOnlyChange: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    Scaffold(
        topBar = {
            MarketTopAppBar(
                title = stringResource(R.string.settings_screen_top_app_bar_title),
                onBackSelected = { onBack() },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(SETTINGS_CONTENT)
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            text = stringResource(R.string.settings_screen_filters_and_visualization),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.settings_screen_only_in_stock_products),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.settings_screen_only_in_stock_products_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Switch(
                            modifier = Modifier.testTag(SETTINGS_IN_STOCK_SWITCH),
                            checked = uiState.inStockOnly,
                            onCheckedChange = { newState ->
                                onInStockOnlyChange(newState)
                            },
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.settings_screen_show_taxes_included),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.settings_screen_show_taxes_included_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Switch(
                            modifier = Modifier.testTag(SETTINGS_TAX_SWITCH),
                            checked = true,
                            onCheckedChange = {},
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            text = stringResource(R.string.settings_screen_appearance),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_screen_app_theme),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(R.string.settings_screen_app_theme_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SegmentedButton(
                                modifier = Modifier.testTag(settingsThemeOption("SYSTEM")),
                                shape = SegmentedButtonDefaults.itemShape(0, 3),
                                onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                                selected = uiState.themeMode == ThemeMode.SYSTEM,
                                label = { Text(stringResource(R.string.settings_screen_theme_system)) },
                            )
                            SegmentedButton(
                                modifier = Modifier.testTag(settingsThemeOption("LIGHT")),
                                shape = SegmentedButtonDefaults.itemShape(1, 3),
                                onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                                selected = uiState.themeMode == ThemeMode.LIGHT,
                                label = { Text(stringResource(R.string.settings_screen_theme_light)) },
                            )
                            SegmentedButton(
                                modifier = Modifier.testTag(settingsThemeOption("DARK")),
                                shape = SegmentedButtonDefaults.itemShape(2, 3),
                                onClick = { onThemeModeSelected(ThemeMode.DARK) },
                                selected = uiState.themeMode == ThemeMode.DARK,
                                label = { Text(stringResource(R.string.settings_screen_theme_dark)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingContentPreview() {
    SettingsContent(
        uiState = SettingsUiState(),
        onBack = {},
        onInStockOnlyChange = {},
        onThemeModeSelected = {},
    )
}
