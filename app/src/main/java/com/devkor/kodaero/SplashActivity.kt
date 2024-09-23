package com.devkor.kodaero

import android.content.Intent
import android.graphics.Color
import android.media.session.MediaSession.Token
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        TokenManager.init(this)

        viewModel = ViewModelProvider(this)[FetchDataViewModel::class.java]

        checkTokensAndFetchUserInfo()

        setStatusBarTextColor(isLightText = false)
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun checkTokensAndFetchUserInfo() {
        val accessToken = TokenManager.getAccessToken()
        val refreshToken = TokenManager.getRefreshToken()

        Log.e("SplashActivity", "accessToken: $accessToken")

        if (accessToken != null && refreshToken != null) {
            viewModel.fetchUserInfo()

            viewModel.userInfo.observe(this, Observer { userInfo ->
                if (userInfo != null) {
                    TokenManager.saveUserInfo(userInfo)
                    navigateToMainActivity()
                } else if (viewModel.responseCode == 403) {
                    navigateToLoginActivity()
                } else {
                    navigateToLoginActivity()
                }
            })
        } else {
            navigateToLoginActivity()
        }
    }

    private fun navigateToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun navigateToLoginActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun setStatusBarTextColor(isLightText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wic = window.insetsController
            if (wic != null) {
                if (isLightText) {
                    wic.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                } else {
                    wic.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else {
            if (isLightText) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
