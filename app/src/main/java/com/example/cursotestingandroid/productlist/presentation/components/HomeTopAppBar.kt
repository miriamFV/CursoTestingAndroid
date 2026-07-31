package com.example.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_BADGE
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_CART_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_FILTERS_BUTTON
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_SETTINGS_BUTTON

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    filtersVisible: Boolean = true,
    cartItemCount: Int,
    onFiltersSelected: (Boolean) -> Unit,
    onSettingsSelected: () -> Unit,
    onCartSelected: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.product_list_screen_top_app_bar_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_FILTERS_BUTTON),
                onClick = { onFiltersSelected(!filtersVisible) }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (filtersVisible) {
                        stringResource(R.string.product_list_screen_top_app_bar_hide_filters)
                    } else {
                        stringResource(R.string.product_list_screen_top_app_bar_show_filters)
                    },
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_SETTINGS_BUTTON),
                onClick = { onSettingsSelected() }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.product_list_screen_top_app_bar_settings),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            BadgedBox(modifier = Modifier.padding(end = 4.dp), badge = {
                if (cartItemCount > 0) {
                    Badge(modifier = Modifier.testTag(TOP_APP_BAR_BADGE)) {
                        Text(
                            text = if (cartItemCount > 99) {
                                stringResource(R.string.product_list_screen_top_app_bar_cart_max_count)
                            } else {
                                cartItemCount.toString()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = Bold
                        )
                    }
                }
            }) {
                IconButton(
                    modifier = Modifier.testTag(TOP_APP_BAR_CART_BUTTON),
                    onClick = { onCartSelected() }) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = stringResource(R.string.product_list_screen_top_app_bar_cart),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    )
}