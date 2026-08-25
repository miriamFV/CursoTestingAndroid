package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.cart.domain.ex.activeAt
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetCartItemsWithPromotionsUseCase
    @Inject
    constructor(
        private val cartRepository: CartRepository,
        private val productRepository: ProductRepository,
        private val promotionRepository: PromotionRepository,
        private val getPromotionForProduct: GetPromotionForProduct,
        private val clock: Clock,
    ) {
        operator fun invoke(): Flow<List<CartItemWithPromotion>> {
            return cartRepository.getCartItems().flatMapLatest { cartItems ->
                val ids = cartItems.mapTo(mutableSetOf()) { it.productId }
                if (ids.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        productRepository.getProductsByIds(ids),
                        promotionRepository.getActivePromotions(),
                    ) { products, promotions ->
                        val now = clock.now()
                        val activePromotions = promotions.activeAt(now)
                        val productsById = products.associateBy { it.id }
                        cartItems.mapNotNull { cartItem ->
                            val product = productsById[cartItem.productId] ?: return@mapNotNull null
                            val promotion = getPromotionForProduct(product, activePromotions)
                            val productWithPromotion = ProductWithPromotion(product, promotion)
                            CartItemWithPromotion(cartItem = cartItem, item = productWithPromotion)
                        }
                    }
                }
            }
        }
    }
