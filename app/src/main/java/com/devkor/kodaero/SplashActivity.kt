package com.devkor.kodaero

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.view.View
import android.view.WindowInsetsController

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(Runnable {
            val i = Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(i)

            finish()
        }, 1000)


        // 상태 바 텍스트와 아이콘을 어두운 색상으로 설정
        setStatusBarTextColor(isLightText = false)

        // 상태 바 투명화 및 콘텐츠 확장 설정
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }
    }


    private fun setStatusBarTextColor(isLightText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wic = window.insetsController
            if (wic != null) {
                if (isLightText) {
                    // 상태 바 텍스트와 아이콘을 밝은 색상으로 설정
                    wic.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                } else {
                    // 상태 바 텍스트와 아이콘을 어두운 색상으로 설정
                    wic.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else {
            // Android 10 이하에서는 systemUiVisibility를 사용
            if (isLightText) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}