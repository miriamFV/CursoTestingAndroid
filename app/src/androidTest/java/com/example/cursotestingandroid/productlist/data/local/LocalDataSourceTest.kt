package com.example.cursotestingandroid.productlist.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cursotestingandroid.core.builders.cartItemEntity
import com.example.cursotestingandroid.core.builders.productEntity
import com.example.cursotestingandroid.core.builders.promotionEntity
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import com.example.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDataSourceTest {

    private lateinit var database: MarketDatabase
    private lateinit var localDataSource: LocalDataSource

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarketDatabase::class.java
        ).build()
        localDataSource =
            LocalDataSource(database.productDao(), database.promotionDao(), database.cartItemDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    //Products
    @Test
    fun givenProducts_whenSaveAndGetAll_thenReturnsPersistedProducts() = runTest {
        //Given
        val products =
            listOf<ProductEntity>(productEntity { withId("p1") }, productEntity { withId("p2") })

        //When
        localDataSource.saveProducts(products)
        val result = localDataSource.getAllProducts().first()

        //Then
        assertEquals(2, result.size)
    }

    @Test
    fun givenSavedProduct_whenGetProductById_thenReturnsCorrectProduct() = runTest {
        //Given
        val productId = "p1"
        val products =
            listOf<ProductEntity>(productEntity { withId("p1"); withName("pan")}, productEntity { withId(productId); withName("leche") })

        //When
        localDataSource.saveProducts(products)
        val result = localDataSource.getProductById(productId).first()

        //Then
        assertNotNull(result)
        assertEquals("leche", result?.name)
    }


    @Test
    fun givenSavedProducts_whenGetProductsByIds_thenReturnsCorrectProducts() = runTest {
        //Given
        val products =
            listOf<ProductEntity>(
                productEntity { withId("p1"); withName("pan") },
                productEntity { withId("p2"); withName("leche") },
                productEntity { withId("p3"); withName("carne") },
                productEntity { withId("p4"); withName("aceite") })
        val idList = setOf<String>("p1", "p4")
        //When
        localDataSource.saveProducts(products)
        val result = localDataSource.getProductsByIds(idList).first()

        //Then
        assertFalse(result.isEmpty())
        assertEquals(2, result.size)
        assertTrue(result.any{it.name == "pan"})
        assertTrue(result.any{it.name == "aceite"})
    }

    //Promotions
    @Test
    fun givenPromotions_whenSaveAndGetAll_thenReturnsPersistedPromotions() = runTest {
        //Given
        val promotions = listOf(
            promotionEntity { withId("promo1") },
            promotionEntity { withId("promo2"); withProductsIds("""["pId1", "pId2"]""") }
        )
        //When
        localDataSource.savePromotions(promotions)
        val result = localDataSource.getAllPromotions().first()

        //Then
        assertEquals(2, result.size)
    }

    //Cart
    @Test
    fun givenCartItem_whenInsertCartItem_thenReturnsSuccessAndItemSaved() = runTest {
        //Given
        val cartItem = cartItemEntity { withProductId("id1"); withQuantity(2) }

        //When
        val result = localDataSource.insertCartItem(cartItem)
        assertTrue(result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        //Then
        assertEquals(1, items.size)
        assertEquals("id1", items.first().productId)
    }

    @Test
    fun givenExistingCartItem_whenUpdateCartItem_thenReturnsSuccessAndCartItemUpdated() = runTest {
        //Given
        val originCartItem = cartItemEntity { withProductId("id1"); withQuantity(2) }
        localDataSource.insertCartItem(originCartItem)

        val updatedCartItem = cartItemEntity { withProductId("id1"); withQuantity(5) }

        //When
        val result = localDataSource.updateCartItem(updatedCartItem)
        assertTrue(result.isSuccess)

        val item = localDataSource.getCartItemById("id1")

        //Then
        assertNotNull(item)
        assertEquals(5, item?.quantity)
    }

    @Test
    fun givenCartItem_whenDeleteCartItem_thenReturnsSuccessAndCartIsEmpty() = runTest {
        //Given
        val cartItem = cartItemEntity { withProductId("id1"); withQuantity(2) }
        localDataSource.insertCartItem(cartItem)

        //When
        val result = localDataSource.deleteCartItem(cartItem)
        assertTrue(result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        //Then
        assertTrue(items.isEmpty())
    }

    @Test
    fun givenMultipleCartItem_whenDeleteCartItem_thenReturnsSuccessAndCartItemListIsUpdated() = runTest {
        val cartItem1 = cartItemEntity { withProductId("id1"); withQuantity(2) }
        val cartItem2 = cartItemEntity { withProductId("id2"); withQuantity(3) }
        val cartItem3 = cartItemEntity { withProductId("id3"); withQuantity(5) }

        localDataSource.insertCartItem(cartItem1)
        localDataSource.insertCartItem(cartItem2)
        localDataSource.insertCartItem(cartItem3)

        val result = localDataSource.deleteCartItem(cartItem2)
        val items = localDataSource.getAllCartItems().first()

        assertTrue(result.isSuccess)
        assertEquals(2, items.size)
        assertTrue(items.any { it.productId != "id2" })
    }

    @Test
    fun givenMultipleCartItems_whenClearCart_thenReturnsSuccessAndCartIsEmpty() = runTest {
        //Given
        val cartItem1 = cartItemEntity { withProductId("id1"); withQuantity(2) }
        val cartItem2 = cartItemEntity { withProductId("id2"); withQuantity(1) }
        val cartItem3 = cartItemEntity { withProductId("id3"); withQuantity(5) }
        localDataSource.insertCartItem(cartItem1)
        localDataSource.insertCartItem(cartItem2)
        localDataSource.insertCartItem(cartItem3)

        //When
        val result = localDataSource.clearCart()
        assertTrue(result.isSuccess)

        val items = localDataSource.getAllCartItems().first()

        //Then
        assertTrue(items.isEmpty())
    }


}