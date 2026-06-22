package com.example.cursotestingandroid.productlist.domain.usecase

import com.example.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val getPromotionForProduct: GetPromotionForProduct
) {

    operator fun invoke(): Flow<List<ProductWithPromotion>> {
        return combine(
            productRepository.getProducts(), promotionRepository.getActivePromotions()
        ) { products, promotions ->

            val now = Instant.now()
            val activePromotions = promotions.filter { promotion ->
                promotion.startTime <= now && now <= promotion.endTime
            }

            products.map { product ->

                val promotion = getPromotionForProduct(product, activePromotions)

                ProductWithPromotion(
                    product = product,
                    promotion = promotion
                )
            }
        }
    }

}