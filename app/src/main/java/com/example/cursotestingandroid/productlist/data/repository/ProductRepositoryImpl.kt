package com.example.cursotestingandroid.productlist.data.repository

import com.example.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.example.cursotestingandroid.productlist.data.local.LocalDataSource
import com.example.cursotestingandroid.productlist.data.mappers.toDomainModel
import com.example.cursotestingandroid.productlist.data.mappers.toEntity
import com.example.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.example.cursotestingandroid.productlist.domain.model.Product
import com.example.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteDataSource,
        private val localDataSource: LocalDataSource,
        private val dispatchers: DispatchersProvider,
    ) : ProductRepository {
        private val refreshScope = CoroutineScope(SupervisorJob() + dispatchers.io)
        private val refreshMutex = Mutex()

        override fun getProducts(): Flow<List<Product>> {
            return localDataSource
                .getAllProducts()
                .map { entities -> entities.mapNotNull { productEntity -> productEntity.toDomainModel() } }
                .onStart {
                    refreshScope.launch {
                        if (!refreshMutex.tryLock()) return@launch
                        try {
                            refreshProduct()
                        } catch (e: Exception) {
                            // TODO
                        } finally {
                            refreshMutex.unlock()
                        }
                    }
                }.catch {
                    // TODO
                }
        }

        override fun getProductById(id: String): Flow<Product?> =
            localDataSource
                .getProductById(id)
                .map { entity ->
                    entity?.toDomainModel()
                }.catch { e: Throwable ->
                    // TODO analityc.trackError(e)
                }

        override fun getProductsByIds(ids: Set<String>): Flow<List<Product>> =
            localDataSource.getProductsByIds(ids).map { entities ->
                entities.mapNotNull { productEntity ->
                    productEntity.toDomainModel()
                }
            }

        override suspend fun refreshProduct() {
            withContext(dispatchers.io) {
                val products = remoteDataSource.getProducts().getOrThrow()
                val productsEntity =
                    products.map { productResponse ->
                        productResponse.toEntity()
                    }
                localDataSource.saveProducts(productsEntity)
            }
        }
    }
