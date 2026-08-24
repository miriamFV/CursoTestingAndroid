package com.example.cursotestingandroid.checkout.domain.usecase

import com.example.cursotestingandroid.core.builders.cartItem
import com.example.cursotestingandroid.core.fakes.FakeCartRepository
import com.example.cursotestingandroid.core.fakes.FakeOrderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del caso de uso de realizar pedido.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [PlaceOrderUseCase] (éxito vacía el carrito; fallo NO lo vacía).
 * Pista: necesitarás un fake de OrderRepository y FakeCartItemRepository.
 */
class PlaceOrderUseCaseTest {
    @Test
    fun `given successful order when invoke then returns success and clears cart`() {
        runTest {
            // GIVEN
            val cart = listOf(cartItem { withProductId("p1") })
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeOrderRepository = FakeOrderRepository()
            // WHEN
            val useCase = PlaceOrderUseCase(fakeOrderRepository, fakeCartRepository)
            val result = useCase()
            // THEN
            assertTrue(result.isSuccess)
            assertEquals(fakeOrderRepository.orderConfirmation, result.getOrNull())
            assertEquals(0, fakeCartRepository.getCartItems().first().size) // clears cart
        }
    }

    @Test
    fun `given repository throws when invoke then returns failure`() {
        runTest {
            // GIVEN
            val cart = listOf(cartItem { withProductId("p1") })
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeOrderRepository = FakeOrderRepository().apply { returnError = true }

            // WHEN
            val useCase = PlaceOrderUseCase(fakeOrderRepository, fakeCartRepository)
            val result = useCase()

            // THEN
            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `given repository throws when invoke then does not clear cart`() {
        runTest {
            // GIVEN
            val cart = listOf(cartItem { withProductId("p1") })
            val fakeCartRepository = FakeCartRepository().apply { setCartItems(cart) }
            val fakeOrderRepository = FakeOrderRepository().apply { returnError = true }
            // WHEN
            val useCase = PlaceOrderUseCase(fakeOrderRepository, fakeCartRepository)
            val result = useCase()

            // THEN
            assertEquals(1, fakeCartRepository.getCartItems().first().size) // clear cart does not work
        }
    }
}
