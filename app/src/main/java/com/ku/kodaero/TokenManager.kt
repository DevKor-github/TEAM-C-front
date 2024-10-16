package com.ku.kodaero

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object TokenManager {

    private const val PREFS_NAME = "token_prefs"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val USER_INFO_KEY = "user_info"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    // Initialize TokenManager
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save tokens
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply()
        }
    }

    // Retrieve AccessToken
    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    // Retrieve RefreshToken
    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }

    // Save UserInfo
    fun saveUserInfo(userInfo: UserInfo) {
        val userInfoJson = gson.toJson(userInfo)
        prefs.edit().putString(USER_INFO_KEY, userInfoJson).apply()
    }

    // Retrieve UserInfo
    fun getUserInfo(): UserInfo? {
        val userInfoJson = prefs.getString(USER_INFO_KEY, null)
        return if (userInfoJson != null) {
            gson.fromJson(userInfoJson, UserInfo::class.java)
        } else {
            null
        }
    }

    // Clear all stored data
    fun clearTokensAndUserInfo() {
        prefs.edit().clear().apply()
    }
}
