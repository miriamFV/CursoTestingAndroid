package com.example.cursotestingandroid.productlist.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.cursotestingandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    filtersVisible: Boolean = true,
    onFiltersSelected: (Boolean) -> Unit,
    onSettingsSelected: () -> Unit
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
            IconButton(onClick = { onFiltersSelected(!filtersVisible) }) {
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
            IconButton(onClick = { onSettingsSelected() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.product_list_screen_top_app_bar_settings),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}