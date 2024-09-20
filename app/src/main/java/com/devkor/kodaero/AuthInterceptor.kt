package com.devkor.kodaero

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // TokenManager에서 액세스 토큰을 가져옵니다.
        val accessToken = TokenManager.getAccessToken()

        // 액세스 토큰이 있는 경우 Authorization 헤더에 추가합니다.
        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("AccessToken", "$accessToken")
            .build()

        // 수정된 요청을 진행합니다.
        return chain.proceed(modifiedRequest)
    }
}
