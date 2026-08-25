package com.example.cursotestingandroid.productlist.data.local.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.cursotestingandroid.core.builders.promotionEntity
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromotionDaoTest {
    private lateinit var database: MarketDatabase
    private lateinit var promotionDao: PromotionDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MarketDatabase::class.java,
                ).build()
        promotionDao = database.promotionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenEmptyDatabase_whenGetAllPromotions_thenEmitsEmptyList() =
        runTest {
            // Given --> Executed in setUp()
            // When
            val promotions = promotionDao.getAllPromotions().first()
            // Then
            assertTrue(promotions.isEmpty())
        }

    @Test
    fun givenTwoPromotions_whenInsertAndGetAllPromotions_thenReturnsAllPromotions() =
        runTest {
            // Given
            val promotion1 =
                promotionEntity {
                    withId("promo1")
                    withPercent(10)
                }
            val promotion2 =
                promotionEntity {
                    withId("promo2")
                    withPercent(20)
                }

            // When
            promotionDao.insertPromotions(listOf(promotion1, promotion2))
            val promotions = promotionDao.getAllPromotions().first()
            // Then

            assertTrue(promotions.isNotEmpty())
            assertEquals(2, promotions.size)
            assertEquals(10, promotions.find { it.id == "promo1" }?.percent)
            assertEquals(20, promotions.find { it.id == "promo2" }?.percent)
        }

    @Test
    fun givenMultiplePromotions_whenClearPromotions_thenDatabaseIsEmpty() =
        runTest {
            // Given
            val promotion1 =
                promotionEntity {
                    withId("promo1")
                }
            val promotion2 =
                promotionEntity {
                    withId("promo2")
                }
            val promotion3 =
                promotionEntity {
                    withId("promo3")
                }
            promotionDao.insertPromotions(listOf(promotion1, promotion2, promotion3))
            // When
            promotionDao.clearPromotions()

            // Then
            val result = promotionDao.getAllPromotions().first()
            assertTrue(result.isEmpty())
        }

    @Test
    fun givenInsertedPromotions_whenReplaceAllPromotions_thenOnlyNewPromotionsRemain() =
        runTest {
            // Given
            val oldPromotion1 =
                promotionEntity {
                    withId("promo1")
                    withPercent(20)
                }
            val oldPromotion2 =
                promotionEntity {
                    withId("promo2")
                    withPercent(10)
                }
            val oldPromotion3 =
                promotionEntity {
                    withId("promo3")
                    withPercent(50)
                }
            val oldPromotions = listOf(oldPromotion1, oldPromotion2, oldPromotion3)
            promotionDao.insertPromotions(oldPromotions)

            // When
            val promotion1 =
                promotionEntity {
                    withId("promo1")
                    withPercent(10)
                }
            val promotion2 =
                promotionEntity {
                    withId("promo2")
                    withPercent(60)
                }
            val newPromotions = listOf(promotion1, promotion2)
            promotionDao.replaceAll(newPromotions)

            // Then
            val result = promotionDao.getAllPromotions().first()
            assertTrue(result.isNotEmpty())
            assertEquals(2, result.size)
            assertTrue(result.any { it.id == "promo1" })
            assertTrue(result.any { it.id == "promo2" })
            assertTrue(result.none { it.id == "promo3" })
            assertEquals(10, result.find { it.id == "promo1" }?.percent)
            assertEquals(60, result.find { it.id == "promo2" }?.percent)
        }

    @Test
    fun givenFlowSubscribed_whenInsertAfterSubscribe_thenEmitUpdatedList() =
        runTest {
            // Given
            promotionDao.getAllPromotions().test {
                val initialValue = awaitItem()
                assertTrue(initialValue.isEmpty())

                // When
                promotionDao.insertPromotions(listOf(promotionEntity { withId("promo1") }))
                val updated = awaitItem()

                // Then
                assertEquals(1, updated.size)
            }
        }
}
