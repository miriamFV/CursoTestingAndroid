package com.example.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.cursotestingandroid.core.builders.productEntity
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: MarketDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarketDatabase::class.java
        ).build()
        productDao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllProducts_thenEmitsEmptyList() = runTest {
        //Given --> Executed in setUp()
        //When
        val products = productDao.getAllProducts().first()
        //Then
        assertTrue(products.isEmpty())
    }

    @Test
    fun givenInsertedProduct_whenGetProductById_thenReturnsProduct() = runTest {
        //Given
        val productId = "productId"
        val productEntity = productEntity{ withId(productId) }
        productDao.insertProducts(listOf(productEntity))
        //When
        val product = productDao.getProductById(productId).first()
        //Then
        assertNotNull(product)
        assertEquals(productId, product?.id)
    }

    @Test
    fun givenInsertedProducts_whenGetProductsByIds_thenReturnsRequestSubset() = runTest {
        //Given
        val productEntityList = listOf(
            productEntity { withId("p1") },
            productEntity { withId("p2") },
            productEntity { withId("p3") })
        productDao.insertProducts(productEntityList)
        //When
        val ids = listOf<String>("p1", "p3")
        val products = productDao.getProductsByIds(ids).first()
        //Then
        assertNotNull(products)
        assertEquals(2, products.size)
        assertTrue(products.any { it.id == "p1" })
        assertTrue(products.any { it.id == "p3" })
        assertTrue(products.none { it.id == "p2" })
    }

    @Test
    fun givenOldProducts_whenReplaceAll_thenOnlyNewProductsRemain() = runTest {
        //Given
        val oldProductEntityList = listOf(
            productEntity { withId("old-p1") },
            productEntity { withId("old-p2") })
        productDao.insertProducts(oldProductEntityList)
        //When
        val newProductEntityList = listOf(
            productEntity { withId("new-p1") },
            productEntity { withId("new-p2") },
            productEntity { withId("new-p3") })
        productDao.replaceAll(newProductEntityList)
        //Then
        val newProducts = productDao.getAllProducts().first()
        assertNotNull(newProducts)
        assertEquals(3, newProducts.size)
        assertTrue(newProducts.any { it.id == "new-p1" })
        assertTrue(newProducts.any { it.id == "new-p2" })
        assertTrue(newProducts.any { it.id == "new-p3" })
        assertTrue(newProducts.none { it.id == "old-p1" || it.id == "old-p2" })
    }

    @Test
    fun givenExistingProduct_whenInsertSameIdWithDifferentData_thenReplaceOldData() = runTest {
        //Given
        val productId = "p1"
        val oldProduct = productEntity { withId(productId); withName("pan") }
        productDao.insertProducts(listOf(oldProduct))
        //When
        val newProduct = productEntity { withId(productId); withName("leche") }
        productDao.replaceAll(listOf(newProduct))
        //Then
        val product = productDao.getProductById(productId).first()
        assertNotNull(product)
        assertEquals("leche", product?.name)
    }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitsUpdateList() = runTest {
        productDao.getAllProducts().test{
            val initialValue = awaitItem()
            assertTrue(initialValue.isEmpty())

            productDao.insertProducts(listOf(productEntity { withId("p1") }))

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("p1", updated.first().id)

            cancelAndIgnoreRemainingEvents()
        }
    }

}