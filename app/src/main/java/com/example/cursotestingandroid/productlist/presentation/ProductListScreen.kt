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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursotestingandroid.R
import com.example.cursotestingandroid.cart.presentation.CartUiState
import com.example.cursotestingandroid.cart.presentation.CartViewModel
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LIST
import com.example.cursotestingandroid.core.presentation.testing.UiTestTag.PRODUCT_LIST_LOADING
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.model.SortOption
import com.example.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.example.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.example.cursotestingandroid.productlist.presentation.components.ProductItem


@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    navigateToSettings: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToCart: () -> Unit
) {
    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember{ SnackbarHostState() }
    val filterVisible by productListViewModel.filterVisible.collectAsStateWithLifecycle()

    LaunchedEffect(Unit){
        productListViewModel.events.collect{ event ->
            when(event){
                is ProductListEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val cartItemCount = remember(cartUiState) {
        when (val state = cartUiState) {
            is CartUiState.Success -> {
                state.cartItems.sumOf { it.cartItem.quantity }
            }
            else -> 0
        }
    }

    ProductListContent(
        uiState = uiState,
        cartItemCount = cartItemCount,
        filterVisible = filterVisible,
        snackbarHostState = snackbarHostState,
        onFiltersSelected = { productListViewModel.setFiltersVisible(it) },
        onCategorySelected = { category -> productListViewModel.setCategory(category) },
        onSortOptionSelected = { sortOption -> productListViewModel.setSortOption(sortOption) },
        onSettingsSelected = navigateToSettings,
        onProductSelected = { productWithPromotion -> navigateToProductDetail(productWithPromotion.product.id) },
        onCartSelected = navigateToCart
    )
}

@Composable
fun ProductListContent(
    uiState: ProductListUiState,
    cartItemCount: Int,
    filterVisible: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onFiltersSelected: (Boolean) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onSettingsSelected: () -> Unit,
    onProductSelected: (ProductWithPromotion) -> Unit,
    onCartSelected: () -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopAppBar(
                filtersVisible = filterVisible,
                cartItemCount = cartItemCount,
                onFiltersSelected = onFiltersSelected,
                onSettingsSelected = onSettingsSelected,
                onCartSelected = { onCartSelected() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when(uiState){
            is ProductListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator(modifier = Modifier.testTag(PRODUCT_LIST_LOADING))
                }
            }
            is ProductListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.message}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ProductListUiState.Success ->  {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                ) {
                    AnimatedVisibility(
                        visible = filterVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FiltersMenu(
                            state = uiState,
                            onCategorySelected = onCategorySelected,
                            onSortedSelected = onSortOptionSelected
                        )
                    }
                    Text(
                        text = stringResource(
                            id = R.string.product_list_screen_products_quantity,
                            uiState.products.size
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if(uiState.products.isEmpty()){
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
                        LazyColumn(modifier = Modifier.testTag(PRODUCT_LIST_LIST)) {
                            items(uiState.products) { item: ProductWithPromotion ->
                                ProductItem(
                                    item = item,
                                    onClick = onProductSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ProductListContentPreview() {
    ProductListContent(
        uiState = ProductListUiState.Loading,
        cartItemCount = 0,
        filterVisible = true,
        onFiltersSelected = {},
        onCategorySelected = {},
        onSortOptionSelected = {},
        onSettingsSelected = {},
        onProductSelected = {},
        onCartSelected = {}
    )
}
