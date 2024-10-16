package com.ku.kodaero

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 요청 URL을 확인합니다.
        val requestUrl = originalRequest.url.toString()

        // "users/login" 경로에 대해서는 토큰을 추가하지 않습니다.
        return if (requestUrl.contains("users/login")) {
            // 토큰을 추가하지 않은 요청을 진행합니다.
            chain.proceed(originalRequest)
        } else {
            // TokenManager에서 액세스 토큰을 가져옵니다.
            val accessToken = TokenManager.getAccessToken()
            val refreshToken = TokenManager.getRefreshToken()

            // 액세스 토큰이 있는 경우 Authorization 헤더에 추가합니다.
            val modifiedRequest = originalRequest.newBuilder()
                .addHeader("AccessToken", "$accessToken")
                .addHeader("RefreshToken", "$refreshToken")
                .build()

            // 수정된 요청을 진행합니다.
            chain.proceed(modifiedRequest)
        }
    }
}

