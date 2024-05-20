package com.example.deckor_teamc_front

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit 설정 및 API 인터페이스 정의
object RetrofitInstance {
    private const val BASE_URL = "http://3.34.68.172:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("api/search")
    suspend fun getBuildingItems(
        @Query("buildingId") buildingId: Int,
        @Query("keyword") keyword: String
    ): ApiResponse
}

// 데이터 모델 정의
data class ApiResponse(
    val statusCode: Int,
    val message: String,
    val data: List<BuildingItem>
)



