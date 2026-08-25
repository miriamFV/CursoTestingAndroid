package com.example.cursotestingandroid.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.example.cursotestingandroid.cart.data.repository.CartRepositoryImpl
import com.example.cursotestingandroid.cart.domain.repository.CartRepository
import com.example.cursotestingandroid.checkout.data.repository.OrderRepositoryImpl
import com.example.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.example.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.example.cursotestingandroid.core.data.local.database.MarketDatabase
import com.example.cursotestingandroid.core.data.util.SystemClock
import com.example.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.example.cursotestingandroid.core.domain.util.Clock
import com.example.cursotestingandroid.di.DataModule
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
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

private val Context.testingDataStore: DataStore<Preferences> by preferencesDataStore("testing_settings")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
object TestDataModule {
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
    @Singleton
    fun provideOrderRepositoryImpl(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository = orderRepositoryImpl

    @Provides
    fun providesProductDao(database: MarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providesPromotionDao(database: MarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun providesCartItemDao(database: MarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun providesDatabase(): MarketDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, MarketDatabase::class.java).build()
    }

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> =
        ApplicationProvider.getApplicationContext<Context>().testingDataStore

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
