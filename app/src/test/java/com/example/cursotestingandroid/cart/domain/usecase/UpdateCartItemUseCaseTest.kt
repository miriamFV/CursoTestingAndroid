package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCartItemUseCaseTest {
    @Test
    fun givenNegativeQuantity_whenInvoke_thenThrowsQuantityMustBePositiveError() =
        runTest {
            // Given
            val productId = "productId-1"
            val useCase = UpdateCartItemUseCase(FakeCartRepository(), FakeProductRepository())
            val quantityToUpdate = -1

            // When
            val exception = runCatching { useCase(productId, quantityToUpdate) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun givenZeroQuantity_whenInvoke_thenProductIsRemovedFromCart() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(2)
                }
            val fakeCartRepository =
                FakeCartRepository().apply { setCartItems(listOf(cartItem)) }
            val fakeProductRepository = FakeProductRepository().apply { setProducts(listOf(product)) }
            val useCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)
            val quantityToUpdate = 0

            // When
            useCase(productId, quantityToUpdate)

            // Then
            assertNull(fakeCartRepository.getCartItemById(productId))
            assertEquals(0, fakeCartRepository.getCartItems().first().size)
        }

    @Test
    fun givenProductNotFound_whenInvoke_thenThrowsNotFoundError() =
        runTest {
            // Given
            val productId = "productId-1"
            val fakeProductRepository = FakeProductRepository().apply { setProducts(emptyList()) }
            val useCase = UpdateCartItemUseCase(FakeCartRepository(), fakeProductRepository)
            val quantityToUpdate = 2

            // When
            val exception = runCatching { useCase(productId, quantityToUpdate) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.NotFoundError)
        }

    @Test
    fun givenQuantityHigherThanStock_whenInvoke_thenThrowsInsufficientStockError() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                    withStock(1)
                }
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }
            val useCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)
            val quantityToUpdate = 5

            // When
            val exception = runCatching { useCase(productId, quantityToUpdate) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.InsufficientStock)
        }

    @Test
    fun givenQuantityLowerThanStock_whenInvoke_thenCartIsUpdated() =
        runTest {
            // Given
            val productId = "productId-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(listOf(cartItem)) }
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)
            val quantityToUpdate = 5

            // When
            useCase(productId, quantityToUpdate)

            // Then
            val items = fakeCartRepository.getCartItems().first()
            assertEquals(1, items.size)
            assertEquals(6, items.first().quantity)
        }
}
