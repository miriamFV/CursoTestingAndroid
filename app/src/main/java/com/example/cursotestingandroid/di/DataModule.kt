package com.example.cursotestingandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.example.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.example.cursotestingandroid.productlist.data.local.database.MarketDatabase
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
    fun provideDispatchersProvider(defaultDispatchersProvider: DefaultDispatchersProvider): DispatchersProvider{
        return defaultDispatchersProvider
    }

    @Provides
    @Singleton
    fun provideProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository {
        return productRepositoryImpl
    }

    @Provides
    @Singleton
    fun providePromotionRepository(promotionRepositoryImpl: PromotionRepositoryImpl): PromotionRepository {
        return promotionRepositoryImpl
    }

    @Provides
    fun providesProductDao(database: MarketDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun providesPromotionDao(database: MarketDatabase): PromotionDao {
        return database.promotionDao()
    }

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context):MarketDatabase{
        return Room.databaseBuilder(
            context = context,
            klass = MarketDatabase::class.java,
            name = "minimarket_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences>{
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository {
        return settingsRepositoryImpl
    }
}