package com.example.cursotestingandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.example.cursotestingandroid.cart.data.repository.CartRepositoryImpl
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import com.example.cursotestingandroid.core.data.util.SystemClock
import com.example.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.example.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.example.cursotestingandroid.productlist.data.repository.ProductRepositoryImpl
import com.example.cursotestingandroid.productlist.data.repository.PromotionRepositoryImpl
import com.example.cursotestingandroid.productlist.data.repository.SettingsRepositoryImpl
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.example.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.example.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDispatchersProvider(defaultDispatchersProvider: DefaultDispatchersProvider): DispatchersProvider =
        defaultDispatchersProvider

    @Provides
    @Singleton
    fun provideProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository =
        productRepositoryImpl

    @Provides
    @Singleton
    fun providePromotionRepository(promotionRepositoryImpl: PromotionRepositoryImpl): PromotionRepository =
        promotionRepositoryImpl

    @Provides
    fun providesProductDao(database: MarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providesPromotionDao(database: MarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun providesCartItemDao(database: MarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
    ): MarketDatabase =
        Room
            .databaseBuilder(
                context = context,
                klass = MarketDatabase::class.java,
                name = "minimarket_database",
            ).build()

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository =
        settingsRepositoryImpl

    @Provides
    @Singleton
    fun provideCartRepository(cartRepositoryImpl: CartRepositoryImpl): CartRepository = cartRepositoryImpl

    @Provides
    @Singleton
    fun provideClock(systemClock: SystemClock): Clock = systemClock
}
