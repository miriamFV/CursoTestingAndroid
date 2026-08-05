package com.example.cursotestingandroid.cart.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var cartRepository: CartRepository

    @Before
    fun setUp() =
        runTest {
            hilt.inject()
            cartRepository.clearCart()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    // Add item to cart

    @Test
    fun givenEmptyCart_whenAddToCart_thenGetCartItemsContainsItem() =
        runTest {
            // When
            cartRepository.addToCart("p1", 2)
            // Then
            val result = cartRepository.getCartItems().first()
            assertEquals(1, result.size)
            assertEquals("p1", result.first().productId)
            assertEquals(2, result.first().quantity)
        }

    @Test
    fun givenCartWithProducts_whenAddAnExistingItem_thenQuantityIsUpdated() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 1)
            // When
            cartRepository.addToCart("p1", 3)
            // Then
            val updatedCartState = cartRepository.getCartItems().first()
            assertEquals(1, updatedCartState.size)
            assertEquals(4, (updatedCartState.find { it.productId == "p1" })?.quantity)
        }

    // Get cart items

    @Test
    fun givenEmptyCart_whenGetCartItems_thenEmitsEmptyList() =
        runTest {
            // When
            val result = cartRepository.getCartItems().first()
            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun givenCartWithProducts_whenGetCartItems_thenReturnsCorrectProducts() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 4)
            // When
            val result = cartRepository.getCartItems().first()
            // Then
            assertEquals(2, result.size)
            assertEquals(2, (result.find { it.productId == "p1" })?.quantity)
            assertEquals(4, (result.find { it.productId == "p2" })?.quantity)
        }

    @Test
    fun givenCartWithProducts_whenGetCartItemById_thenReturnsCorrectProduct() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 4)
            // When
            val item = cartRepository.getCartItemById("p2")
            // Then
            assertNotNull(item)
            assertEquals("p2", item?.productId)
            assertEquals(4, item?.quantity)
        }

    // Update cart

    @Test
    fun givenCartWithProducts_whenUpdateQuantity_thenQuantityIsUpdated() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 1)
            // When
            cartRepository.updateQuantity("p1", 4)
            // Then
            val updatedCartState = cartRepository.getCartItems().first()
            assertEquals(1, updatedCartState.size)
            assertEquals(4, (updatedCartState.find { it.productId == "p1" })?.quantity)
        }

    @Test
    fun givenCartWithProducts_whenUpdateNonExistingItem_thenThrowsNotFoundError() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 1)
            // When
            val exception = runCatching { cartRepository.updateQuantity("p3", 3) }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.NotFoundError)
        }

    // Remove from cart

    @Test
    fun givenCartWithProducts_whenRemoveAnExistingItem_thenCartIsUpdated() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 1)
            // When
            cartRepository.removeFromCart("p2")
            // Then
            val cart = cartRepository.getCartItems().first()
            assertEquals(1, cart.size)
            assertTrue(cart.none { it.productId == "p2" })
        }

    @Test
    fun givenItemInCart_whenRemoveFromCart_thenCartIsEmpty() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            // When
            cartRepository.removeFromCart("p1")
            // Then
            val cart = cartRepository.getCartItems().first()
            assertTrue(cart.isEmpty())
        }

    @Test(expected = AppError.NotFoundError::class)
    fun givenEmptyCart_whenRemoveFromCart_thenThrowsNotFound() =
        runTest {
            cartRepository.removeFromCart("p1")
        }

    @Test
    fun givenCartWithProducts_whenRemoveNonExistingItem_thenThrowsNotFoundError() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 1)
            // When
            val exception = runCatching { cartRepository.removeFromCart("p3") }.exceptionOrNull()
            // Then
            assertTrue(exception is AppError.NotFoundError)
        }

    // Clear cart

    @Test
    fun givenCartWithProducts_whenClearCart_thenCartIsEmpty() =
        runTest {
            // Given
            cartRepository.addToCart("p1", 2)
            cartRepository.addToCart("p2", 1)
            // When
            cartRepository.clearCart()
            // Then
            val cart = cartRepository.getCartItems().first()
            assertTrue(cart.isEmpty())
        }
}
