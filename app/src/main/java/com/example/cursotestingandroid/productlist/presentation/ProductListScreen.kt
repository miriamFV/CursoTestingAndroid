package com.example.cursotestingandroid.productlist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.example.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.example.cursotestingandroid.productlist.presentation.components.ProductItem

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()

    val filtersVisible by productListViewModel.filtersVisible.collectAsStateWithLifecycle()

    val snackbarHostState = remember{ SnackbarHostState() }

    LaunchedEffect(Unit){
        productListViewModel.events.collect{ event ->
            when(event){
                is ProductListEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                filtersVisible = filtersVisible,
                onFiltersSelected = { showFilters -> productListViewModel.setFiltersVisible(showFilters) },
                onSettingsSelected = { }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when(val state = uiState){
            is ProductListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            is ProductListUiState.Error ->  {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues),
                    contentAlignment = Alignment.Center){
                    Text("Error: ${state.message}", fontSize = 30.sp, color = Color.Red)
                }
            }
            is ProductListUiState.Success ->  {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                ) {
                    AnimatedVisibility(
                        visible = filtersVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FiltersMenu(
                            state = state,
                            onCategorySelected = { category ->
                                productListViewModel.setCategory(
                                    category
                                )
                            },
                            onSortedSelected = { sortOption ->
                                productListViewModel.setSortOption(
                                    sortOption
                                )
                            }
                        )
                    }
                    Text(
                        text = stringResource(
                            id = R.string.product_list_screen_products_quantity,
                            state.products.size
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if(state.products.isEmpty()){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🔍", style = MaterialTheme.typography.displayMedium)
                                Text(
                                    text = stringResource(R.string.product_list_screen_no_products),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }else{
                        LazyColumn {
                            items(state.products) { item: ProductWithPromotion ->
                                ProductItem(item = item, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}
