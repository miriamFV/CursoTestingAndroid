package com.example.cursotestingandroid.productlist.data.remote

import com.example.cursotestingandroid.core.domain.model.AppError
import com.example.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.example.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RemoteDataSourceTest {

    private val server = MockWebServer()
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var json: Json

    @Before
    fun setUp() {
        server.start()
        json = Json{
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(MarketApiService::class.java)
        remoteDataSource = RemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun givenEmptyJsonResponse_whenGetProducts_thenReturnsEmptyList() = runTest {
        //Given
        server.enqueue(MockResponse().setBody("""{"products":[]}""").setResponseCode(200))

        //When
        val response = remoteDataSource.getProducts()

        //Then
        assertTrue(response.isSuccess)
        assertTrue(response.getOrThrow().isEmpty())
    }

    @Test
    fun givenValidJasonFile_whenGetProducts_thenReturnsMappedDtos() = runTest {
        //Given
        val jsonResource = ClassLoader.getSystemResource("products_success.json").readText()
        server.enqueue(MockResponse().setBody(jsonResource).setResponseCode(200))

        //When
        val response = remoteDataSource.getProducts()

        //Then
        assertTrue(response.isSuccess)
        assertEquals(40, response.getOrNull()?.size)
    }

    @Test
    fun givenSerializeProducts_whenGetProducts_thenReturnsMatchesOriginalObject() = runTest {
        //Given
        val productResponse = ProductResponse(
            id = "id1",
            name = "pan",
            price = 100,
            category = "bread",
            stock = 5
        )
        val jsonString = json.encodeToString(ProductsResponse(listOf(productResponse)))
        server.enqueue(MockResponse().setBody(jsonString).setResponseCode(200))

        //When
        val response = remoteDataSource.getProducts()

        //Then
        assertTrue(response.isSuccess)
        assertEquals(1, response.getOrNull()?.size)
        assertTrue(response.getOrThrow().first().id == "id1")
    }

    @Test
    fun given404Response_whenGetProducts_thenReturnsNotFoundError() = runTest {
        //Given
        server.enqueue(MockResponse().setResponseCode(404))

        //When
        val response = remoteDataSource.getProducts()

        //Then
        assertTrue(response.isFailure)
        assertTrue(response.exceptionOrNull() is AppError.NotFoundError)
    }

    @Test
    fun givenMalformedJson_whenGetProducts_thenReturnsUnknownError() = runTest {
        //Given
        server.enqueue(MockResponse().setBody("errorjdnajdnf").setResponseCode(200))

        //When
        val response = remoteDataSource.getProducts()

        //Then
        assertTrue(response.isFailure)
        assertTrue(response.exceptionOrNull() is AppError.UnknownError)
    }

    @Test
    fun givenPromotionsRequest_whenGetPromotions_thenCallsCorrectEndpoint() = runTest {
        //Given
        server.enqueue(MockResponse().setBody("""{"promotions": []}""").setResponseCode(200))

        //When
        remoteDataSource.getPromotions()
        val result = server.takeRequest()

        //Then
        assertEquals("/data/promotions.json", result.path)
        assertEquals("GET", result.method)
    }


}