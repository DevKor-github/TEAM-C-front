package com.devkor.kodaero

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 개발 서버
    private const val BASE_URL = "http://43.202.47.183/api/"

    // 배포 서버
    // private const val BASE_URL = "http://3.36.90.27/api/"


    val instance: ApiService by lazy {
        // Logging Interceptor 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // 요청 및 응답 바디를 포함한 모든 정보를 로깅
        }

        // OkHttpClient를 설정하고 Interceptor 추가
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())  // AuthInterceptor 추가
            .addInterceptor(logging)  // HttpLoggingInterceptor 추가
            .build()

        // Retrofit 빌더에 OkHttpClient 추가
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)  // OkHttpClient를 Retrofit에 설정
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
