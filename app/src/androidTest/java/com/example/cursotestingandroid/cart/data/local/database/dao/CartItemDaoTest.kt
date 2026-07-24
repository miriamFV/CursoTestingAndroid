package com.example.cursotestingandroid.cart.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cursotestingandroid.core.builders.cartItemEntity
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartItemDaoTest {
    private lateinit var database: MarketDatabase
    private lateinit var cartItemDao: CartItemDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarketDatabase::class.java
        ).build()
        cartItemDao = database.cartItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllCartItems_thenEmitsEmptyList() = runTest {
        //Given --> Executed in setUp()
        //When
        val cartItems = cartItemDao.getAllCartItems().first()
        //Then
        assertTrue(cartItems.isEmpty())
    }

    @Test
    fun givenEmptyCart_whenInsertItem_thenItemIsPersisted() = runTest {
        //Given
        val productId = "productId"
        val quantity = 1
        val cartItemEntity = cartItemEntity{ withProductId(productId); withQuantity(quantity) }
        cartItemDao.insertCartItem(cartItemEntity)
        //When
        val result = cartItemDao.getAllCartItems().first()
        //Then
        assertEquals(1, result.size)
        assertEquals(productId, result.first().productId)
        assertEquals(quantity, result.first().quantity)
    }

    @Test
    fun givenInsertedCartItem_whenGetCartItemById_thenReturnsCartItem() = runTest {
        //Given
        val productId = "productId"
        val quantity = 3
        val cartItemEntity = cartItemEntity{ withProductId(productId); withQuantity(quantity) }
        cartItemDao.insertCartItem(cartItemEntity)
        //When
        val cartItem = cartItemDao.getCartItemById(productId)
        //Then
        assertNotNull(cartItem)
        assertEquals(productId, cartItem?.productId)
        assertEquals(quantity, cartItem?.quantity)
    }

    @Test
    fun givenEmptyCart_whenGetCartItemById_thenReturnsNull() = runTest {
        //Given
        val productId = "productId"
        //When
        val result = cartItemDao.getCartItemById(productId)
        //Then
        assertNull(result)
    }


    @Test
    fun givenExistingItem_whenUpdateItemQuentity_thenReturnsQuantityIsUpdated() = runTest {
        //Given
        val productId = "productId"
        val cartItemEntity = cartItemEntity{ withProductId(productId); withQuantity(1) }
        cartItemDao.insertCartItem(cartItemEntity)
        //When
        cartItemDao.updateCartItem(cartItemEntity{ withProductId(productId); withQuantity(15) })
        val result = cartItemDao.getCartItemById(productId)
        //Then
        assertEquals(15, result?.quantity)
    }


    @Test
    fun givenItemCart_whenDeleteItem_thenCartBecomesEmpty() = runTest {
        //Given
        val productId = "productId"
        val cartItemEntity = cartItemEntity{ withProductId(productId); withQuantity(1) }
        cartItemDao.insertCartItem(cartItemEntity)
        //When
        cartItemDao.deleteCartItem(cartItemEntity)
        val result = cartItemDao.getAllCartItems().first()
        //Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun givenMultipleItems_whenClearCart_thenCartBecomesEmpty() = runTest {
        //Given
        cartItemDao.insertCartItem(cartItemEntity{ withProductId("id1"); withQuantity(1) })
        cartItemDao.insertCartItem(cartItemEntity{ withProductId("id2"); withQuantity(1) })
        cartItemDao.insertCartItem(cartItemEntity{ withProductId("id3"); withQuantity(1) })
        //When
        cartItemDao.clearCart()
        val result = cartItemDao.getAllCartItems().first()
        //Then
        assertTrue(result.isEmpty())
    }


    @Test
    fun givenExistingItemId_whenInsertDuplicateId_thenItemIsReplaced() = runTest {
        //Given
        val item1 = cartItemEntity{ withProductId("id1"); withQuantity(1) }
        val item2 = cartItemEntity{ withProductId("id1"); withQuantity(7) }

        //When
        cartItemDao.insertCartItem(item1)
        cartItemDao.insertCartItem(item2)
        val result = cartItemDao.getAllCartItems().first()
        //Then
        assertEquals(7, result.first().quantity)
    }

}