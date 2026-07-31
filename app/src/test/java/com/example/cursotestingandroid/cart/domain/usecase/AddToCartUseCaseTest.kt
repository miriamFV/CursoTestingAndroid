package com.example.cursotestingandroid.cart.domain.usecase

import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.builders.product
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddToCartUseCaseTest {
    @Test
    fun givenZeroQuantity_whenInvoke_thenThrowsQuantityMustBePositiveError() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val fakeProductRepository = FakeProductRepository()
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            val quantity = 0
            // When
            val exception = runCatching { useCase(productId = "id", quantity = quantity) }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun givenNegativeQuantity_whenInvoke_thenThrowsQuantityMustBePositiveError() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val fakeProductRepository = FakeProductRepository()
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            val quantity = -1
            // When
            val exception = runCatching { useCase(productId = "id", quantity = quantity) }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun givenNoneExistingProduct_whenInvoke_thenThrowsNotFoundError() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val fakeProductRepository = FakeProductRepository().apply { setProducts(emptyList()) }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            // When
            val exception = runCatching { useCase(productId = "id", quantity = 1) }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.NotFoundError)
        }

    @Test
    fun givenInsufficientStock_whenInvoke_thenThrowsInsufficientStockError() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(2)
                }
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            // When
            val exception = runCatching { useCase(productId = productId, quantity = 3) }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.Validation.InsufficientStock)
            assertEquals(2, (exception as AppError.Validation.InsufficientStock).available)
        }

    @Test
    fun givenProductWithSufficientStock_whenInvoke_thenProductIsAddedCorrectly() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            // When
            useCase(productId = productId, quantity = 3)
            // Then
            val items = fakeCartRepository.getCartItems().first()
            assertEquals(productId, items.first().productId)
            assertEquals(3, items.first().quantity)
        }

    @Test
    fun givenDefaultQuantityInUseCase_whenInvoke_thenOneProductIsAddedCorrectly() =
        runTest {
            // Given
            val fakeCartRepository = FakeCartRepository()
            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            val fakeProductRepository =
                FakeProductRepository().apply {
                    setProducts(listOf(product))
                }
            val useCase = AddToCartUseCase(fakeCartRepository, fakeProductRepository)
            // When
            useCase(productId = productId)
            // Then
            val items = fakeCartRepository.getCartItems().first()
            assertEquals(productId, items.first().productId)
            assertEquals(1, items.first().quantity)
        }

    @Test
    fun givenZeroQuantity_whenInvoke_thenAnyRepositoryIsCalled() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartRepository>()
            val useCase = AddToCartUseCase(cartRepository, productRepository)
            val quantity = 0
            // When
            val exception = runCatching { useCase(productId = "id", quantity = quantity) }.exceptionOrNull()
            // Then
            coVerify(exactly = 0) { productRepository.getProductById(any()) }
            coVerify(exactly = 0) { cartRepository.getCartItemById(any()) }
            coVerify(exactly = 0) { cartRepository.addToCart(any(), any()) }
        }

    @Test
    fun givenValidProduct_whenInvoke_thenAddToCartIsCalledWithExpectedValues() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartRepository>()

            val productId = "id-test-1"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            coEvery { productRepository.getProductById(productId) } returns flowOf(product)
            coEvery { cartRepository.getCartItemById(productId) } returns null
            coEvery { cartRepository.addToCart(productId, 3) } just runs

            val useCase = AddToCartUseCase(cartRepository, productRepository)

            // When
            useCase(productId = productId, quantity = 3)

            // Then
            coVerify(exactly = 1) { productRepository.getProductById(productId) }
            coVerify(exactly = 1) { cartRepository.getCartItemById(productId) }
            coVerify(exactly = 1) { cartRepository.addToCart(productId, 3) }
        }
}
