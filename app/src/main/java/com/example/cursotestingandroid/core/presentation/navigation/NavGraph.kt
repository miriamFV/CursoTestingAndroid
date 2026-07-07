package com.example.cursotestingandroid.core.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cursotestingandroid.cart.presentation.CartScreen
import com.example.cursotestingandroid.productdetail.presentation.ProductDetailScreen
import com.example.cursotestingandroid.productlist.presentation.ProductListScreen
import com.example.cursotestingandroid.settings.presentation.SettingsScreen

@Composable
fun NavGraph() {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(Screen.ProductList)
    val entries = entryProvider<NavKey>{
        entry<Screen.ProductList>{
            ProductListScreen(
                navigateToSettings = { backStack.add(Screen.Settings) },
                navigateToProductDetail = { productId ->
                    backStack.add(
                        Screen.ProductDetail(
                            productId
                        )
                    )
                },
                navigateToCart = {
                    backStack.add(Screen.Cart)
                }
            )
        }
        entry<Screen.Cart>{
            CartScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<Screen.Settings>{
            SettingsScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<Screen.ProductDetail> { route ->
            ProductDetailScreen(
                productId = route.productId,
                onBack = { backStack.removeLastOrNull() })
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entries,
        onBack = {backStack.removeLastOrNull()}
    )
}