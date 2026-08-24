package com.example.cursotestingandroid.checkout.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * EXAMEN — Tests de INTEGRACIÓN del repositorio de pedidos.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [OrderRepositoryImpl] sobre RemoteDataSource real + MockWebServer
 * (200 -> OrderConfirmation mapeada; 404 -> AppError.NotFoundError; red caída -> AppError.NetworkError).
 * Pistas: encola respuestas en `mockWebServer.server`, inyecta con Hilt (`hilt.inject()`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OrderRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var orderRepository: OrderRepository

    @Before
    fun setUp() =
        runTest {
            hilt.inject()
        }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    private fun readJson(fileName: String): String {
        val context =
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .context
        return context.assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }
    }

    @Test
    fun givenSuccessfulResponse_whenPlaceOrder_thenReturnsOrderConfirmation() {
        runTest {
            // GIVEN
            val json = readJson("order_confirmation.json")
            mockWebServer.server.enqueue(MockResponse().setBody(json).setResponseCode(200))
            // WHEN
            val orderConfirmation = orderRepository.placeOrder()
            // THEN
            assertEquals("ORD-1001", orderConfirmation.orderId)
            assertEquals(130, orderConfirmation.etaMinutes)
            assertEquals(0.0, orderConfirmation.total)
        }
    }

    @Test(expected = AppError.NotFoundError::class)
    fun given404Response_whenPlaceOrder_thenThrowsNotFoundError() {
        runTest {
            // GIVEN
            mockWebServer.server.enqueue(MockResponse().setResponseCode(404))
            // WHEN
            orderRepository.placeOrder()
        }
    }

    @Test(expected = AppError.NetworkError::class)
    fun givenNetworkFailure_whenPlaceOrder_thenThrowsNetworkError() {
        runTest {
            // GIVEN
            mockWebServer.server.enqueue(MockResponse().setResponseCode(500))
            // WHEN
            orderRepository.placeOrder()
        }
    }
}
