package com.example.cursotestingandroid.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.core.mockwebserver.MarketApiDispatcher
import com.example.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.example.cursotestingandroid.core.mockwebserver.ProductErrorDispatcher
import com.example.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.example.cursotestingandroid.core.utils.asAsset
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFailsWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OfflineFirstIntegrationTest {

    companion object{
        const val DEFAULT_PRODUCT_ASSET = "product_list_default.json"
        const val UPDATED_PRODUCT_ASSET = "product_list_updated.json"
        const val DEFAULT_PRODUCT_SIZE = 3
        const val UPDATED_PRODUCT_SIZE = 1
    }

    @get:Rule(order = 0)
    val mockWebServerRule= MockWebServerRule()

    @get:Rule(order = 1)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: MarketDatabase

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() = runTest {
        hiltAndroidRule.inject()
        database.clearAllTables()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = "http://localhost:8080/"
    }

    @Test
    fun givenSuccessfullRefresh_whenGetProducts_thenRoomContainsRemoteProducts() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCT_ASSET)
        productRepository.refreshProduct()
        val cachedProducts = productRepository.getProducts().first{ products ->
            products.size == DEFAULT_PRODUCT_SIZE //Wait until out cachedProduct size would be 3
        }
        assertEquals(DEFAULT_PRODUCT_SIZE, cachedProducts.size)
    }

    @Test
    fun givenEmptyCacheAndFailedRefresh_whenGetProducts_thenEmitsEmptyList() = runTest {
        serveProductsError()
//        val result = runCatching {  productRepository.refreshProduct()}
//        assertTrue(result.isFailure)

        assertFailsWith<AppError.NetworkError>{productRepository.refreshProduct()}
        val products = productRepository.getProducts().first { it.isEmpty() }
        assertTrue(products.isEmpty())
    }

    @Test
    fun givenCachedProductsAndFailedRefresh_whenGetProducts_thenReturnsPreviousCache() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCT_ASSET)
        productRepository.refreshProduct()
        productRepository.getProducts().first{ products ->
            products.size == DEFAULT_PRODUCT_SIZE
        }
        serveProductsError()
        assertFailsWith<AppError.NetworkError>{productRepository.refreshProduct()}
        val cachedProducts = productRepository.getProducts().first{ products ->
            products.size == DEFAULT_PRODUCT_SIZE
        }
        assertEquals(DEFAULT_PRODUCT_SIZE, cachedProducts.size)
    }

    @Test
    fun givenCachedProducts_whenRefreshWhenNewPayLoad_thenContainsOnlyLatestProducts() = runTest {
        serveProductsFromAsset(DEFAULT_PRODUCT_ASSET)
        productRepository.refreshProduct()
        productRepository.getProducts().first{ products ->
            products.size == DEFAULT_PRODUCT_SIZE
        }

        serveProductsFromAsset(UPDATED_PRODUCT_ASSET)
        productRepository.refreshProduct()
        val updatedProducts = productRepository.getProducts().first { products ->
            products.size == UPDATED_PRODUCT_SIZE
        }

        assertEquals(UPDATED_PRODUCT_SIZE, updatedProducts.size)
        assertEquals("updated-p1", updatedProducts.first().id)
        assertEquals("Pan integral", updatedProducts.first().name)
    }

    private fun serveProductsFromAsset(assetName: String) {
        mockWebServerRule.server.dispatcher = MarketApiDispatcher(
            productJson = assetName.asAsset()
        )
    }

    private fun serveProductsError(){
        mockWebServerRule.server.dispatcher = ProductErrorDispatcher()
    }

}