package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.cart.domain.ex.activeAt
import com.example.cursotestingandroid.cart.domain.model.CartItem
import com.example.cursotestingandroid.cart.domain.model.CartSummary
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.productlist.domain.model.Product
import com.example.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.example.cursotestingandroid.productlist.domain.model.Promotion
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

class GetCartSummaryUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct,
    private val clock: Clock
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CartSummary> {
        return cartRepository.getCartItems()
            .flatMapLatest { cartItems ->
                val ids = cartItems.mapTo(mutableSetOf()) { it.productId }
                if (ids.isEmpty()) {
                    flowOf(CartSummary(0.0, 0.0, 0.0))
                } else {
                    combine(
                        productRepository.getProductsByIds(ids),
                        promotionRepository.getActivePromotions()
                    ) { products, promotions ->
                        calculateSummary(cartItems, products, promotions, clock)
                    }
                }
            }
    }

    private fun calculateSummary(
        cartItems: List<CartItem>,
        products: List<Product>,
        promotions: List<Promotion>,
        clock: Clock
    ): CartSummary {
        val now = clock.now()
        val activePromotions = promotions.activeAt(now)

        val productsById = products.associateBy { it.id }
        var subtotal = 0.0
        var discountTotal = 0.0

        for (cartItem in cartItems) {
            val product = productsById[cartItem.productId] ?: continue
            val itemTotal = product.price * cartItem.quantity
            subtotal += itemTotal

            discountTotal += calculateDiscountForProduct(
                product = product,
                quantity = cartItem.quantity,
                activePromotions = activePromotions
            )
        }
        val total = (subtotal - discountTotal).coerceAtLeast(0.0)
        return CartSummary(subtotal = subtotal, discountTotal = discountTotal, finalTotal = total)
    }

    private fun calculateDiscountForProduct(
        product: Product,
        quantity: Int,
        activePromotions: List<Promotion>
    ): Double {

        val selectedPromotion = getPromotionForProduct(product, activePromotions)

        return when (selectedPromotion) {
            is ProductPromotion.BuyXPayY -> {
                val buy = selectedPromotion.buy
                val pay = selectedPromotion.pay
                val freePerGroup = (buy - pay).coerceAtLeast(0)
                val groups = quantity / buy
                val freeItems = freePerGroup * groups
                product.price * freeItems
            }

            is ProductPromotion.Percent -> {
                val itemSubTotal = product.price * quantity
                itemSubTotal * (selectedPromotion.percent / 100)
            }

            null -> 0.0
        }
    }
}