package com.example.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    @get:Rule(order = 0)
    val mockWebServerRule= MockWebServerRule()

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp(){
        hiltAndroidRule.inject()
    }

    @After
    fun tearDown(){
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    private val productsJson = """
        {"products": [
            {
            "id": "p1",
            "name": "Leche",
            "description": "Leche Entera 1L",
            "category": "Dairy",
            "priceCents": 120,
            "stock": 0,
            "imageUrl": "https://images.unsplash.com/photo-1580910051074-3eb694886505"
            },
            {
            "id": "p2",
            "name": "Huevos",
            "description": "Huevos Camperos (12u)",
            "category": "Dairy",
            "priceCents": 310,
            "stock": 8,
            "imageUrl": "https://images.unsplash.com/photo-1587486913049-53fc88980cfc"
            }]
        }""".trimIndent()

    @Test
    fun givenValidProductsJson_whenRefreshIsCalled_thenDatabaseEmitProductsFromRoom() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

        //When
        productRepository.refreshProduct()

        //Then
        val products = productRepository.getProducts().first()
        assertTrue(products.isNotEmpty())
        assertTrue(products.size == 2)
        assertEquals("Leche", products.find { it.id == "p1" }?.name)
    }

    @Test
    fun givenEmptyProductsJson_whenRefreshIsCalled_thenGetProductsEmitsEmptyList() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody("""{"products":[]}""").setResponseCode(200))

        //When
        productRepository.refreshProduct()

        //Then
        val products = productRepository.getProducts().first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun givenProductsJson_whenRefreshAndGetProductById_thenReturnsCorrectProduct() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

        //When
        productRepository.refreshProduct()
        val product = productRepository.getProductById("p2").first()

        //Then
        assertNotNull(product)
        assertEquals("Huevos", product?.name)
    }


    @Test(expected = Exception::class) //Then
    fun givenServerReturns500_whenRefreshIsCalled_thenThrowException() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(500))
        //When
        productRepository.refreshProduct()
    }

    @Test
    fun givenCachedProducts_whenRefreshWithNewProducts_thenFlowEmitsUpdatedData() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
        productRepository.refreshProduct()

        //When
        val newProductsJson = """
        {"products": [
            {"id": "p1", "name": "Pan", "description": "Pan integral", "category": "Bread", "priceCents": 200, "stock": 5, "imageUrl": "https://images.unsplash.com/photo-1580910051074-3eb694886505"}
        ]}""".trimIndent()
        mockWebServerRule.server.enqueue(MockResponse().setBody(newProductsJson).setResponseCode(200))
        productRepository.refreshProduct()

        val products = productRepository.getProducts().first()

        //Then
        assertEquals("Pan", products.find { it.id == "p1" }?.name)
        assertEquals(2.0, products.find { it.id == "p1" }?.price)
    }

    @Test
    fun givenProductsEndpoint_whenRefreshIsCalled_thenRequestIsGetToCorrectPath() = runTest {
        //Given
        mockWebServerRule.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

        //When
        productRepository.refreshProduct()

        //Then
        val request = mockWebServerRule.server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("data/products.json") == true)
    }

}