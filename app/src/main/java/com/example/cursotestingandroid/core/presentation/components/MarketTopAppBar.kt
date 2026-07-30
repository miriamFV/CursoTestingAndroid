package com.example.cursotestingandroid.core.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_APP_BAR_BACK
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.TOP_BAR_TITLE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketTopAppBar(modifier: Modifier = Modifier, title: String, onBackSelected: () -> Unit) {

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                modifier = Modifier.testTag(TOP_BAR_TITLE),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(
                modifier = Modifier.testTag(TOP_APP_BAR_BACK),
                onClick = { onBackSelected() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}